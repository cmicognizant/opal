/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.ace.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Ace editor
 *
 * @see http://ace.ajax.org
 */
public class AceEditor extends SimplePanel implements HasText, HasEnabled, HasChangeHandlers {

  @SuppressWarnings({ "FieldCanBeLocal", "UnusedDeclaration" })
  private final JavaScriptObject editor;

  /**
   * Create editor, mode defaults to javascript.
   */
  public AceEditor() {
    editor = createEditor(getElement());
    // Hack to avoid fire ChangeEvent on Ace change event because Ace API fire change events on SetValue() and for all internal changes.
    // So we fire ChangeEvent on key down
    addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), AceEditor.this);
      }
    });
  }

  private static native JavaScriptObject createEditor(Element element) /*-{
      var editor = $wnd.ace.edit(element);
      editor.setTheme("ace/theme/textmate");
      editor.getSession().setMode("ace/mode/javascript");
      editor.getSession().setTabSize(2);
      return editor;
  }-*/;

  public final native void setMode(String mode) /*-{
      var editor = this.@org.obiba.opal.web.gwt.ace.client.AceEditor::editor;
      editor.getSession().setMode("ace/mode/" + mode);
      editor.renderer.setShowGutter(mode == "javascript");
  }-*/;

  @Override
  public final native String getText() /*-{
      return this.@org.obiba.opal.web.gwt.ace.client.AceEditor::editor.getValue();
  }-*/;

  public final native String getBeautifiedText() /*-{
      var value = this.@org.obiba.opal.web.gwt.ace.client.AceEditor::editor.getValue();
      return $wnd.js_beautify(value, { 'indent_size': 2 });
  }-*/;

  @Override
  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

  private HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
    return addDomHandler(handler, KeyUpEvent.getType());
  }

  @Override
  public final native boolean isEnabled() /*-{
      return this.@org.obiba.opal.web.gwt.ace.client.AceEditor::editor.getReadOnly();
  }-*/;

  @Override
  public final native void setEnabled(boolean enabled) /*-{
      this.@org.obiba.opal.web.gwt.ace.client.AceEditor::editor.setReadOnly(!enabled);
  }-*/;

  @Override
  public native void setText(String value) /*-{
      var editor = this.@org.obiba.opal.web.gwt.ace.client.AceEditor::editor;
      editor.setValue(value);
      editor.clearSelection();
      editor.gotoLine(0);
  }-*/;

  /**
   * Get selected text
   */
  public final native String getSelectedText() /*-{
      var editor = this.@org.obiba.opal.web.gwt.ace.client.AceEditor::editor;
      return editor.getSession().getTextRange(editor.getSelectionRange());
  }-*/;

  /**
   * Register a handler for change events generated by the editor.
   *
   * @param callback the change event handler
   */
  public final native void addAceEditorOnChangeHandler(AceEditorCallback callback) /*-{
      this.@org.obiba.opal.web.gwt.ace.client.AceEditor::editor.getSession().on("change", function (e) {
//          console.log(e);
          callback.@org.obiba.opal.web.gwt.ace.client.AceEditorCallback::invokeAceCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(e);
      });
  }-*/;

}
