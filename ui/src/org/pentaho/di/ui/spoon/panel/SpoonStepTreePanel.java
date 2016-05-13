package org.pentaho.di.ui.spoon.panel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectUsageCount;
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeDelegate;
import org.pentaho.di.ui.spoon.trans.TransGraph;

public class SpoonStepTreePanel extends SpoonTransJobTreePanel {

  private final static int HISTORY_ITEMS = 10;

  public SpoonStepTreePanel( Composite parent ) {
    //TODO
    super( parent, BaseMessages.getString( PKG, "Spoon.Steps" ),
        StepPluginType.class, GUIResource.getInstance().getImagesSteps() );
    setSupplier( new SpoonTreePanel.TreeSupplier() {
      @Override
      public void fillTree( Tree tree, String filter ) {
        refreshStepTree( tree, filter );
      }

    } );
    setShowTooltips( true );
    addDoubleClick( getTree() );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  protected StepTypeTreeItemInfo getItemData( TreeItem item ) {
    return super.getItemData( item );
  }


  private void addDoubleClick( final Tree tree ) {
    tree.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
      }

      public void widgetDefaultSelected( SelectionEvent e ) {
        TransGraph transGraph = getSpoon().getActiveTransGraph();
        if ( transGraph != null ) {
          StepTypeTreeItemInfo data = getSelectedItemData( tree );
          if ( data != null ) {
            transGraph.addStepToChain( data.getTarget(), ( e.stateMask & SWT.SHIFT ) != 0 );
          }
        }
      }
    } );
  }

  private void refreshStepTree( Tree stepsTree, String filter ) {
    PluginRegistry registry = PluginRegistry.getInstance();

    final List<PluginInterface> baseSteps = registry.getPlugins( StepPluginType.class );
    final List<String> baseCategories = registry.getCategories( StepPluginType.class );

    for ( String baseCategory : baseCategories ) {
      TreeItem item = new TreeItem( stepsTree, SWT.NONE );
      item.setText( baseCategory );
      item.setImage( GUIResource.getInstance().getImageFolder() );

      List<PluginInterface> sortedCat = getSortedPlugins( baseSteps, baseCategory );

      for ( PluginInterface stepPlugin : sortedCat ) {
        if ( filterMatch( stepPlugin ) ) {
          final Image stepImage = getSmallImage( stepPlugin );
          String pluginName = Const.NVL( stepPlugin.getName(), "" );
          TreeItem pluginItem = createTreeItem( item, pluginName, stepImage );
          // extra data
          StepTypeTreeItemInfo data = new StepTypeTreeItemInfo( stepPlugin );
          pluginItem.setData( data );
        }
      }
    }

    // Add History Items...
    TreeItem item = new TreeItem( stepsTree, SWT.NONE );
    item.setText( BaseMessages.getString( PKG, "Spoon.History" ) );
    item.setImage( GUIResource.getInstance().getImageFolder() );

    List<ObjectUsageCount> pluginHistory = props.getPluginHistory();
    // The top 10 at most, the rest is not interesting anyway
    for ( int i = 0; i < pluginHistory.size() && i < HISTORY_ITEMS; i++ ) {
      ObjectUsageCount usage = pluginHistory.get( i );
      PluginInterface stepPlugin =
        PluginRegistry.getInstance().findPluginWithId( StepPluginType.class, usage.getObjectName() );
      if ( stepPlugin != null && filterMatch( stepPlugin ) ) {
        final Image stepImage = getSmallImage( stepPlugin );
        String pluginName = Const.NVL( stepPlugin.getName(), "" );
        createTreeItem( item, pluginName, stepImage );
      }
    }
  }


  /**
   *
   * @see SpoonTreeDelegate#getTreeObjects(Tree, Tree, Tree)
   */
  // TODO: no longer needed if going fwd with TreeItemInfo (except legacy)
  @Override
  public TreeSelection[] getTreeObjects() {
    TreeItem[] selection = getTree().getSelection();
    List<TreeSelection> objects = new ArrayList<TreeSelection>( selection.length );
    for ( TreeItem treeItem : selection ) {
      String[] path = ConstUI.getTreeStrings( treeItem );
      // path = category/step for leaf nodes
      if ( path.length == 2 ) {
        TreeSelection object =
            new TreeSelection( path[1], PluginRegistry.getInstance().findPluginWithName(
              StepPluginType.class, path[1] ) );
        objects.add( object );
      }
    }
    return objects.toArray( new TreeSelection[objects.size()] );
  }


  private Image getSmallImage( PluginInterface pi ) {
    return GUIResource.getInstance().getImagesStepsSmall().get( pi.getIds()[ 0 ] );
  }


  /**
   * how this would look for the steps design tab (would refactor w job entry type)
   * drag data: plugin.getName()
   * parent tree would keep the ref to transGrah and use the step info
   */
  public static class StepTypeTreeItemInfo extends PluginInterfaceTypeTreeItemInfo {

    public StepTypeTreeItemInfo( PluginInterface plugin ) {
      super( plugin );
    }

    @Override
    public DragAndDropContainer getDragAndDropContainer() {
      return new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, getTarget().getName() );
    }

    protected Image getTooltipImage( Display display, PluginInterface plugin ) {
      Image image =
          GUIResource.getInstance().getImagesSteps().get( plugin.getIds()[0] ).getAsBitmapForSize(
              display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
      return image;
    }
  }
//  public static class StepTypeTreeItemInfo extends TreeItemInfo<PluginInterface> {
//
//    public StepTypeTreeItemInfo( PluginInterface plugin ) {
//      super( plugin );
//    }
//
//    @Override
//    public DragAndDropContainer getDragAndDropContainer() {
//      return new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, getTarget().getName() );
//    }
//
//    //TODO refact w job entry
//    @Override
//    public boolean showTooltip( DefaultToolTip toolTip, TreeItem item, MouseEvent move ) {
//      PluginInterface plugin = getTarget();
//      Image image = getTooltipImage( item.getDisplay(), plugin );
//      toolTip.setImage( image );
//      toolTip.setText( plugin.getName() + Const.CR + Const.CR + plugin.getDescription() );
//      toolTip.setBackgroundColor( GUIResource.getInstance().getColor( 255, 254, 225 ) );
//      toolTip.setForegroundColor( GUIResource.getInstance().getColor( 0, 0, 0 ) );
//      toolTip.show( new org.eclipse.swt.graphics.Point( move.x, move.y ) );
//      return true;
//    }
//
//    protected Image getTooltipImage( Display display, PluginInterface plugin ) {
//      Image image =
//          GUIResource.getInstance().getImagesSteps().get( plugin.getIds()[0] ).getAsBitmapForSize(
//              display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
//      return image;
//    }
//  }

//  /**
//   * alternative w lookup
//   */
//  private static class LookupStepTypeItemInfo extends LookupTreeItemInfo<PluginInterface> {
//
//    public LookupStepTypeItemInfo( String id ) {
//      super( id );
//    }
//
//    @Override
//    protected PluginInterface lookup( String id ) {
//      return PluginRegistry.getInstance().findPluginWithName(
//          StepPluginType.class, id );
//    }
//
//    @Override
//    public DragAndDropContainer getDragAndDropContainer() {
//      return new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, getTarget().getName() );
//    }
//
//    //TODO refact w job entry
//    @Override
//    public boolean showTooltip( DefaultToolTip toolTip, TreeItem item, MouseEvent move ) {
//      PluginInterface plugin = getTarget();
//      Image image = getTooltipImage( item.getDisplay(), plugin );
//      toolTip.setImage( image );
//      toolTip.setText( plugin.getName() + Const.CR + Const.CR + plugin.getDescription() );
//      toolTip.setBackgroundColor( GUIResource.getInstance().getColor( 255, 254, 225 ) );
//      toolTip.setForegroundColor( GUIResource.getInstance().getColor( 0, 0, 0 ) );
//      // TODO: we're setting a shift/offset to the tooltip on creation, why also displace here?
//      // toolTip.show( new org.eclipse.swt.graphics.Point( move.x + 10, move.y + 10 ) );
//      toolTip.show( new org.eclipse.swt.graphics.Point( move.x, move.y ) );
//      return true;
//    }
//
//    protected Image getTooltipImage( Display display, PluginInterface plugin ) {
//      Image image =
//          GUIResource.getInstance().getImagesSteps().get( plugin.getIds()[0] ).getAsBitmapForSize(
//              display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
//      return image;
//    }
//  }
//
//  /**
//   * afterthought lookup version
//   */
//  private static class LookupStepTypeTreeItemInfo extends StepTypeTreeItemInfo {
//
//    private String pluginName;
//
//    public LookupStepTypeTreeItemInfo( PluginInterface plugin ) {
//      super( null );
//      pluginName = plugin.getName();
//    }
//
//    @Override
//    public PluginInterface getTarget() {
//      return PluginRegistry.getInstance().findPluginWithName(
//          StepPluginType.class, pluginName );
//    }
//  }
}
