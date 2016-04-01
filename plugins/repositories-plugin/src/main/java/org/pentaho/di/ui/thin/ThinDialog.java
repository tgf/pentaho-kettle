package org.pentaho.di.ui.thin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;


/**
 * Created by bmorrise on 2/18/16.
 */
public class ThinDialog extends Dialog {

  protected Shell parent;
  protected int width;
  protected int height;
  protected String title;
  protected String url;
  protected Browser browser;
  protected Shell dialog;
  protected Display display;

  public ThinDialog( Shell shell, int width, int height, String title, String url ) {
    super( shell );

    this.width = width;
    this.height = height;
    this.title = title;
    this.url = url;
  }

  public void createDialog() {

    Shell parent = getParent();
    display = parent.getDisplay();

    dialog = new Shell( parent );
    dialog.setText( title );
    dialog.setSize( width, height );
    dialog.setLayout( new FillLayout() );

    try {
      browser = new Browser( dialog, SWT.NONE );
      browser.setUrl( url );
    } catch ( Exception e ) {
      MessageBox messageBox = new MessageBox( dialog, SWT.ICON_ERROR | SWT.OK );
      messageBox.setMessage( "Browser cannot be initialized." );
      messageBox.setText( "Exit" );
      messageBox.open();
      //  System.exit( -1 );
    }

    dialog.open();
  }
}
