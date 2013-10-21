/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.presenter;

import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceDataProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.MongoDbDatabaseDto;
import org.obiba.opal.web.model.client.database.SqlDatabaseDto;
import org.obiba.opal.web.model.client.opal.AclAction;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;

public class DataDatabasesPresenter extends PresenterWidget<DataDatabasesPresenter.Display> implements RequestAdministrationPermissionEvent.Handler {

  private final ModalProvider<SqlDatabasePresenter> sqlDatabaseModalProvider;

  private final ModalProvider<MongoDatabasePresenter> mongoDatabaseModalProvider;

  private final AuthorizationPresenter authorizationPresenter;

  private final ResourceDataProvider<DatabaseDto> resourceSqlDatabasesProvider = new ResourceDataProvider<DatabaseDto>(
      DatabaseResources.sqlDatabases());

  private final ResourceDataProvider<DatabaseDto> resourceMongoDbProvider = new ResourceDataProvider<DatabaseDto>(
      DatabaseResources.mongoDatabases());

  private Command confirmedCommand;

  @Inject
  public DataDatabasesPresenter(Display display, EventBus eventBus,
      ModalProvider<SqlDatabasePresenter> sqlDatabaseModalProvider,
      ModalProvider<MongoDatabasePresenter> mongoDatabaseModalProvider,
      Provider<AuthorizationPresenter> authorizationPresenter) {
    super(eventBus, display);
    this.sqlDatabaseModalProvider = sqlDatabaseModalProvider.setContainer(this);
    this.mongoDatabaseModalProvider = mongoDatabaseModalProvider.setContainer(this);
    this.authorizationPresenter = authorizationPresenter.get();
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.sqlDatabases()) //
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListSqlDatabasesAuthorization())) //
        .get().send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.mongoDatabases()) //
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListMongoDbAuthorization())) //
        .get().send();
  }

  @Override
  protected void onReveal() {
    refresh();
  }

  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.sqlDatabases()) //
        .authorize(authorizer) //
        .get().send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(DatabaseResources.mongoDatabases()) //
        .authorize(authorizer) //
        .get().send();
  }

  @Override
  protected void onBind() {
    super.onBind();

    registerHandler(
        getEventBus().addHandler(DatabaseCreatedEvent.getType(), new DatabaseCreatedEvent.DatabaseCreatedHandler() {
          @Override
          public void onDatabaseCreated(DatabaseCreatedEvent event) {
            refresh();
          }
        }));
    registerHandler(
        getEventBus().addHandler(DatabaseUpdatedEvent.getType(), new DatabaseUpdatedEvent.DatabaseUpdatedHandler() {
          @Override
          public void onDatabaseUpdated(DatabaseUpdatedEvent event) {
            refresh();
          }
        }));
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {

      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if(event.getSource() == confirmedCommand && event.isConfirmed()) {
          confirmedCommand.execute();
        }
      }
    }));

    getView().getActions().setActionHandler(new ActionHandler<DatabaseDto>() {

      @Override
      public void doAction(DatabaseDto dto, String actionName) {
        if(dto.getEditable() && actionName.equalsIgnoreCase(DELETE_ACTION)) {

          getEventBus().fireEvent(ConfirmationRequiredEvent
              .createWithKeys(confirmedCommand = new DeleteDatabaseCommand(dto), "deleteDatabase",
                  "confirmDeleteDatabase"));

        } else if(dto.getEditable() && actionName.equalsIgnoreCase(EDIT_ACTION)) {

          SqlDatabaseDto sqlDatabaseDto = (SqlDatabaseDto) dto
              .getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings);
          MongoDbDatabaseDto mongoDbDatabaseDto = (MongoDbDatabaseDto) dto
              .getExtension(MongoDbDatabaseDto.DatabaseDtoExtensions.settings);
          if(sqlDatabaseDto != null) {
            sqlDatabaseModalProvider.get().editDatabase(dto);
          } else if(mongoDbDatabaseDto != null) {
            mongoDatabaseModalProvider.get().editDatabase(dto);
          }

        } else if(actionName.equalsIgnoreCase(Display.TEST_ACTION)) {
          DatabaseAdministrationPresenter.testConnection(getEventBus(), dto.getName());
        }
      }

    });

    registerHandler(getView().getAddSqlButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        sqlDatabaseModalProvider.get().createNewDatabase();
      }

    }));
    registerHandler(getView().getAddMongoButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        mongoDatabaseModalProvider.get().createNewDatabase();
      }

    }));

    authorizationPresenter
        .setAclRequest("databases", new AclRequest(AclAction.DATABASES_ALL, DatabaseResources.databases()));
  }

  private void refresh() {
    getView().getSqlTable().setVisibleRangeAndClearData(new Range(0, 10), true);
    getView().getMongoTable().setVisibleRangeAndClearData(new Range(0, 10), true);
  }



  public interface Display extends View {

    String TEST_ACTION = "Test";

    HasActionHandler<DatabaseDto> getActions();

    HasClickHandlers getAddSqlButton();

    HasData<DatabaseDto> getSqlTable();

    HasData<DatabaseDto> getMongoTable();

    HasClickHandlers getAddMongoButton();

  }

  private class DeleteDatabaseCommand implements Command {

    private final DatabaseDto dto;

    private DeleteDatabaseCommand(DatabaseDto dto) {
      this.dto = dto;
    }

    @Override
    public void execute() {
      deleteDatabase(dto);
    }

    private void deleteDatabase(DatabaseDto database) {
      ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
          .forResource(DatabaseResources.database(database.getName())) //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              refresh();
            }

          }) //
          .delete().send();
    }
  }



  private final class ListSqlDatabasesAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      // Only bind the table to its data provider if we're authorized
      if(resourceSqlDatabasesProvider.getDataDisplays().isEmpty()) {
        resourceSqlDatabasesProvider.addDataDisplay(getView().getSqlTable());
      }
    }

    @Override
    public void unauthorized() {
    }

  }

  private final class ListMongoDbAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      // Only bind the table to its data provider if we're authorized
      if(resourceMongoDbProvider.getDataDisplays().isEmpty()) {
        resourceMongoDbProvider.addDataDisplay(getView().getMongoTable());
      }
    }

    @Override
    public void unauthorized() {
    }

  }

}