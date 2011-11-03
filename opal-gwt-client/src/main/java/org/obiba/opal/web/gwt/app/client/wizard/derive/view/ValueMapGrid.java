/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.workbench.view.Grid;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.ListDataProvider;

public class ValueMapGrid extends FlowPanel {

  private static final int DEFAULT_PAGE_SIZE_MIN = 10;

  private static final int DEFAULT_PAGE_SIZE_MAX = 100;

  private SimplePager pager;

  private AbstractCellTable<ValueMapEntry> table;

  private ListDataProvider<ValueMapEntry> dataProvider;

  private List<ValueMapEntry> valueMapEntries;

  private int pageSizeMin = DEFAULT_PAGE_SIZE_MIN;

  private int pageSizeMax = DEFAULT_PAGE_SIZE_MAX;

  private String gridHeight = "30em";

  public ValueMapGrid() {
    super();
    this.pager = new SimplePager();
    this.pager.setPageSize(DEFAULT_PAGE_SIZE_MAX);
    this.pager.addStyleName("right-aligned");
    add(pager);
    this.table = null;
  }

  public void setPageSizeMin(int pageSizeMin) {
    this.pageSizeMin = pageSizeMin;
  }

  public void setPageSizeMax(int pageSizeMax) {
    this.pageSizeMax = pageSizeMax;
  }

  public void setGridHeight(String gridHeight) {
    this.gridHeight = gridHeight;
  }

  public void populate(List<ValueMapEntry> valueMapEntries) {
    this.valueMapEntries = valueMapEntries;

    initializeTable();

    dataProvider = new ListDataProvider<ValueMapEntry>(valueMapEntries);
    dataProvider.addDataDisplay(table);
    dataProvider.refresh();

    addStyleName("value-map");
  }

  private void initializeTable() {
    if(table != null) {
      remove(table);
      table = null;
    }

    if(valueMapEntries.size() > pageSizeMin) {
      table = new Grid<ValueMapEntry>(pager.getPageSize());
      table.setHeight(gridHeight);
    } else {
      table = new Table<ValueMapEntry>(pager.getPageSize());
    }
    table.addStyleName("clear");
    table.setPageSize(pageSizeMax);
    table.setWidth("100%");
    add(table);
    pager.setPageSize(pageSizeMax);
    pager.setDisplay(table);
    pager.setVisible((valueMapEntries.size() > pager.getPageSize()));

    initializeColumns();
  }

  private void initializeColumns() {
    // Value
    Column<ValueMapEntry, String> valueColumn = new Column<ValueMapEntry, String>(new TextCell(new TrustedHtmlRenderer())) {

      @Override
      public String getValue(ValueMapEntry entry) {
        switch(entry.getType()) {
        case CATEGORY_NAME:
          return "<span class='category'>" + entry.getValue() + "</span>";
        case DISTINCT_VALUE:
          return "<span class='distinct'>" + entry.getValue() + "</span>";
        case OTHER_VALUES:
          return "<span class='special others'>" + entry.getValue() + "</span>";
        case EMPTY_VALUES:
          return "<span class='special empties'>" + entry.getValue() + "</span>";
        default:
          return entry.getValue();
        }
      }
    };
    table.addColumn(valueColumn, "Value");

    // New Value
    Column<ValueMapEntry, String> newValueColumn = new Column<ValueMapEntry, String>(new TextInputCell()) {
      @Override
      public String getValue(ValueMapEntry entry) {
        return entry.getNewValue();
      }
    };
    table.addColumn(newValueColumn, "New Value");
    newValueColumn.setFieldUpdater(new FieldUpdater<ValueMapEntry, String>() {
      public void update(int index, ValueMapEntry entry, String value) {
        entry.setNewValue(value);
        debug();
      }
    });

    // Missing
    Column<ValueMapEntry, Boolean> missingColumn = new Column<ValueMapEntry, Boolean>(new CheckboxCell()) {

      @Override
      public Boolean getValue(ValueMapEntry entry) {
        return entry.isMissing();
      }
    };
    table.addColumn(missingColumn, "Missing");
    missingColumn.setFieldUpdater(new FieldUpdater<ValueMapEntry, Boolean>() {
      @Override
      public void update(int index, ValueMapEntry entry, Boolean value) {
        entry.setMissing(value);
        debug();

      }
    });
  }

  private void debug() {
    int i = 1;
    for(ValueMapEntry entry : valueMapEntries) {
      GWT.log((i++) + ": " + entry.getValue() + ", " + entry.getNewValue() + ", " + entry.isMissing());
    }
  }

  /**
   * Escape user string, not ours.
   */
  private final class TrustedHtmlRenderer extends AbstractSafeHtmlRenderer<String> {

    private final RegExp rg = RegExp.compile("(^<span class='[\\w\\s]+'>)(.*)(</span>$)");

    @Override
    public SafeHtml render(String object) {
      if(object == null) return SafeHtmlUtils.EMPTY_SAFE_HTML;

      MatchResult res = rg.exec(object);
      if(res.getGroupCount() == 4) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(SafeHtmlUtils.fromTrustedString(res.getGroup(1)));
        builder.appendEscaped(res.getGroup(2));
        builder.append(SafeHtmlUtils.fromTrustedString(res.getGroup(3)));
        return builder.toSafeHtml();
      } else {
        return SafeHtmlUtils.fromString(object);
      }

    }
  }
}
