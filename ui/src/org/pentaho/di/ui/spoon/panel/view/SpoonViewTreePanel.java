package org.pentaho.di.ui.spoon.panel.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.TreeMemory;
import org.pentaho.di.ui.spoon.DatabasesCollector;
import org.pentaho.di.ui.spoon.SelectionTreeExtension;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.spoon.delegates.SpoonTabsDelegate;
import org.pentaho.di.ui.spoon.panel.SpoonTreePanel;
import org.pentaho.di.ui.spoon.panel.TreeItemInfo;
import org.pentaho.di.ui.spoon.panel.ViewTreePanelMenuHandler;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulMenupopup;

/**
 *
 */
public class SpoonViewTreePanel extends SpoonTreePanel {

  private String XUL_MENU_FILE = "ui/view-tree-menu.xul";
  //  private static String POPUP_MENU_REF = "POPUP_MENU";
  private XulDomContainer xulContainer;
  private ViewTreePanelMenuHandler menuHandler = new ViewTreePanelMenuHandler();

  private SpoonTabsDelegate tabDelegate;

  public SpoonViewTreePanel( Composite parent, SpoonTabsDelegate tabDelegate ) {
    super( parent, BaseMessages.getString( PKG, "Spoon.Explorer" ) );
    init( parent );
    this.tabDelegate = tabDelegate;
    final Tree tree = getTree();
    tree.addMenuDetectListener( new MenuDetectListener() {
      public void menuDetected( MenuDetectEvent e ) {
        showMenu( tree );
      }
    } );
  }

  private void init( Composite parent ) {
    try {
      //TODO
      KettleXulLoader xulLoader = new KettleXulLoader();
      xulLoader.setIconsSize( ConstUI.SMALL_ICON_SIZE, ConstUI.SMALL_ICON_SIZE );
      xulLoader.setOuterContext( parent.getShell() );
      xulLoader.setSettingsManager( XulSpoonSettingsManager.getInstance() );
      xulContainer = xulLoader.loadXul( XUL_MENU_FILE, new XulSpoonResourceBundle() );
      // TODO new event handler
      xulContainer.addEventHandler( Spoon.getInstance() );
      xulContainer.addEventHandler( menuHandler );
      // TODO check where are all these logs going
    } catch ( IllegalArgumentException e ) {
      getLog().logError( e.getLocalizedMessage(), e );
    } catch ( XulException e ) {
      getLog().logError( e.getLocalizedMessage(), e );
    }
  }

//  @Override
//  public void setTreeDelegate( SpoonTreeDelegate treeDelegate ) {
//    super.setTreeDelegate( treeDelegate );
//    
//  }

  //TODO: temp visibility
  public Tree getTree() {
    return super.getTree();
  }

  @Override
  public TreeSelection[] getTreeObjects() {
    //  return getTreeDelegate().getTreeObjects( getTree(), getTree(), null );
    TreeItem[] selection = getTree().getSelection();
    List<TreeSelection> objects = new ArrayList<TreeSelection>( selection.length );
    for ( TreeItem item : selection ) {
      TreeSelection object = getLegacySelection( item );
      if ( object != null ) {
        objects.add( object );
      }
    }
    return objects.toArray( new TreeSelection[objects.size()] );
  }

  private synchronized void showMenu( Tree tree ) {
    TreeItem item = getSelectedItem( tree );
    TreeItemInfo<?> itemInfo = getItemData( item );
    if ( itemInfo != null ) {
      Menu popup = itemInfo.getContextMenu( tree );
      if ( popup != null ) {
        // TODO: when do we need parent?
        // TODO: remove handler/selectionObject and move to base
        menuHandler.setSelectionObject( itemInfo.getTarget() );
        menuHandler.setSelectionObjectParent( getParentData( item ) );
        ConstUI.displayMenu( popup, tree );
      } else {
        tree.setMenu( null );
      }
    }
    createPopUpMenuExtension( tree );
  }

  private Object getParentData( TreeItem item ) { //TODO: add parent data ref directly to data map?
    TreeItem parent = item.getParentItem();
    while ( parent != null ) {
      TreeItemInfo<?> data = getItemData( item );
      if( data != null ) {
        return data.getTarget();
      }
      parent = parent.getParentItem();
    }
    return null;
  }

  private TreeSelection getLegacySelection( TreeItem item ) {
    TreeItemInfo<?> info = getItemData( item );
    if ( info == null ) {
      return null;
    }
    Object[] ancestry = new Object[2];
    int i = 0;
    for ( TreeItem parent = item.getParentItem(); parent != null; parent = parent.getParentItem() ) {
      TreeItemInfo<?> parentInfo = getItemData( parent );
      if ( parentInfo != null ) {
        ancestry[i++] = parentInfo.getTarget();
      }
    }
    return new TreeSelection( item.getText(), info.getTarget(), ancestry[0], ancestry[1] );
  }

  // TODO:
  private void createPopUpMenuExtension( Tree selectionTree ) {
    try {
      ExtensionPointHandler.callExtensionPoint( getLog(), KettleExtensionPoint.SpoonPopupMenuExtension.id, selectionTree );
    } catch ( Exception e ) {
      // TODO msg?
      getLog().logError( "Error handling menu right click on job entry through extension point", e );
    }
  }
  //TODO: temp testing
  public void refreshSelectionTree( TransMeta activeTransMeta, boolean showAll ) {
    Tree selectionTree = getTree();
    clearTree( selectionTree );
    String filter = getToolbar().getSelectionFilter();

    TreeItem tiTrans = new TreeItem( selectionTree, SWT.NONE );
    tiTrans.setText( Spoon.STRING_TRANSFORMATIONS );
    tiTrans.setImage( GUIResource.getInstance().getImageFolder() );

    // Set expanded if this is the only transformation shown.
    if ( !showAll ) {
      TreeMemory.getInstance().storeExpanded( Spoon.STRING_SPOON_MAIN_TREE, tiTrans, true );
    }

    GUIResource guiResource = GUIResource.getInstance();
    for ( TabMapEntry entry : tabDelegate.getTabs() ) {
      Object managedObject = entry.getObject().getManagedObject();
      if ( managedObject instanceof TransMeta ) {
        TransMeta transMeta = (TransMeta) managedObject;

        if ( showAll || ( activeTransMeta != null && activeTransMeta.equals( transMeta ) ) ) {

          // Add a tree item with the name of transformation
          //
          String name = tabDelegate.makeTabName( transMeta, entry.isShowingLocation() );
          if ( Utils.isEmpty( name ) ) {
            name = Spoon.STRING_TRANS_NO_NAME;
          }

          TreeItem tiTransName = createTreeItem( tiTrans, name, guiResource.getImageTransTree() );

          // Set expanded if this is the only transformation
          // shown.
          if ( props.isOnlyActiveFileShownInTree() ) {
            TreeMemory.getInstance().storeExpanded( Spoon.STRING_SPOON_MAIN_TREE, tiTransName, true );
          }

          SubTreeSupplier dbConnections = new ConnectionsSubTreeSupplier( transMeta, transMeta.getRepository() );
          dbConnections.fillSubTree( tiTransName, filter );
//            refreshDbConnectionsSubtree( tiTransName, transMeta, guiResource );
//
          StepInstancesSupplier steps = new StepInstancesSupplier( transMeta );
          steps.setXulDomContainer( xulContainer );
          steps.fillSubTree( tiTransName, filter );

          HopsSubtreeSupplier hops = new HopsSubtreeSupplier( transMeta );
          hops.fillSubTree( tiTransName, filter );

//
//            refreshPartitionsSubtree( tiTransName, transMeta, guiResource );
//
//            refreshSlavesSubtree( tiTransName, transMeta, guiResource );
//
//            refreshClustersSubtree( tiTransName, transMeta, guiResource );
//
            refreshSelectionTreeExtension( tiTransName, transMeta, guiResource );

        }
      }
    }
  }

  void refreshSelectionTreeExtension( TreeItem tiRootName, AbstractMeta meta, GUIResource guiResource ) {
    try {
      ExtensionPointHandler.callExtensionPoint( getLog(), KettleExtensionPoint.SpoonViewTreeExtension.id,
          new SelectionTreeExtension( tiRootName, meta, guiResource, Spoon.REFRESH_SELECTION_EXTENSION ) );
    } catch ( Exception e ) {
      getLog().logError( "Error handling menu right click on job entry through extension point", e );
    }
  }

  // TODO: decent class names
  protected abstract static class SubTreeSupplier {
    protected static Class<?> PKG = Spoon.class;

    protected static GUIResource guiResource = GUIResource.getInstance();
    protected XulDomContainer xulDomContainer;

    public abstract void fillSubTree( TreeItem root, String filter );

    protected TreeItem createTreeItem( TreeItem parent, String text, Image image ) {
      TreeItem item = new TreeItem( parent, SWT.NONE );
      item.setText( text );
      item.setImage( image );
      return item;
    }

    public XulDomContainer getXulDomContainer() {
      return xulDomContainer;
    }

    public void setXulDomContainer( XulDomContainer xulDomContainer ) {
      this.xulDomContainer = xulDomContainer;
    }

    protected TreeItem createFolder( TreeItem parent, String text ) {
      return createTreeItem( parent, text,  guiResource.getImageFolder() );
    }

    protected Menu getItemMenu( String id, Tree tree ) {
      if ( xulDomContainer != null ) {
        XulMenupopup xulMenu = (XulMenupopup) xulDomContainer.getDocumentRoot().getElementById( id );
        if ( xulMenu != null ) {
          MenuManager menuMgr = (MenuManager) xulMenu.getManagedObject();
          Menu menu = menuMgr.createContextMenu( tree );
          menuMgr.updateAll( true );
          return menu;
        }
      }
      return null;
    }

    protected Spoon getSpoon() {
      return Spoon.getInstance();
    }
  }

  private static class StepInstancesSupplier extends SubTreeSupplier {
    private static String POPUP_ID = "step-inst";
    private TransMeta meta;
    
    public StepInstancesSupplier( TransMeta transMeta ) {
      meta = transMeta;
    }

    @Override
    public void fillSubTree( TreeItem root, String filter ) {
      // TODO: should folders be outside?
      TreeItem tiStepTitle = createTreeItem( root, Spoon.STRING_STEPS, guiResource.getImageFolder() );
      tiStepTitle.setData( new TreeItemInfo<Class<StepMeta>>( StepMeta.class ) );
      Menu popupMenu = getItemMenu( POPUP_ID, root.getParent() );

      // Put the steps below it.
      for ( int i = 0; i < meta.nrSteps(); i++ ) {
        StepMeta stepMeta = meta.getStep( i );
        if ( stepMeta.isMissing() ) {
          continue;
        }
        PluginInterface stepPlugin =
          PluginRegistry.getInstance().findPluginWithId( StepPluginType.class, stepMeta.getStepID() );

        // TODO: 
        if ( !filterMatch( stepMeta.getName(), filter ) ) {
          continue;
        }

        Image stepIcon = guiResource.getImagesStepsSmall().get( stepPlugin.getIds()[ 0 ] );
        if ( stepIcon == null ) {
          stepIcon = guiResource.getImageFolder();
        }

        TreeItem tiStep = createTreeItem( tiStepTitle, stepMeta.getName(), stepIcon );

        if ( stepMeta.isShared() ) {
          tiStep.setFont( guiResource.getFontBold() );
        }
        if ( !stepMeta.isDrawn() ) {
          tiStep.setForeground( guiResource.getColorDarkGray() );
        }

        // extra stuff
        tiStep.setData( new StepMetaItemInfo( stepMeta, popupMenu ) );
      }
    }

    private static class StepMetaItemInfo extends TreeItemInfo<StepMeta> {
      private Menu menu;
  
      public StepMetaItemInfo( StepMeta object, Menu menu ) {
        super( object );
        this.menu = menu;
      }
  
      @Override
      public DragAndDropContainer getDragAndDropContainer() {
        // TODO allows drag&drop but apparently can't do anything with it
        return new DragAndDropContainer( DragAndDropContainer.TYPE_STEP, getTarget().getName() );
      }
  
      @Override
      public Menu getContextMenu( Tree tree ) {
        return menu;
      }
    }
  }

  private static class HopsSubtreeSupplier extends SubTreeSupplier {
    private TransMeta transMeta;

    public HopsSubtreeSupplier( TransMeta meta ) {
      this.transMeta = meta;
    }

    @Override
    public void fillSubTree( TreeItem root, String filter ) {
      TreeItem tiHopTitle = createTreeItem( root, Spoon.STRING_HOPS, guiResource.getImageFolder() );
      tiHopTitle.setData( new TreeItemInfo<TransMeta>( transMeta ) {
        @Override
        public Menu getContextMenu( Tree tree ) {
          Menu menu = createMenu( tree );
          createMenuItem( menu, PKG, "Spoon.Menu.Popup.BASE.New", new SelectionAdapter() { 
            @Override
            public void widgetSelected( SelectionEvent e ) {
              getSpoon().newHop( getTarget() );
            }
          } );
          createMenuItem( menu, PKG, "Spoon.Menu.Popup.HOPS.SortHops", new SelectionAdapter() { 
            @Override
            public void widgetSelected( SelectionEvent e ) {
              getTarget().sortHops();
            }
          } );
          return menu;
        }
      });

      for ( int i = 0; i < transMeta.nrTransHops(); i++ ) {
        TransHopMeta hopMeta = transMeta.getTransHop( i );

        if ( !filterMatch( hopMeta.toString(), filter ) ) {
          continue;
        }

        Image icon = hopMeta.isEnabled() ? guiResource.getImageHopTree() : guiResource.getImageDisabledHopTree();
        TreeItem item = createTreeItem( tiHopTitle, hopMeta.toString(), icon );
        item.setData( new TreeItemInfo<TransHopMeta>( hopMeta ) {
          @Override
          public Menu getContextMenu( Tree tree ) {
            Menu menu = createMenu( tree );
            createMenuItem( menu, PKG, "Spoon.Menu.Popup.STEPS.Edit", new SelectionAdapter() { 
              @Override
              public void widgetSelected( SelectionEvent e ) {
                getSpoon().editHop( transMeta, getTarget() );
              }
            } );
            createMenuItem( menu, PKG, "Spoon.Menu.Popup.STEPS.Delete", new SelectionAdapter() { 
              @Override
              public void widgetSelected( SelectionEvent e ) {
                getSpoon().delHop( transMeta, getTarget() );
              }
            } );
            return menu;
          }
        });
      }
    }
  }

  //  //TODO: Legacy!!
  //  private void executeSpoonTreeDelegateExtensionPoint( TreeItem item ) {
  //    TreeItemInfo<?> info = getItemData( item );
  //    if ( info != null ) {
  //      // TODO: only do this if that extension point is being used
  //      String[] path = ConstUI.getTreeStrings( item );
  //      if ( path.length >= 3 ) {
  //        AbstractMeta meta = getUpstreamData( item, AbstractMeta.class );
  //        executeExtensionPoint( new SpoonTreeDelegateExtension( meta, path, path.length, new ArrayList<>( 0 ) ) );
  //      }
  //    }
  //  }
  //  @SuppressWarnings( "unchecked" )
  //  private <T> T getUpstreamData( TreeItem item, Class<T> clazz ) {
  //    for ( TreeItem parent = item.getParentItem(); parent != null; parent = parent.getParentItem() ) {
  //      TreeItemInfo<?> info =  getItemData( item );
  //      if ( info != null && clazz.isAssignableFrom( info.getTarget().getClass() ) ) {
  //        return (T) info.getTarget();
  //      }
  //    }
  //    return null;
  //  }
  //  private void executeExtensionPoint( SpoonTreeDelegateExtension extension ) {
  //    try {
  //      ExtensionPointHandler
  //          .callExtensionPoint( getLog(), KettleExtensionPoint.SpoonTreeDelegateExtension.id, extension );
  //    } catch ( Exception e ) {
  //      getLog().logError( "Error handling SpoonTreeDelegate through extension point", e );
  //    }
  //  }



}
