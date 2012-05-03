/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class DeriveFromVariablePresenter extends DerivationPresenter<DeriveFromVariablePresenter.Display> {

  private Map<String, DatasourceDto> datasourceByName = new HashMap<String, DatasourceDto>();
  private Map<String, VariableDto> variablesByName = new HashMap<String, VariableDto>();
  private WizardType wizardType;
  private TableDto table;

  private String preSelectedDatasource;
  private String preSelectedTable;
  private String preSelectedVariable;

  private final Collection<String> restrictedDatasources = new HashSet<String>();
  private final Collection<String> restrictedTables = new HashSet<String>();

  @Inject
  public DeriveFromVariablePresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  public void generateDerivedVariable() {
  }

  @Override
  void setTable(TableDto table) {
    this.table = table;
  }

  @Override
  public void onReveal() {
    if(wizardType == DeriveVariablePresenter.FromWizardType) {
      findDerivedFromVariable();
    }
  }

  @Override
  protected void onBind() {
    addChangeHandlers();
  }

  private void findDerivedFromVariable() {
    String derivedFromUri = VariableDtos.getDerivedFrom(getDerivedVariable());
    if(derivedFromUri == null) {
      initDatasources();
    } else {
      ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(derivedFromUri).get()
          .withCallback(new ResourceCallback<VariableDto>() {
            @Override
            public void onResource(Response response, VariableDto derivedFromVariable) {
              preSelectedVariable = derivedFromVariable.getName();

              ResourceRequestBuilderFactory.<TableDto>newBuilder()
                  .forResource(derivedFromVariable.getParentLink().getLink()).get()
                  .withCallback(new ResourceCallback<TableDto>() {
                    @Override
                    public void onResource(Response response, TableDto tableDto) {
                      preSelectedTable = tableDto.getName();
                      preSelectedDatasource = tableDto.getDatasourceName();
                      initDatasources();
                    }
                  }).send();

            }
          }).send();
    }
  }

  private void initDatasources() {

    ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(table.getViewLink()).get()
        .withCallback(new ResourceCallback<ViewDto>() {
          @Override
          public void onResource(Response response, ViewDto view) {
            for(String table : JsArrays.toIterable(view.getFromArray())) {
              String[] parts = table.split("\\.");
              restrictedDatasources.add(parts[0]);
              restrictedTables.add(parts[1]);
            }

            ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
                .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
                  @Override
                  public void onResource(Response response, JsArray<DatasourceDto> resource) {
                    datasourceByName.clear();
                    if(resource != null) {
                      for(int i = 0; i < resource.length(); i++) {
                        DatasourceDto datasourceDto = resource.get(i);
                        if(restrictedDatasources.contains(datasourceDto.getName())) {
                          datasourceByName.put(datasourceDto.getName(), datasourceDto);
                        }
                      }
                    }
                    List<String> datasources = new ArrayList<String>(datasourceByName.keySet());
                    Collections.sort(datasources);
                    getView().setDatasources(datasources, preSelectedDatasource);
                    loadTables();
                  }
                }).send();

          }
        }).send();

  }

  private void loadTables() {
    List<String> tables = new ArrayList<String>();
    DatasourceDto datasource = datasourceByName.get(getView().getSelectedDatasource());
    if(datasource != null) {
      for(int i = 0; i < datasource.getTableArray().length(); i++) {
        String name = datasource.getTableArray().get(i);
        if(restrictedTables.contains(name)) {
          tables.add(name);
        }
      }
      Collections.sort(tables);
    }
    getView().setTables(tables, preSelectedTable);
    loadVariables();
  }

  private void loadVariables() {
    UriBuilder uriBuilder = UriBuilder.create()
        .segment("datasource", getView().getSelectedDatasource(), "table", getView().getSelectedTable(), "variables");
    ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
          @Override
          public void onResource(Response response, JsArray<VariableDto> resource) {
            getView().setVariables(resource, preSelectedVariable);
            variablesByName.clear();
            if(resource != null) {
              for(int i = 0; i < resource.length(); i++) {
                VariableDto variableDto = resource.get(i);
                variablesByName.put(variableDto.getName(), variableDto);
              }
            }
          }
        }).send();
  }

  @Override
  List<DefaultWizardStepController.Builder> getWizardStepBuilders(WizardStepController.StepInHandler stepInHandler) {
    List<DefaultWizardStepController.Builder> stepBuilders = new ArrayList<DefaultWizardStepController.Builder>();
    stepBuilders.add(getView().getDeriveFromVariableStepController(wizardType != DeriveVariablePresenter.FromWizardType)
        .onValidate(new ValidationHandler() {
          @Override
          public boolean validate() {
            if(wizardType == DeriveVariablePresenter.FromWizardType && getView().getSelectedVariable() == null) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error("VariableSelectionIsRequired").build());
              return false;
            }
            return true;
          }
        }).onStepOut(new DeriveFromVariableStepOutHandler()));
    return stepBuilders;
  }

  private class DeriveFromVariableStepOutHandler implements WizardStepController.StepOutHandler {

    @Override
    public void onStepOut() {
      if(wizardType == DeriveVariablePresenter.FromWizardType) {
        setOriginalVariable(variablesByName.get(getView().getSelectedVariable()));
        VariableDtos.setDerivedFrom(getDerivedVariable(), getOriginalVariable());
      }
    }
  }

  private void addChangeHandlers() {
    getView().getDatasourceList().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        loadTables();
      }
    });
    getView().getTableList().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        loadVariables();
      }
    });
  }

  public void setWizardType(WizardType wizardType) {
    this.wizardType = wizardType;
  }

  public interface Display extends View {

    BranchingWizardStepController.Builder getDeriveFromVariableStepController(boolean skip);

    /**
     * Set a collection of datasources retrieved from Opal.
     */
    void setDatasources(List<String> datasources, @Nullable String selectedDatasource);

    /**
     * Get the datasource selected by the user.
     */
    String getSelectedDatasource();

    HasChangeHandlers getDatasourceList();

    /**
     * Get the table selected by the user.
     */
    String getSelectedTable();

    HasChangeHandlers getTableList();

    /**
     * Get the variable selected by the user.
     */
    String getSelectedVariable();

    void setVariables(JsArray<VariableDto> variables, @Nullable String selectedVariable);

    void setTables(List<String> tables, @Nullable String selectedTable);

  }

}
