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
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeDelegate;
import org.pentaho.di.ui.spoon.job.JobGraph;

public class SpoonJobEntryTreePanel extends SpoonTransJobTreePanel {

  public SpoonJobEntryTreePanel( Composite parent ) {
    //TODO
    super( parent, BaseMessages.getString( PKG, "Spoon.Entries" ),
        JobEntryPluginType.class, GUIResource.getInstance().getImagesJobentries() );
    setSupplier( new SpoonTreePanel.TreeSupplier() {
      @Override
      public void fillTree( Tree tree, String filter ) {
        refreshJobEntryTree( tree, filter );
      }
    } );
    setShowTooltips( true );
    addDoubleClick( getTree() );
  }

  /**
   * @see SpoonTreeDelegate#getTreeObjects(Tree, Tree, Tree)
   */
  @Override
  public TreeSelection[] getTreeObjects() {
    // TODO: get data obj
    TreeItem[] selection = getTree().getSelection();
    List<TreeSelection> objects = new ArrayList<TreeSelection>( selection.length );
    for ( TreeItem treeItem : selection ) {
      String[] path = ConstUI.getTreeStrings( treeItem );
      // path = category/step for leaf nodes
      if ( path.length == 2 ) {
        PluginRegistry registry = PluginRegistry.getInstance();
        Class<? extends PluginTypeInterface> pluginType = JobEntryPluginType.class;
        PluginInterface plugin = registry.findPluginWithName( pluginType, path[1] );

        // Retry for Start
        //
        if ( plugin == null ) {
          if ( path[1].equals( JobMeta.STRING_SPECIAL_START ) ) {
            plugin = registry.findPluginWithId( pluginType, JobMeta.STRING_SPECIAL );
          }
        }
        // Retry for Dummy
        //
        if ( plugin == null ) {
          if ( path[1].equals( JobMeta.STRING_SPECIAL_DUMMY ) ) {
            plugin = registry.findPluginWithId( pluginType, JobMeta.STRING_SPECIAL );
          }
        }

        if ( plugin != null ) {
          TreeSelection object = new TreeSelection( path[1], plugin );
          objects.add( object );
        }
      }
    }
    return objects.toArray( new TreeSelection[objects.size()] );
  }

  private void addDoubleClick( final Tree tree ) {
    tree.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
      }

      public void widgetDefaultSelected( SelectionEvent e ) {
        JobGraph jobGraph = getSpoon().getActiveJobGraph();
        if ( jobGraph != null ) {
          JobEntryTypeTreeItemInfo data = getSelectedItemData( tree );
          if ( data != null ) {
            jobGraph.addJobEntryToChain( data.getTarget().getName(), ( e.stateMask & SWT.SHIFT ) != 0 );
          }
        }
      }
    } );
  }

  // from Spoon
  private void refreshJobEntryTree( Tree jobEntryTree, String filter ) {
    PluginRegistry registry = PluginRegistry.getInstance();

    List<PluginInterface> baseJobEntries = registry.getPlugins( JobEntryPluginType.class );
    List<String> baseCategories = registry.getCategories( JobEntryPluginType.class );

    for ( String baseCategory : baseCategories ) {
      TreeItem item = new TreeItem( jobEntryTree, SWT.NONE );
      item.setText( baseCategory );
      item.setImage( GUIResource.getInstance().getImageFolder() );

      List<PluginInterface> sortedCat = getSortedPlugins( baseJobEntries, baseCategory );

      for ( PluginInterface jobEntryPlugin : sortedCat ) {
        if ( filterMatch( jobEntryPlugin ) ) {
          final Image jobEntryImage = getSmallImage( jobEntryPlugin );
          String pluginName = Const.NVL( jobEntryPlugin.getName(), "" );
          TreeItem pluginItem = createTreeItem( item, pluginName, jobEntryImage );

          JobEntryTypeTreeItemInfo data = new JobEntryTypeTreeItemInfo( jobEntryPlugin );
          pluginItem.setData( data );
        }
      }

      // grab general category for special entries
      if ( baseCategory.equalsIgnoreCase( JobEntryPluginType.GENERAL_CATEGORY ) ) {
        addSpecialItems( item );
      }
    }
  }

  private void addSpecialItems( TreeItem generalItem ) {
    // First add a few "Special entries: Start, Dummy, OK, ERROR
    // We add these to the top of the base category, we don't care about
    // the sort order here.
    //
    JobEntryCopy startEntry = JobMeta.createStartEntry();
    JobEntryCopy dummyEntry = JobMeta.createDummyEntry();

    String[] specialText = new String[] { startEntry.getName(), dummyEntry.getName(), };
    String[] specialTooltip = new String[] { startEntry.getDescription(), dummyEntry.getDescription(), };
    Image[] specialImage =
      new Image[] {
        GUIResource.getInstance().getImageStartMedium(), GUIResource.getInstance().getImageDummyMedium() };

    int pos = 0;
    for ( int i = 0; i < specialText.length; i++ ) {
      if ( !filterMatch( specialText[i] ) && !filterMatch( specialTooltip[i] ) ) {
        continue;
      }

      TreeItem specialItem = new TreeItem( generalItem, SWT.NONE, pos );
      specialItem.setImage( specialImage[i] );
      specialItem.setText( specialText[i] );

      // coreJobToolTipMap.put( specialText[i], specialTooltip[i] );
      pos++;
    }
  }

  private Image getSmallImage( PluginInterface pi ) {
    return GUIResource.getInstance().getImagesJobentriesSmall().get( pi.getIds()[ 0 ] );
  }

  public static class JobEntryTypeTreeItemInfo extends PluginInterfaceTypeTreeItemInfo {

    public JobEntryTypeTreeItemInfo( PluginInterface plugin ) {
      super( plugin );
    }

    @Override
    public DragAndDropContainer getDragAndDropContainer() {
      return new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_JOB_ENTRY, getTarget().getName() );
    }

    @Override
    protected Image getTooltipImage( Display display, PluginInterface plugin ) {
      Image image =
          GUIResource.getInstance().getImagesJobentries().get( plugin.getIds()[0] ).getAsBitmapForSize(
              display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
      return image;
    }
  }
}
