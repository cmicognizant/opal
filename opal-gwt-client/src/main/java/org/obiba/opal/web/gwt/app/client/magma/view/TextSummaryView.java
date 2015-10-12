/*******************************************************************************
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.AbstractTabPanel;
import org.obiba.opal.web.gwt.app.client.ui.SummaryFlexTable;
import org.obiba.opal.web.gwt.plot.client.FrequencyChartFactory;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.TextSummaryDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class TextSummaryView extends Composite {

    interface DefaultSummaryViewUiBinder extends UiBinder<Widget, TextSummaryView> {}

    private static final DefaultSummaryViewUiBinder uiBinder = GWT.create(DefaultSummaryViewUiBinder.class);

    private static final Translations translations = GWT.create(Translations.class);

    @UiField
    AbstractTabPanel chartsPanel;

    @UiField
    FlowPanel freqPanel;

    @UiField
    FlowPanel pctPanel;

    @UiField
    SummaryFlexTable stats;


    private FrequencyChartFactory chartFactory = null;



    public TextSummaryView(final String title,TextSummaryDto summaryDto, Collection<FrequencyDto> frequenciesNonMissing,
                           Collection<FrequencyDto> frequenciesMissing, double totalNonMissing, double totalMissing, double totalOther,
                           int maxResults) {

        initWidget(uiBinder.createAndBindUi(this));
        GWT.log("INSIDE TextSummaryView");
        chartsPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                GWT.log("event.getSelectedItem() "+event.getSelectedItem());
                GWT.log("chartFactory "+chartFactory);
                GWT.log("pctPanel.getWidgetCount()  "+pctPanel.getWidgetCount() );
                if(event.getSelectedItem() == 1 && chartFactory != null && pctPanel.getWidgetCount() == 0) {
                    GWT.log("TESTING TITLE");
                    pctPanel.add(chartFactory.createPercentageChart(title));
                }
            }
        });

        stats.clear();
        GWT.log("summaryDto.getFrequenciesArray()  "+summaryDto.getFrequenciesArray().length() );
        if(summaryDto.getFrequenciesArray() != null) {
            double total = totalNonMissing + totalMissing + totalOther;


            stats.drawHeader();
            stats.drawValuesFrequencies(frequenciesNonMissing,
                    TranslationsUtils.replaceArguments(translations.nonMissingTopN(), String.valueOf(maxResults)),
                    translations.notEmpty(), totalNonMissing + totalOther, totalOther, total);

            stats.drawValuesFrequencies(frequenciesMissing, translations.missingLabel(), translations.naLabel(), totalMissing,
                    total);
            stats.drawTotal(total);

            chartFactory = new FrequencyChartFactory();
            for(FrequencyDto frequency : JsArrays.toIterable(summaryDto.getFrequenciesArray())) {
                if(frequency.hasValue()) {
                    chartFactory.push(frequency.getValue(), frequency.getFreq(),
                            new BigDecimal(frequency.getPct() * 100).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
            }
            freqPanel.add(chartFactory.createValueChart(title));
        }
    }
}
