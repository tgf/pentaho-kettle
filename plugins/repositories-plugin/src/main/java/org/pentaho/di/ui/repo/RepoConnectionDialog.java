package org.pentaho.di.ui.repo;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.ui.repository.IRepoConnectionCallback;
import org.pentaho.di.ui.thin.ThinDialog;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

import java.util.HashMap;

/**
 * Created by bmorrise on 2/21/16.
 */
public class RepoConnectionDialog extends ThinDialog {

  private static final int WIDTH = 630;
  private static final int HEIGHT = 630;
  private static final String TITLE = "New Repository Connection";
  private static final String WEB_CLIENT_PATH =  "/repositories/web/index.html";
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";

  public RepoConnectionDialog( Shell shell ) {
    super( shell, WIDTH, HEIGHT, TITLE, getRepoURL() );
  }

  public void open( final IRepoConnectionCallback callback ) {
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

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }
  

  private static Integer getOsgiServicePort() {
    // if no service port is specified try getting it from
    ServerPort osgiServicePort = ServerPortRegistry.getPort( OSGI_SERVICE_PORT );
    if ( osgiServicePort != null ) {
      return osgiServicePort.getAssignedPort();
    }
    return null;
  }

  private static String getRepoURL() {
    return "http://localhost:" + getOsgiServicePort() + WEB_CLIENT_PATH;
  }
}
