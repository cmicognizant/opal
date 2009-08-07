/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map;

import java.util.Locale;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class GraphBuilder {

  private static final Logger log = LoggerFactory.getLogger(GraphBuilder.class);

  private Graph graph;

  private String baseUri;

  private Resource resource;

  private boolean changed = false;

  public GraphBuilder(Graph graph, String baseUri) {
    this.graph = graph;
    this.baseUri = baseUri;
  }

  public GraphBuilder forResource(String type, String localName) {
    this.resource = graph.getValueFactory().createURI(baseUri + "/" + type + "/", localName);
    return this;
  }

  public Graph build() {
    return graph;
  }

  public Graph getCurrentGraph() {
    return graph;
  }

  public GraphBuilder withLiteral(URI predicate, String value) {
    return withLiteral(predicate, value, null);
  }

  public GraphBuilder withLiteral(URI predicate, String value, Locale locale) {
    if(value == null) throw new IllegalArgumentException("Cannot add predicate '" + predicate + "': literal value cannot be null");
    Literal literal = graph.getValueFactory().createLiteral(value, locale != null ? locale.getLanguage() : null);
    log.info("adding statement {} {} {}", new Object[] { resource, predicate, literal });
    trackChange(graph.add(resource, predicate, literal));
    return this;
  }

  public void clearChanged() {
    changed = false;
  }

  public boolean hasChanged() {
    return changed;
  }

  public GraphBuilder withRelation(URI predicate, String type, String related) {
    URI relation = graph.getValueFactory().createURI(baseUri + "/" + type + "/", related);
    return withRelation(predicate, relation);
  }

  public GraphBuilder withRelation(URI predicate, URI related) {
    log.info("adding statement {} {} {}", new Object[] { resource, predicate, related });
    trackChange(graph.add(resource, predicate, related));
    return this;
  }

  protected void trackChange(boolean change) {
    changed = changed || change;
    log.info("graph changed {}", changed);
  }
}
