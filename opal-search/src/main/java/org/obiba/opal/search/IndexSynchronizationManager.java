/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.security.BackgroundJobServiceAuthToken;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.search.service.OpalSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@code IndexSynchronization} tasks. This class will monitor the state of all indices periodically. When an
 * index is determined to be out of date, a {@code IndexSynchronization} task is created and run.
 */
@Component
@Transactional(readOnly = true)
public class IndexSynchronizationManager {

  private static final Logger log = LoggerFactory.getLogger(IndexSynchronizationManager.class);

  // Grace period before reindexing (in seconds)
  private static final int GRACE_PERIOD = 300;

  @Autowired
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private Set<IndexManager> indexManagers;

  @Autowired
  private OpalSearchService opalSearchService;

  @Autowired
  private TransactionalThreadFactory transactionalThreadFactory;

  private final SyncProducer syncProducer = new SyncProducer();

  private SyncConsumer syncConsumer;

  private IndexSynchronization currentTask;

  private final BlockingQueue<IndexSynchronization> indexSyncQueue = new LinkedBlockingQueue<>();

  private Thread consumer;

  // Every minute
  @Scheduled(fixedDelay = 60 * 1000)
  public void synchronizeIndices() {

    if(!opalSearchService.isRunning()) return;

    if(syncConsumer == null) {
      // start one IndexSynchronization consumer thread per index manager
      syncConsumer = new SyncConsumer();
      startConsumerThread();
    } else if(consumer != null && !consumer.isAlive() && !consumer.isInterrupted()) {
      // restart consumer if it died unexpectedly
      startConsumerThread();
    }
    getSubject().execute(syncProducer);
  }

  public void synchronizeIndex(IndexManager indexManager, ValueTable table, int gracePeriod) {
    syncProducer.index(indexManager, table, gracePeriod);
  }

  /**
   * This does the same as synchronizeIndex, but if the index is currently being updated it is cancelled and re-added
   * to the queue.
   *
   * @param indexManager
   * @param table
   * @param gracePeriod
   */
  public void restartSynchronizeIndex(IndexManager indexManager, ValueTable table, int gracePeriod) {
    if(currentTask != null &&
           currentTask.getValueTable().getName().equals(table.getName()) &&
           currentTask.getValueTable().getDatasource().getName().equals(table.getDatasource().getName())) {
      stopTask();
    }

    syncProducer.index(indexManager, table, gracePeriod, true);
  }

  public IndexSynchronization getCurrentTask() {
    return currentTask;
  }

  public void stopTask() {
    currentTask.stop();
    syncProducer.deleteCurrentTaskFromQueue();
  }

  private void startConsumerThread() {
    consumer = transactionalThreadFactory.newThread(getSubject().associateWith(syncConsumer));
    consumer.setName("Index Synchronization Consumer " + syncConsumer);
    consumer.setPriority(Thread.MIN_PRIORITY);
    consumer.start();
  }

  public void terminateConsumerThread() {
    if(consumer != null && consumer.isAlive()) consumer.interrupt();
  }

  private Subject getSubject() {
    // Login as background task user
    try {
      PrincipalCollection principals = SecurityUtils.getSecurityManager()
          .authenticate(BackgroundJobServiceAuthToken.INSTANCE).getPrincipals();
      return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
    } catch(AuthenticationException e) {
      log.warn("Failed to obtain system user credentials: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private class SyncProducer implements Runnable {

    @Override
    public void run() {
      try {
        for(Datasource ds : MagmaEngine.get().getDatasources()) {
          for(ValueTable table : ds.getValueTables()) {
            log.debug("Check index for table: {}.{}", ds.getName(), table.getName());
            for(IndexManager indexManager : indexManagers) {
              checkIndexable(indexManager, table);
            }
          }
        }
      } catch(Exception ignored) {
        log.debug("Error while checking indexable", ignored);
      }
    }

    private void checkIndexable(IndexManager indexManager, ValueTable table) {
      if(indexManager.isReady() && indexManager.isIndexable(table)) {
        ValueTableIndex index = indexManager.getIndex(table);
        // Check that the index is older than the ValueTable
        if(index.requiresUpgrade() || !index.isUpToDate()) {
          index(indexManager, table, GRACE_PERIOD);
        }
      }
    }

    private void index(IndexManager indexManager, ValueTable table, int seconds) {
      index(indexManager, table, seconds, false);
    }

    private void index(IndexManager indexManager, ValueTable table, int seconds, boolean force) {
      // The index needs to be updated
      Value value = table.getTimestamps().getLastUpdate();
      // Check that the last modification to the ValueTable is older than the gracePeriod
      // If we don't know (null value), reindex
      if(value.isNull() || value.compareTo(gracePeriod(seconds)) < 0) {
        if (force) {
          forceSubmitTask(indexManager, table);
        } else {
          submitTask(indexManager, table);
        }
      }
    }

    /**
     * Returns a {@code Value} with the date and time at which things are reindexed.
     *
     * @return value
     */
    private Value gracePeriod(int seconds) {
      // Now
      Calendar gracePeriod = Calendar.getInstance();
      // Move back in time by GRACE_PERIOD seconds
      gracePeriod.add(Calendar.SECOND, -seconds);
      // Things modified before this value can be reindexed
      return DateTimeType.get().valueOf(gracePeriod);
    }

    /**
     * Check if the index is not the current task, or in the queue before adding it to the indexation queue.
     *
     * @param indexManager
     * @param table
     */
    private void submitTask(IndexManager indexManager, ValueTable table) {
      ValueTableIndex index = indexManager.getIndex(table);
      if(currentTask != null && currentTask.getIndexManager().getName().equals(indexManager.getName()) &&
          currentTask.getValueTableIndex().getIndexName().equals(index.getIndexName())) return;

      if(!isAlreadyQueued(indexManager, index)) {
        forceSubmitTask(indexManager, table);
      }
    }

    /**
     * Add an index to the indexation queue without checking if it is already in there or if it is the currently
     * running task. This can be useful to re-start a currently running index job that has been signalled to stop but
     * hasn't stopped yet.
     *
     * @param indexManager
     * @param table
     */
    private void forceSubmitTask(IndexManager indexManager, ValueTable table) {
      ValueTableIndex index = indexManager.getIndex(table);
      log.trace("Queueing for indexing {} in {}", index.getIndexName(), indexManager.getName());
      indexSyncQueue.offer(indexManager.createSyncTask(table, index));
    }

    private void deleteCurrentTaskFromQueue() {
      log.trace("Deleting current task from queue : {}", currentTask.getValueTable().getName());
      indexSyncQueue.remove(currentTask);
    }
  }

  public boolean isAlreadyQueued(IndexManager indexManager, ValueTableIndex index) {
    for(IndexSynchronization s : indexSyncQueue) {
      if(s.getValueTableIndex().getIndexName().equals(index.getIndexName()) &&
          s.getIndexManager().getName().equals(indexManager.getName())) {
        log.trace("Indexation is already queued...");
        return true;
      }
    }
    log.trace("Indexation is not queued...");
    return false;
  }

  private class SyncConsumer implements Runnable {

    @Override
    public void run() {
      log.debug("Starting indexing consumer");
      try {
        //noinspection InfiniteLoopStatement
        while(true) {
          consume(indexSyncQueue.take());
        }
      } catch(InterruptedException ignored) {
        log.debug("Stopping indexing consumer");
      }
    }

    private void consume(IndexSynchronization sync) {
      currentTask = sync;
      try {
        log.trace("Prepare indexing {} in {}", sync.getValueTableIndex().getIndexName(),
            sync.getIndexManager().getName());
        // check if still indexable: indexation config could have changed
        if(sync.getIndexManager().isReady()) {
          getSubject().execute(sync);
        }
      } catch(NoSuchDatasourceException | NoSuchValueTableException e) {
        log.trace("Cannot index: ", e.getMessage());
      } finally {
        currentTask = null;
      }
    }
  }
}
