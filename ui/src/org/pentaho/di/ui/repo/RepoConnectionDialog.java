package org.pentaho.di.ui.repo;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.thin.ThinDialog;

import java.util.HashMap;

/**
 * Created by bmorrise on 2/21/16.
 */
public class RepoConnectionDialog extends ThinDialog {

  public static int WIDTH = 630;
  public static int HEIGHT = 630;
  public static String TITLE = "New Repository Connection";
  public static String URL = "http://localhost:8000";

  public RepoConnectionDialog( Shell shell ) {
    super( shell, WIDTH, HEIGHT, TITLE, URL );
  }

  public void open( final RepoConnectionCallback callback ) {
    super.createDialog();

    new BrowserFunction( browser, "createPentahoRepo" ) {
      @Override public Object function( Object[] arguments ) {
        try {
          callback.invoke( new ObjectMapper().readValue((String) arguments[0], HashMap.class) );
        } catch ( Exception e ) {
          return false;
        }
        return true;
      }
    };

    new BrowserFunction( browser, "close" ) {
      @Override public Object function( Object[] arguments ) {
        dialog.dispose();
        return null;
      }
    };

    while ( !display.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }
}
