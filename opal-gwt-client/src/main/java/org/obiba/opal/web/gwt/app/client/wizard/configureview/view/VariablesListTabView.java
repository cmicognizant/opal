/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.VariablesListTabPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class VariablesListTabView extends ViewImpl implements VariablesListTabPresenter.Display {

  private static final VariablesListTabViewUiBinder uiBinder = GWT.create(VariablesListTabViewUiBinder.class);

  private final Widget widget;

  @UiField(provided = true)
  SuggestBox variableNameSuggestBox;

  @UiField
  Anchor previous;

  @UiField
  Anchor next;

  @UiField
  Button saveChangesButton;

  @UiField
  Button addButton;

  @UiField
  Button removeButton;

  @UiField
  TextBox variableName;

  @UiField
  ListBox valueType;

  @UiField
  CheckBox repeatableCheckbox;

  @UiField
  SimplePanel scriptWidgetPanel;

  @UiField
  HorizontalTabLayout variableDetailTabs;

  @UiField
  SimplePanel categoriesTabPanel;

  @UiField
  SimplePanel attributesTabPanel;

  @UiField
  SimplePanel summaryTabPanel;

  @UiField
  TextBox occurenceGroup;

  @UiField
  TextBox mimeType;

  @UiField
  TextBox unit;

  MultiWordSuggestOracle suggestions;

  private String entityType;

  public VariablesListTabView() {
    variableNameSuggestBox = new SuggestBox(suggestions = new MultiWordSuggestOracle());
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public HasBeforeSelectionHandlers<Integer> getDetailTabs() {
    return variableDetailTabs;
  }

  @Override
  public void displayDetailTab(int tabNumber) {
    variableDetailTabs.selectTab(tabNumber);
  }

  @Override
  public int getSelectedTab() {
    return variableDetailTabs.getSelectedIndex();
  }

  @Override
  public void addCategoriesTabWidget(Widget categoriesTabWidget) {
    categoriesTabPanel.clear();
    categoriesTabPanel.add(categoriesTabWidget);
  }

  @Override
  public void addAttributesTabWidget(Widget attributesTabWidget) {
    attributesTabPanel.clear();
    attributesTabPanel.add(attributesTabWidget);
  }

  @Override
  public void addSummaryTabWidget(Widget summaryTabWidget) {
    summaryTabPanel.clear();
    summaryTabPanel.add(summaryTabWidget);
  }

  @Override
  public void addVariableNameSuggestion(String variableName) {
    suggestions.add(variableName);
  }

  @Override
  public void clearVariableNameSuggestions() {
    suggestions.clear();
  }

  @UiTemplate("VariablesListTabView.ui.xml")
  interface VariablesListTabViewUiBinder extends UiBinder<Widget, VariablesListTabView> {
  }

  @Override
  public HandlerRegistration addPreviousVariableNameClickHandler(ClickHandler handler) {
    return previous.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addNextVariableNameClickHandler(ClickHandler handler) {
    return next.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addVariableNameChangedHandler(ValueChangeHandler<String> handler) {
    return variableNameSuggestBox.addValueChangeHandler(handler);
  }

  @Override
  public HandlerRegistration addVariableNameSelectedHandler(SelectionHandler<Suggestion> handler) {
    return variableNameSuggestBox.addSelectionHandler(handler);
  }

  @Override
  public HandlerRegistration addVariableNameEnterKeyPressed(KeyDownHandler handler) {
    return variableNameSuggestBox.addKeyDownHandler(handler);
  }

  @Override
  public void setSelectedVariableName(String variableName, String previousVariableName, String nextVariableName) {
    variableNameSuggestBox.setText(variableName);
    previous.setTitle(previousVariableName);
    next.setTitle(nextVariableName);

    previous.setEnabled(previousVariableName != null);
    next.setEnabled(nextVariableName != null);
  }

  @Override
  public String getSelectedVariableName() {
    return variableNameSuggestBox.getTextBox().getText();
  }

  @Override
  public HandlerRegistration addRepeatableValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    return repeatableCheckbox.addValueChangeHandler(handler);
  }

  @Override
  public void setEnabledOccurenceGroup(Boolean enabled) {
    occurenceGroup.setEnabled(enabled);
  }

  @Override
  public void clearOccurrenceGroup() {
    occurenceGroup.setText("");
  }

  @Override
  public HasText getOccurenceGroup() {
    return occurenceGroup;
  }

  @Override
  public HasValue<Boolean> getRepeatable() {
    return repeatableCheckbox;
  }

  @Override
  public HandlerRegistration addSaveChangesClickHandler(ClickHandler handler) {
    return saveChangesButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addAddVariableClickHandler(ClickHandler handler) {
    return addButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addVariableClickHandler(ClickHandler handler) {
    return addButton.addClickHandler(handler);
  }

  @Override
  public void setNewVariable(VariableDto variableDto) {
    // Set the entity type (not displayed)
    entityType = variableDto.getEntityType();

    // Set the UI fields.
    variableName.setValue(variableDto.getName());
    setValueType(variableDto);
    repeatableCheckbox.setValue(variableDto.getIsRepeatable());
    setOccurrenceGroup(variableDto);
    setUnit(variableDto);
    setMimeType(variableDto);
  }

  @Override
  public VariableDto getVariableDto(String script) {
    VariableDto variableDto = VariableDto.create();
    variableDto.setName(variableName.getValue());
    variableDto.setIsRepeatable(repeatableCheckbox.getValue());
    if(repeatableCheckbox.getValue()) variableDto.setOccurrenceGroup(occurenceGroup.getValue());
    variableDto.setValueType(valueType.getValue(valueType.getSelectedIndex()));
    variableDto.setEntityType(entityType);
    variableDto.setMimeType(mimeType.getValue());
    variableDto.setUnit(unit.getValue());
    VariableDtos.setScript(variableDto, script);
    return variableDto;
  }


  @Override
  public HandlerRegistration addRemoveVariableClickHandler(ClickHandler handler) {
    return removeButton.addClickHandler(handler);
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == Slots.Test) {
      scriptWidgetPanel.add(content);
    }
  }

  @Override
  public void setScriptWidgetVisible(boolean visible) {
    scriptWidgetPanel.setVisible(visible);
  }

  //
  // Methods
  //

  private void setValueType(VariableDto variableDto) {
    for(int i = 0; i < valueType.getItemCount(); i++) {
      valueType.setItemSelected(i, valueType.getValue(i).equals(variableDto.getValueType()));
    }
    if(valueType.getSelectedIndex() == -1) {
      valueType.setSelectedIndex(0);
    }
  }

  private void setOccurrenceGroup(VariableDto variableDto) {
    if(variableDto.getIsRepeatable()) {
      occurenceGroup.setEnabled(true);

      if(variableDto.hasOccurrenceGroup()) {
        occurenceGroup.setText(variableDto.getOccurrenceGroup());
      } else {
        occurenceGroup.setText("");
      }
    } else {
      occurenceGroup.setEnabled(false);
      occurenceGroup.setText("");
    }
  }

  private void setUnit(VariableDto variableDto) {
    if(variableDto.hasUnit()) {
      unit.setText(variableDto.getUnit());
    } else {
      unit.setText("");
    }
  }

  private void setMimeType(VariableDto variableDto) {
    if(variableDto.hasMimeType()) {
      mimeType.setText(variableDto.getMimeType());
    } else {
      mimeType.setText("");
    }
  }

  @Override
  public void saveChangesEnabled(boolean enabled) {
    saveChangesButton.setEnabled(enabled);
  }

  @Override
  public void removeButtonEnabled(boolean enabled) {
    removeButton.setEnabled(enabled);
  }

  @Override
  public void addButtonEnabled(boolean enabled) {
    addButton.setEnabled(enabled);
  }

  @Override
  public void navigationEnabled(boolean enabled) {
    next.setVisible(enabled);
    previous.setVisible(enabled);
    DOM.setElementPropertyBoolean(variableNameSuggestBox.getElement(), "disabled", !enabled);
  }

  @Override
  public HasText getName() {
    return variableName;
  }

  @Override
  public HandlerRegistration addNameChangedHandler(ChangeHandler changeHandler) {
    return variableName.addChangeHandler(changeHandler);
  }

  @Override
  public HandlerRegistration addValueTypeChangedHandler(ChangeHandler changeHandler) {
    return valueType.addChangeHandler(changeHandler);
  }

  @Override
  public HandlerRegistration addOccurrenceGroupChangedHandler(ChangeHandler changeHandler) {
    return occurenceGroup.addChangeHandler(changeHandler);
  }

  @Override
  public HandlerRegistration addUnitChangedHandler(ChangeHandler changeHandler) {
    return unit.addChangeHandler(changeHandler);
  }

  @Override
  public HandlerRegistration addMimeTypeChangedHandler(ChangeHandler changeHandler) {
    return mimeType.addChangeHandler(changeHandler);
  }

  @Override
  public void formEnable(boolean enabled) {
    navigationEnabled(enabled);
    saveChangesButton.setEnabled(enabled);

    variableDetailTabs.setVisible(enabled);

    variableName.setEnabled(enabled);
    removeButton.setEnabled(enabled);
    addButton.setEnabled(true); // Regardless of form state the add button is enabled.
    valueType.setEnabled(enabled);
    repeatableCheckbox.setEnabled(enabled);
    occurenceGroup.setEnabled(enabled);
    unit.setEnabled(enabled);
    mimeType.setEnabled(enabled);
  }

  @Override
  public void formClear() {
    variableName.setText("");
    repeatableCheckbox.setValue(false);
    occurenceGroup.setText("");
    occurenceGroup.setEnabled(false); // Occurrence group is only enabled when repeatableCheckbox is true.
    unit.setText("");
    mimeType.setText("");
  }

  @Override
  public void variableNameEnabled(boolean enabled) {
    variableName.setEnabled(enabled);

  }
}
