/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.FileDto;
import org.obiba.opal.web.model.client.FunctionalUnitDto;
import org.obiba.opal.web.model.client.ImportCommandOptionsDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

/**
 *
 */
public class DataImportPresenter extends WidgetPresenter<DataImportPresenter.Display> {

  public interface Display extends WidgetDisplay {
    HasCloseHandlers<PopupPanel> getDialogBox();

    void setDatasources(JsArray<DatasourceDto> datasources);

    String getSelectedDatasource();

    void showDialog();

    /**
     * @param root
     */
    void setFiles(FileDto root);

    /**
     * @return
     */
    HasClickHandlers getImport();

    /**
     * @param units
     */
    void setUnits(JsArray<FunctionalUnitDto> units);

    /**
     * @return
     */
    String getArchiveDirectory();

    /**
     * @return
     */
    String getSelectedUnit();

    /**
     * @return
     */
    JsArrayString getFiles();

    /**
     * @param text
     */
    void showErrors(String text);

    /**
     * 
     */
    void hideDialog();
  }

  private final ResourceRequestBuilderFactory factory;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public DataImportPresenter(final Display display, final EventBus eventBus, ResourceRequestBuilderFactory factory) {
    super(display, eventBus);
    this.factory = factory;
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {

    factory.<FileDto> newBuilder().forResource("/files").get().withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto root) {
        getDisplay().setFiles(root);
      }
    }).send();

    factory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
        getDisplay().setDatasources(datasources);
      }
    }).send();

    factory.<JsArray<FunctionalUnitDto>> newBuilder().forResource("/functional-units").get().withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
      @Override
      public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
        getDisplay().setUnits(units);
      }
    }).send();

    getDisplay().getDialogBox().addCloseHandler(new CloseHandler<PopupPanel>() {
      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        unbind();
      }
    });

    getDisplay().getImport().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ImportCommandOptionsDto dto = ImportCommandOptionsDto.create();
        dto.setDestination(getDisplay().getSelectedDatasource());
        dto.setArchive(getDisplay().getArchiveDirectory());
        dto.setFilesArray(getDisplay().getFiles());
        dto.setUnit(getDisplay().getSelectedUnit());
        factory.newBuilder().forResource("/shell/import").post().withResourceBody(ImportCommandOptionsDto.stringify(dto)).withCallback(400, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            getDisplay().showErrors(response.getText());
          }
        }).withCallback(204, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            getDisplay().hideDialog();
          }
        }).send();
      }

    });
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

}
