/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class CreateFolderDialogPresenter extends WidgetPresenter<CreateFolderDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    HasClickHandlers getCreateFolderButton();

    HasClickHandlers getCancelButton();

    HasText getFolderToCreate();

    HasCloseHandlers<DialogBox> getDialog();

  }

  private Translations translations = GWT.create(Translations.class);

  private FileDto currentFolder;

  @Inject
  public CreateFolderDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addEventHandlers();
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
    initDisplayComponents();
    getDisplay().showDialog();
  }

  protected void initDisplayComponents() {
    getDisplay().getFolderToCreate().setText("");
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getCreateFolderButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        String folderToCreate = getDisplay().getFolderToCreate().getText();
        if(folderToCreate.equals("")) {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, translations.folderNameIsRequired(), null));
        } else if(folderToCreate.equals(".") || folderToCreate.equals("..")) {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, translations.dotNamesAreInvalid(), null));
        } else {
          if(currentFolder.getPath().equals("/")) { // create under root
            createFolder("/", folderToCreate);
          } else {
            createFolder(currentFolder.getPath(), folderToCreate);
          }
        }
      }
    }));

    super.registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    });

  }

  private void createFolder(final String destination, final String folder) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 201) {
          eventBus.fireEvent(new FolderCreationEvent(destination + "/" + folder));
          getDisplay().hideDialog();
        } else {
          GWT.log(response.getText());
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/files" + destination).post().withBody("text/plain", folder).withCallback(201, callbackHandler).withCallback(403, callbackHandler).withCallback(500, callbackHandler).send();
  }

  public void setCurrentFolder(FileDto currentFolder) {
    this.currentFolder = currentFolder;
  }
}
