package org.pentaho.di.ui.spoon.panel;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.dnd.XMLTransfer;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeDelegate;

// TODO separate ui part as ..extends composite ?
// TODO: default key listeners and such
public abstract class SpoonTreePanel {

  private static int TOOLTIP_POPUP_DELAY = 350;
  private static int TOOLTIP_HIDE_DELAY = 5000;

  protected static Class<?> PKG = Spoon.class;
  protected static PropsUI props = PropsUI.getInstance();

  private SpoonTreePanelToolbar toolbar;
  
  //TODO TEMP
  private String testLabel;

  protected Composite panelComposite;//TODO keep in main?
  // TODO could use a TreeListener?
  private Tree objectsTree;
  private TreeSupplier supplier;
  private SpoonTreeDelegate treeDelegate;

  private boolean showTooltips;
  private DefaultToolTip tooltip;
  //TODO
  // addCoreObjectsTree autollapse, d&drop

  public SpoonTreePanel( Composite parent, String testLabel ) {
    this.testLabel = testLabel;
    buildUI( parent );
    addDragSource( objectsTree );
  }

  /**
   * 
   * @return currently selected objects
   */
  public abstract TreeSelection[] getTreeObjects();

  public interface TreeSupplier {
    void fillTree( Tree tree, String filter );
  }

  protected void buildUI( Composite parent ) {
    // TODO should border be here?
    panelComposite = new Composite( parent, SWT.BORDER );
    fillParentFD( panelComposite );
    panelComposite.setBackground( new Color( parent.getDisplay(), 0 ,255,255 ) );
    panelComposite.setLayout( new FormLayout() );

    Composite toolbarComposite = new Composite( panelComposite, SWT.FLAT );
    FormData fdToolbarComposite = new FormData();
    fdToolbarComposite.top = new FormAttachment( 0 );
    fdToolbarComposite.left = new FormAttachment( 0 );
    fdToolbarComposite.right = new FormAttachment( 100 );
//    fdToolbarComposite.height = 40;
    toolbarComposite.setLayoutData( fdToolbarComposite );

    FillLayout layoutToolbarComposite = new FillLayout();
    //  layoutToolbarComposite.marginWidth = 10;
    //  layoutToolbarComposite.marginHeight = 2;
    toolbarComposite.setLayout( layoutToolbarComposite );
    props.setLook( toolbarComposite, Props.WIDGET_STYLE_TOOLBAR );
    //TODO
    toolbarComposite.setBackground( new Color( parent.getDisplay(), 255, 0 ,255 ) );

    Label toolbarSeparator = new Label( panelComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
    toolbarSeparator.setBackground( GUIResource.getInstance().getColorWhite() );
    FormData fdToolbarSeparator = new FormData();
    fdToolbarSeparator.left = new FormAttachment( 0 );
    fdToolbarSeparator.right = new FormAttachment( 100 );
    fdToolbarSeparator.top = new FormAttachment( toolbarComposite );
    toolbarSeparator.setLayoutData( fdToolbarSeparator );

    toolbar = new SpoonTreePanelToolbar( toolbarComposite, SWT.FLAT );
    toolbar.setText( testLabel );
    objectsTree = new Tree( panelComposite, SWT.FLAT );
    FormData fdObjectsTree = new FormData();
    fdObjectsTree.top = new FormAttachment( toolbarSeparator );
    fdObjectsTree.left = new FormAttachment( 0 );
    fdObjectsTree.right = new FormAttachment( 100 );
    fdObjectsTree.bottom = new FormAttachment( 100 );
    objectsTree.setLayoutData( fdObjectsTree );

    tooltip = new DefaultToolTip( objectsTree, ToolTip.RECREATE, true );
    tooltip.setRespectMonitorBounds( true );
    tooltip.setRespectDisplayBounds( true );
    tooltip.setPopupDelay( TOOLTIP_POPUP_DELAY );
    tooltip.setHideDelay( TOOLTIP_HIDE_DELAY );
    tooltip.setShift( new org.eclipse.swt.graphics.Point( ConstUI.TOOLTIP_OFFSET + 10, ConstUI.TOOLTIP_OFFSET + 10 ) );
  }

  protected Tree getTree() {
    return objectsTree;
  }
  protected SpoonTreePanelToolbar getToolbar() {
    return toolbar;
  }

  protected static LogChannelInterface getLog() {
    // TODO
    return Spoon.getInstance().getLog();
  }

  protected Spoon getSpoon() {
    return Spoon.getInstance();
  }

  public void setSupplier( TreeSupplier treeSupplier ) {
    this.supplier = treeSupplier;
    refreshTree( objectsTree, supplier );
    objectsTree.addMouseMoveListener( new MouseMoveListener() {
      @Override
      public void mouseMove( MouseEvent e ) {
        if ( isShowTooltips() ) {
          TreeItem item = searchMouseOverTreeItem( objectsTree.getItems(), e.x, e.y );
          if ( item != null ) {
//            supplier.showTooltip( tooltip, item, e );
            TreeItemInfo<?> data = (TreeItemInfo<?>) item.getData();
            if ( data != null ) {
              data.showTooltip( tooltip, item, e );
            }
          }
        }
      }
    } );
  }

//  public void setTreeDelegate( SpoonTreeDelegate treeDelegate ) {
//    this.treeDelegate = treeDelegate;
//    treeDelegate.addDragSourceToTree( objectsTree, null, objectsTree );
//  }

  public SpoonTreeDelegate getTreeDelegate() {
    // TODO
    return Spoon.getInstance().delegates.tree;
  }

  public Composite getUI() {
    return panelComposite;
  }

  public void setEnabled( boolean enabled ) {
    setDeepEnabled( panelComposite, enabled );
  }

  public boolean isShowTooltips() {
    return showTooltips;
  }

  public void setShowTooltips( boolean showTooltips ) {
    this.showTooltips = showTooltips;
  }

  public void setAllExpanded( boolean expanded ) {
    tidyBranches( objectsTree.getItems(), expanded );
  }

  // TODO: move to supplier or remove supplier
  protected TreeItem createTreeItem( TreeItem parent, String text, Image image ) {
    TreeItem item = new TreeItem( parent, SWT.NONE );
    item.setText( text );
    item.setImage( image );
    return item;
  }

  public void filterTree( String filterText ) {
    assert supplier != null;
    clearTree( objectsTree );
    supplier.fillTree( objectsTree, filterText );
    setAllExpanded( !Utils.isEmpty( filterText ) );
  }

  protected void clearTree( Tree tree ) {
    //TODO must dispose manually?
    for ( TreeItem item : tree.getItems() ) {
      item.dispose();
    }
    tree.removeAll();
  }

  private void refreshTree( Tree tree, TreeSupplier supplier ) {
    clearTree( tree );
    supplier.fillTree( tree, null );
  }

  private void tidyBranches( TreeItem[] items, boolean expand ) {
    for ( TreeItem item : items ) {
      item.setExpanded( expand );
      tidyBranches( item.getItems(), expand );
    }
  }

  private static void setDeepEnabled( Composite composite, boolean enabled ) {
    composite.setEnabled( enabled );
    for ( Control control : composite.getChildren() ) {
      if ( control instanceof Composite ) {
        setDeepEnabled( (Composite) control, enabled );
      } else {
        control.setEnabled( enabled );
      }
    }
  }

  protected boolean filterMatch( String text ) {
    return filterMatch( text, getToolbar().getSelectionFilter() );
  }

  public static boolean filterMatch( String string, String filter ) {
    if ( Utils.isEmpty( string ) || Utils.isEmpty( filter ) ) {
      return true;
    }

    try {
      if ( string.matches( filter ) ) {
        return true;
      }
    } catch ( Exception e ) {
      // PatternSyntaxException
      // TODO: was sys out
      getLog().logError( "Not a valid pattern [" + filter + "] : " + e.getMessage() );
    }

    return string.toUpperCase().contains( filter.toUpperCase() );
  }

  protected TreeItem searchMouseOverTreeItem( TreeItem[] treeItems, int x, int y ) {
    for ( TreeItem treeItem : treeItems ) {
      if ( treeItem.getBounds().contains( x, y ) ) {
        return treeItem;
      }
      if ( treeItem.getItemCount() > 0 ) {
        treeItem = searchMouseOverTreeItem( treeItem.getItems(), x, y );
        if ( treeItem != null ) {
          return treeItem;
        }
      }
    }
    return null;
  }

  //TODO temp
  private static void fillParentFD( Composite comp ) {
    FormData formData = new FormData();
    formData.top = new FormAttachment( 0 );
    formData.left = new FormAttachment( 0 );
    formData.right = new FormAttachment( 100 );
    formData.bottom = new FormAttachment( 100 );
    comp.setLayoutData( formData );
  }

  /**
   * @see SpoonTreeDelegate#addDragSourceToTree(Tree, Tree, Tree)
   */
  protected void addDragSource( final Tree tree ) {
    Transfer[] ttypes = new Transfer[] { XMLTransfer.getInstance() };
    DragSource ddSource = new DragSource( tree, DND.DROP_MOVE );
    ddSource.setTransfer( ttypes );
    ddSource.addDragListener( new DragSourceListener() {

      @Override
      public void dragStart( DragSourceEvent event ) {
        // TODO did macos have issues w multiple sel?
        TreeItem[] selection = tree.getSelection();
        TreeItem item = selection.length > 0 ? selection[0] : null;
        TreeItemInfo<?> data = getItemData( item );
        event.doit = ( data != null && data.getDragAndDropContainer() != null );
        return;
      }

      @Override
      public void dragSetData( DragSourceEvent event ) {
        TreeItem[] selection = tree.getSelection();
        TreeItem item = selection.length > 0 ? selection[0] : null;
        TreeItemInfo<?> data = getItemData( item );
        event.data = data.getDragAndDropContainer();
      }

      @Override
      public void dragFinished( DragSourceEvent event ) {
      }
    } );
  }

  @SuppressWarnings( "unchecked" )
  protected <T extends TreeItemInfo<?>> T getItemData( TreeItem item ) {
    return item == null ? null : (T) item.getData();
  }

  protected <T extends TreeItemInfo<?>> T getSelectedItemData( Tree tree ) {
    return getItemData( getSelectedItem( tree ) );
  }
  protected TreeItem getSelectedItem( Tree tree ) {
    TreeItem[] selected = getTree().getSelection();
    return selected.length > 0 ? selected[0] : null;
  }

  // TODO
  protected class SpoonTreePanelToolbar extends Composite  {
    private Text selectionFilter;
    private Label selectionLabel;

    public SpoonTreePanelToolbar( Composite parent, int style ) {
      super( parent, style );
      init( parent );
    }

    public void setText( String text ) {
      selectionLabel.setText( text );
    }

    public String getSelectionFilter() {
      return selectionFilter.getText();
    }

    private void init( Composite parent ) {
      GridLayout toolbarCompositeLayout = new GridLayout();
      toolbarCompositeLayout.numColumns = 3;
//      toolbarCompositeLayout.marginLeft = 10;
//      toolbarCompositeLayout.marginRight = 10;
      setLayout( toolbarCompositeLayout );

      setBackground( new Color( parent.getDisplay(), 255, 255, 0 ) );

      selectionLabel = new Label( this, SWT.HORIZONTAL );
      GridData gdLabel = new GridData();
      selectionLabel.setLayoutData( gdLabel );
//      selectionLabel.setText( testLabel );

      selectionFilter = createSelectionFilter( this );
      selectionFilter.addModifyListener( new ModifyListener() {
        public void modifyText( ModifyEvent modEvt ) {
          SpoonTreePanel.this.filterTree( selectionFilter.getText() );
        }
      } );

      ToolBar treeTb = new ToolBar( this, SWT.HORIZONTAL | SWT.FLAT );
      GridData fdTreeTb = new GridData();
      fdTreeTb.horizontalAlignment = SWT.END;
      props.setLook( treeTb, Props.WIDGET_STYLE_TOOLBAR );
      
      RowLayout toolbarLayout = new RowLayout();
      treeTb.setLayout( toolbarLayout );

      createTreeButtons( treeTb );
//      Label sep4 = new Label( toolbarComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
//      sep4.setBackground( GUIResource.getInstance().getColorWhite() );
//      FormData fdSep4 = new FormData();
//      fdSep4.left = new FormAttachment( 0, 0 );
//      fdSep4.right = new FormAttachment( 100, 0 );
//      fdSep4.top = new FormAttachment( treeTb, 5 );
//      sep4.setLayoutData( fdSep4 );
    }

    private void createTreeButtons( ToolBar treeTb ) {
      ToolItem expandAll = new ToolItem( treeTb, SWT.PUSH );
      expandAll.setImage( GUIResource.getInstance().getImageExpandAll() );
      expandAll.addSelectionListener( new SelectionAdapter() {
        public void widgetSelected( SelectionEvent event ) {
          SpoonTreePanel.this.setAllExpanded( true );
        }
      } );
      ToolItem collapseAll = new ToolItem( treeTb, SWT.PUSH );
      collapseAll.setImage( GUIResource.getInstance().getImageCollapseAll() );
      collapseAll.addSelectionListener( new SelectionAdapter() {
        public void widgetSelected( SelectionEvent event ) {
          SpoonTreePanel.this.setAllExpanded( false );
        }
      } );
    }

    private Text createSelectionFilter( Composite parent ) {
      Text selectionFilter =
        new Text( parent, SWT.SINGLE
          | SWT.BORDER | SWT.LEFT | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL );
      selectionFilter.setToolTipText( BaseMessages.getString( PKG, "Spoon.SelectionFilter.Tooltip" ) );
      GridData gridData = new GridData();
      gridData.horizontalAlignment = GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;
      selectionFilter.setLayoutData( gridData );
      return selectionFilter;
    }
  }
}
