package org.pentaho.di.ui.spoon.trans;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TransPreviewExtension {

  private Composite previewTab;
  private Control previewToolbar;
  private Composite preview;

  /**
   * 
   * @param previewTab
   * @param previewToolbar
   */
  public TransPreviewExtension( Composite previewTab, Control previewToolbar, Composite preview ) {
    this.preview = preview;
    this.previewTab = previewTab;
    this.previewToolbar = previewToolbar;
  }

  public Composite getPreviewTab() {
    return previewTab;
  }

  public Control getPreviewToolbar() {
    return previewToolbar;
  }

  public Composite getPreview() {
    return preview;
  }
}
