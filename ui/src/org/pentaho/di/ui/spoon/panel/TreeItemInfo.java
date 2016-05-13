package org.pentaho.di.ui.spoon.panel;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.ui.xul.containers.XulMenupopup;

/**
 * associate to each tree item a treeItemInfo that know what to do and has a ref to the target object?
 * would spread SpoonTreeDelegate over short classes
 * pros:  possibly get rid of getTreeObjects <-- except for extension pts :(
 *        get rid of tree selection
 *        possibly get rid of a good chunk of dragDropSource
 * TODO: check if all calls on getTreeSelection are handled the same (same objects for each element)
 * TODO: how many items would we end up with?
 * TODO: is generic worth it or will we end up with just <?> all the time?
 * TODO: any reusables between view/design or just split inh there?
 */
public class TreeItemInfo<T> {

  // the object (step class, connection etc)
  private T target;


  public TreeItemInfo( T object ) {
    target = object;
  }

  // TODO: don't expose?
  public T getTarget() {
    return target;
  }

  public boolean showTooltip( DefaultToolTip tooltip, TreeItem item, MouseEvent evt ) {
    return false;
  }

  /**
   * the right-click popup menu
   * TODO: use map?
   */
  public Menu getContextMenu( Tree tree ) {
    return null;
  }

  protected Menu createMenu( Tree tree ) {
    // TODO: dispose old? does xul keep a perm refs to its managed object?
    Menu oldMenu = tree.getMenu();
    if ( oldMenu != null ) {
      tree.setMenu( null );
      for ( MenuItem item : oldMenu.getItems() ) {
        item.dispose();
      }
      oldMenu.dispose();
    }
    return new Menu( tree );
  }

  protected MenuItem createMenuItem( Menu menu, Class<?> pkg , String labelKey, SelectionListener command ) {
    String label = BaseMessages.getString( pkg, labelKey );
    MenuItem item = new MenuItem( menu, SWT.NONE );
    item.setText( label );
    item.addSelectionListener( command );
    return item;
  }

  protected MenuItem createSeparator( Menu menu ) {
    return new MenuItem( menu, SWT.SEPARATOR );
  }
  // TODO: getDragAndDropContainer, null if N/A
//  public boolean isDraggable() {
//    return getDragAndDropType() != null;
//  }

  public DragAndDropContainer getDragAndDropContainer() {
    return null;
  }

  public void doubleClick() {
  }

  public static class StepTreeItemInfo extends TreeItemInfo<StepMeta> {

    public StepTreeItemInfo( StepMeta object ) {
      super( object );
    }
    
  }

  public static abstract class LookupTreeItemInfo<T> extends TreeItemInfo<T> {

    private String id;

    public LookupTreeItemInfo( String id ) {
      super( null );
      this.id = id;
    }

    protected String getId() {
      return id;
    }

    @Override
    public T getTarget() {
      return lookup( id );
    }

    protected abstract T lookup( String id );
  }


}
