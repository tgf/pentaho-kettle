package org.pentaho.di.ui.spoon.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;

// TODO: need to add access to job/trans graph (may be nice excuse to refactor stuff to AbstractGraph btw)
public abstract class SpoonTransJobTreePanel extends SpoonTreePanel {
  private DefaultToolTip tooltip;
  private Map<String, String> tooltipMap = new HashMap<>();;
  Map<String, SwtUniversalImage> imageMap;
  Class<? extends PluginTypeInterface> pluginTypeClass;

  public SpoonTransJobTreePanel( Composite parent, String testLabel,
      Class<? extends PluginTypeInterface> pluginTypeClass,
      Map<String, SwtUniversalImage> imageMap ) {
    super( parent, testLabel );
    // TODO
    this.pluginTypeClass = pluginTypeClass;
    this.imageMap = imageMap;
  }

  @Override
  protected void buildUI( Composite parent ) {
    super.buildUI( parent );
    tooltip = new DefaultToolTip( getTree(), ToolTip.RECREATE, true );
    tooltip.setRespectMonitorBounds( true );
    tooltip.setRespectDisplayBounds( true );
    tooltip.setPopupDelay( 350 );
    tooltip.setHideDelay( 5000 );
    tooltip.setShift( new org.eclipse.swt.graphics.Point( ConstUI.TOOLTIP_OFFSET, ConstUI.TOOLTIP_OFFSET ) );

    getTree().addMouseMoveListener( new MouseMoveListener() {
      @Override
      public void mouseMove( MouseEvent e ) {
        if ( isShowTooltips() ) {
          TreeItem item = searchMouseOverTreeItem( getTree().getItems(), e.x, e.y );
          if ( item != null ) {
            showTooltip( tooltip, item, e );
          }
        }
      }
    } );
  }

  protected void showTooltip( DefaultToolTip tooltip, TreeItem item, MouseEvent e ) {
    showItemTooltip( tooltip, item.getText(), e, tooltipMap, imageMap, pluginTypeClass );
  }

  protected boolean showItemTooltip( DefaultToolTip toolTip, String name, MouseEvent move,
      Map<String, String> tooltipMap,
      Map<String, SwtUniversalImage> imageMap, Class<? extends PluginTypeInterface> clazz ) {
    String tip = tooltipMap.get( name );
    if ( tip != null ) {
      PluginInterface plugin = PluginRegistry.getInstance().findPluginWithName( clazz, name );
      if ( plugin != null ) {
        Image image =
            imageMap.get( plugin.getIds()[0] ).getAsBitmapForSize( getTree().getDisplay(),
                ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
        toolTip.setImage( image );
        toolTip.setText( name + Const.CR + Const.CR + tip );
        toolTip.setBackgroundColor( GUIResource.getInstance().getColor( 255, 254, 225 ) );
        toolTip.setForegroundColor( GUIResource.getInstance().getColor( 0, 0, 0 ) );
        toolTip.show( new org.eclipse.swt.graphics.Point( move.x + 10, move.y + 10 ) );
        return true;
      }
    }
    return false;
  }

  protected boolean filterMatch( PluginInterface pi ) {
    return filterMatch( pi.getName() ) || filterMatch( pi.getDescription() );
  }

  /**
   * get alphabetized list of steps/entries in category
   * @param baseSteps
   * @param baseCategory
   * @return
   */
  protected List<PluginInterface> getSortedPlugins( final List<PluginInterface> baseSteps, String baseCategory ) {
    List<PluginInterface> sortedCat = new ArrayList<PluginInterface>();
    for ( PluginInterface baseStep : baseSteps ) {
      if ( baseStep.getCategory().equalsIgnoreCase( baseCategory ) ) {
        sortedCat.add( baseStep );
      }
    }
    Collections.sort( sortedCat, new Comparator<PluginInterface>() {
      public int compare( PluginInterface p1, PluginInterface p2 ) {
        // TODO there's a jira for ignoring case here
        return p1.getName().compareToIgnoreCase( p2.getName() );
      }
    } );
    return sortedCat;
  }

  public abstract static class PluginInterfaceTypeTreeItemInfo extends TreeItemInfo<PluginInterface> {

    public PluginInterfaceTypeTreeItemInfo( PluginInterface plugin ) {
      super( plugin );
    }

//    @Override
//    public DragAndDropContainer getDragAndDropContainer() {
//      return new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, getTarget().getName() );
//    }

    //TODO refact w job entry
    @Override
    public boolean showTooltip( DefaultToolTip toolTip, TreeItem item, MouseEvent move ) {
      PluginInterface plugin = getTarget();
      Image image = getTooltipImage( item.getDisplay(), plugin );
      toolTip.setImage( image );
      toolTip.setText( plugin.getName() + Const.CR + Const.CR + plugin.getDescription() );
      toolTip.setBackgroundColor( GUIResource.getInstance().getColor( 255, 254, 225 ) );
      toolTip.setForegroundColor( GUIResource.getInstance().getColor( 0, 0, 0 ) );
      toolTip.show( new org.eclipse.swt.graphics.Point( move.x, move.y ) );
      return true;
    }

    abstract protected Image getTooltipImage( Display display, PluginInterface plugin ); 
//    {
//      Image image =
//          GUIResource.getInstance().getImagesSteps().get( plugin.getIds()[0] ).getAsBitmapForSize(
//              display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
//      return image;
//    }
  }
}
