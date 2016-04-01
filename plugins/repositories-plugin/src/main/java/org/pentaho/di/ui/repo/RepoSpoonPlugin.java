package org.pentaho.di.ui.repo;

import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

@org.pentaho.di.ui.spoon.SpoonPlugin( id = "repositories-plugin", image = "" )
@SpoonPluginCategories( { "spoon" } )
public class RepoSpoonPlugin implements SpoonPluginInterface {

  //  private static final String RESOURCE_PATH = "org/pentaho/di/ui/repo/res";
  //  private static final String OVERLAY_FILE_PATH =  RESOURCE_PATH + "/spoon_overlay.xul";
  //  private static final String SPOON_CATEGORY = "spoon";

  //  private Log logger = LogFactory.getLog( this.getClass() );
  //  private XulDomContainer container;
  private RepoMenuHandler menuHandler = new RepoMenuHandler();


  @Override
  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    //  if ( category.equals( SPOON_CATEGORY ) ) {
    //    this.container = container;
    //    container.registerClassLoader( getClass().getClassLoader() );
    //    container.loadOverlay( OVERLAY_FILE_PATH );
    //    container.addEventHandler( menuHandler );
    //
    //    // refresh menus
    //    Spoon.getInstance().enableMenus();
    //  }
  }

  

  @Override
  public SpoonLifecycleListener getLifecycleListener() {
    return new SpoonLifecycleListener() {
      public void onEvent( SpoonLifeCycleEvent evt ) {
        if ( evt.equals( SpoonLifeCycleEvent.STARTUP ) ) {
          Spoon.getInstance().setRepositoriesExtension( menuHandler );
        }
      }
    };
  }

  @Override
  public SpoonPerspective getPerspective() {
    // no perspective
    return null;
  }

  // destroy-method in blueprint xml
  public void removeFromContainer() throws XulException {
    Spoon.getInstance().setRepositoriesExtension( null );
    //  if ( container == null ) {
    //    return;
    //  }
    //  final Spoon spoon = Spoon.getInstance();
    //  final String menuHandlerName = menuHandler.getName();
    //  container.removeOverlay( OVERLAY_FILE_PATH );
    //  container.getEventHandlers().remove( menuHandlerName );
    //  container.deRegisterClassLoader( RepoSpoonPlugin.class.getClassLoader() );
    //  // refresh menus
    //  spoon.enableMenus();
  }
}
