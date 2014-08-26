/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.copyscripts.view;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.copyscripts.presenter.CopyScriptsPresenter;
import org.obiba.opal.web.gwt.app.client.magma.copyscripts.presenter.CopyScriptsUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import javax.annotation.Nullable;
import java.util.List;

/**
 * View of the dialog used to export data from Opal.
 */
public class CopyScriptsView extends ModalPopupViewWithUiHandlers<CopyScriptsUiHandlers>
    implements CopyScriptsPresenter.Display {

  interface Binder extends UiBinder<Widget, CopyScriptsView> {}

  @UiField
  Modal modal;

  @UiField
  Alert copyNTable;

  @UiField
  ListBox datasources;

  @UiField
  ControlGroup newTableNameGroup;

  @UiField
  TextBox newName;

  @UiField
  CheckBox incremental;

  @UiField
  CheckBox copyNullValues;

  @Inject
  public CopyScriptsView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.copyData());
  }

  @UiHandler("submitButton")
  public void onSubmit(ClickEvent event) {
    getUiHandlers().onSubmit(getSelectedDatasource(), newName.getText());
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public void setDatasources(List<ViewDto> datasources) {
    this.datasources.clear();
    for(ViewDto datasource : datasources) {
      //if(!"null".equals(datasource.getType())) {
        this.datasources.addItem(datasource.getName());
      //}
    }
  }

  @Override
  public void showNewName(String name) {
    newTableNameGroup.setVisible(true);
    newName.setText(name);
  }

  @Override
  public boolean isIncremental() {
    return incremental.getValue();
  }

  @Override
  public boolean isCopyNullValues() {
    return copyNullValues.getValue();
  }

  @Override
  public boolean isWithVariables() {
    return true;
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void showCopyNAlert(String message) {
    copyNTable.setText(message);
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NEW_TABLE_NAME:
          group = newTableNameGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  private String getSelectedDatasource() {
    return datasources.getValue(datasources.getSelectedIndex());
  }
}
