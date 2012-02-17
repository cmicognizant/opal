/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DestinationSelectionStepPresenter extends WidgetPresenter<DestinationSelectionStepPresenter.Display> {

  public interface Display extends WidgetDisplay, WizardStepDisplay {

    void setDatasources(JsArray<DatasourceDto> datasources);

    String getSelectedDatasource();

    boolean hasTable();

    String getSelectedTable();

    void setTable(String name);

    void showTables(boolean visible);

  }

  private ImportFormat importFormat;

  JsArray<DatasourceDto> datasources;

  @Inject
  public DestinationSelectionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  public void setImportFormat(ImportFormat importFormat) {
    this.importFormat = importFormat;
    hideShowTables();
    refreshDatasources();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
  }

  @Override
  protected void onUnbind() {
  }

  private void refreshDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> resource) {
        datasources = JsArrays.toSafeArray(resource);

        for(int i = 0; i < datasources.length(); i++) {
          DatasourceDto d = datasources.get(i);
          d.setTableArray(JsArrays.toSafeArray(d.getTableArray()));
          d.setViewArray(JsArrays.toSafeArray(d.getViewArray()));
        }

        getDisplay().setDatasources(datasources);
        // updateSelectableDatasources();
      }
    }).send();
  }

  public boolean validate() {
    if(ImportFormat.XML.equals(importFormat) == false) {
      // table cannot be empty and cannot be a view
      if(getDisplay().hasTable() == false) {
        eventBus.fireEvent(NotificationEvent.newBuilder().error("DestinationTableRequired").build());
        return false;
      }
      return validateDestinationTableIsNotView();
    }
    return true;
  }

  private boolean validateDestinationTableIsNotView() {
    String dsName = getDisplay().getSelectedDatasource();
    String tableName = getDisplay().getSelectedTable();
    for(int i = 0; i < datasources.length(); i++) {
      DatasourceDto ds = datasources.get(i);
      if(ds.getName().equals(dsName) && ds.getViewArray() != null) {
        for(int j = 0; j < ds.getViewArray().length(); j++) {
          if(ds.getViewArray().get(j).equals(tableName)) {
            eventBus.fireEvent(NotificationEvent.newBuilder().error("DestinationTableCannotBeView").build());
            return false;
          }
        }
        return true;
      }
    }

    return true;
  }

  private void hideShowTables() {
    getDisplay().showTables(ImportFormat.XML.equals(importFormat) == false);
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  public void refreshDisplay() {
    refreshDatasources();
  }

  @Override
  public void revealDisplay() {
  }

  public void updateImportData(ImportData importData) {
    importData.setDestinationDatasourceName(getDisplay().getSelectedDatasource());
    if(getDisplay().hasTable()) {
      importData.setDestinationTableName(getDisplay().getSelectedTable());
    } else
      importData.setDestinationTableName(null);
  }
}
