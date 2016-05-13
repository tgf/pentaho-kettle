package org.pentaho.di.ui.spoon.panel.view;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.DatabasesCollector;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.panel.SpoonTreePanel;
import org.pentaho.di.ui.spoon.panel.TreeItemInfo;
import org.pentaho.di.ui.spoon.panel.view.SpoonViewTreePanel.SubTreeSupplier;

public class ConnectionsSubTreeSupplier extends SubTreeSupplier {
  private Image itemImage = guiResource.getImageConnectionTree();
  private DatabasesCollector collector;
  private HasDatabasesInterface context;

  public ConnectionsSubTreeSupplier( HasDatabasesInterface dbs, Repository repo ) {
    collector = new DatabasesCollector( dbs, repo );
    context = dbs;
  }

  @Override
  public void fillSubTree( TreeItem root, String filter ) {

    TreeItem tiDbTitle = createFolder( root, Spoon.STRING_CONNECTIONS );
    // for the legacy stuff selection and menu popup
    tiDbTitle.setData( new TreeItemInfo<HasDatabasesInterface>( context ) {
      @Override
      public Menu getContextMenu(Tree tree) {
        Menu menu = createMenu( tree );
        createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.New", new SelectionAdapter() {
          public void widgetSelected( SelectionEvent e ) {
            Spoon.getInstance().delegates.db.newConnection();
          }
        } );
        createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.NewConnectionWizard", new SelectionAdapter() {
          public void widgetSelected( SelectionEvent e ) {
            Spoon.getInstance().createDatabaseWizard();
          }
        } );
        createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.ClearDBCacheComplete", new SelectionAdapter() {
          public void widgetSelected( SelectionEvent e ) {
            Spoon.getInstance().delegates.db.clearDBCache( null );
          }
        } );
        return menu;
      };
    } );

    try {
      collector.collectDatabases();
    } catch ( KettleException e ) {
      new ErrorDialog( root.getParent().getShell(),
          BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
          BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingFromRepo.DbConnections" ), e );
      return;
    }
    for ( String dbName : collector.getDatabaseNames() ) {
      if ( !SpoonTreePanel.filterMatch( dbName, filter ) ) {
        continue;
      }
      DatabaseMeta databaseMeta = collector.getMetaFor( dbName );

      TreeItem tiDb = createTreeItem( tiDbTitle, databaseMeta.getDisplayName(), itemImage );

      tiDb.setData( new DbMetaItemInfo( databaseMeta, context ) );
      if ( databaseMeta.isShared() ) {
        tiDb.setFont( guiResource.getFontBold() );
      }
    }
  }


  private static class DbMetaItemInfo extends TreeItemInfo<DatabaseMeta> {

    private HasDatabasesInterface parentTarget;

    public DbMetaItemInfo( DatabaseMeta object, HasDatabasesInterface context ) {
      super( object );
      parentTarget = context; 
    }

    @Override
    public DragAndDropContainer getDragAndDropContainer() {
      DragAndDropContainer dnd =
          new DragAndDropContainer( DragAndDropContainer.TYPE_DATABASE_CONNECTION, getTarget().getName() );
      return dnd;
    }

    @Override
    public Menu getContextMenu( Tree tree ) {
      Menu menu = createMenu( tree );
      createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.New", new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          Spoon.getInstance().delegates.db.newConnection();
        }
      } );
      createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.Edit", new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          Spoon.getInstance().editConnection( getTarget() );
        }
      } );
      createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.Duplicate", new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          Spoon.getInstance().delegates.db.dupeConnection( parentTarget, getTarget() );
        }
      } );
      createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.CopyToClipboard", new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          Spoon.getInstance().delegates.db.clipConnection( getTarget() );
        }
      } );
      createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.Delete", new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          Spoon.getInstance().delConnection( parentTarget, getTarget() );
        }
      } );
      createSeparator( menu );
      createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.SQLEditor", new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          Spoon.getInstance().delegates.db.sqlConnection( getTarget() );
        }
      } );
      createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.ClearDBCache", new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          Spoon.getInstance().delegates.db.clearDBCache( getTarget() );
        }
      } );
      createMenuItem( menu, PKG,
          getTarget().isShared() ? "Spoon.Menu.Popup.CONNECTIONS.UnShare" : "Spoon.Menu.Popup.CONNECTIONS.Share",
          new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
              DatabaseMeta dbMeta = getTarget();
              Spoon spoon = Spoon.getInstance();
              if ( dbMeta.isShared() ) {
                spoon.unShareObject( dbMeta );
              } else {
                spoon.shareObject( dbMeta );
              }
            }
          } );
      createMenuItem( menu, PKG, "Spoon.Menu.Popup.CONNECTIONS.Explore", new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          Spoon.getInstance().delegates.db.exploreDB( getTarget(), true );
        }
      } );
      
      return menu;
    }
  }

}
