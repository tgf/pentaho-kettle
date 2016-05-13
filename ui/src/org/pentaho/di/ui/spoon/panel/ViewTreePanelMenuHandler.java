package org.pentaho.di.ui.spoon.panel;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.delegates.SpoonStepsDelegate;
import org.pentaho.di.ui.util.HelpUtils;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.impl.XulEventHandler;

public class ViewTreePanelMenuHandler implements XulEventHandler {
  public static final String PARENT = "parent-info";

  private String XUL_MENU_FILE = "ui/view-tree-menu.xul";

  private String name = "handler";
  private Object data;
  private XulDomContainer xulDomContainer;
  //TODO: temp?
//  private Spoon theSpoon;
//  private XulLoader xulLoader;

  private Object selectionObject;
  private Object selectionObjectParent;
  private TreeItem targetItem;

  private static final class ShareCodes {
    public static final String DATABASE = "database-inst-share";
    public static final String STEP = "step-inst-share";
    public static final String PARTITION_SCHEMA = "ppartition-schema-inst-share";
    public static final String CLUSTER_SCHEMA = "cluster-schema-inst-share";
    public static final String SLAVE_SERVER = "slave-server-inst-share";
  }
////
//  private Tree tree;
//  private SpoonViewTreePanel viewTreePanel;
//  private static Map<Class<?>, String> treeClassMap = Collections.unmodifiableMap( new HashMap<Class<?>, String>() {
//    private static final long serialVersionUID = 1L;
//    {
//      put( TransMeta.class, "trans-class");
//      put( JobMeta.class, "job-class");
//      put( TransHopMeta.class, "trans-hop-class");
//      put( DatabaseMeta.class, "database-class");
//      put( PartitionSchema.class, "partition-schema-class");
//      put( ClusterSchema.class, "cluster-schema-class");
//      put( SlaveServer.class, "slave-cluster-class");
//    }
//  } );
//
//  private static Map<Class<?>, String> treeInstMap = Collections.unmodifiableMap( new HashMap<Class<?>, String>() {
//    private static final long serialVersionUID = 1L;
//    {
//      put( TransMeta.class, "trans-inst");
//      put( JobMeta.class, "job-inst");
//      put( PluginInterface.class, "step-plugin");
//      put( DatabaseMeta.class, "database-inst");
//      put( StepMeta.class, "step-inst");
//      put( JobEntryCopy.class, "job-entry-copy-inst");
//      put( TransHopMeta.class, "trans-hop-inst");
//      put( PartitionSchema.class, "partition-schema-inst");
//      put( ClusterSchema.class, "cluster-schema-inst");
//      put( SlaveServer.class, "slave-server-inst");
//    }
//  } );

  public ViewTreePanelMenuHandler() {
    
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public Object getData() {
    return data;
  }

  public void setData( Object data ) {
    this.data = data;
  }

  public XulDomContainer getXulDomContainer() {
    return xulDomContainer;
  }

  public void setXulDomContainer( XulDomContainer xulDomContainer ) {
    this.xulDomContainer = xulDomContainer;
  }

  private SpoonStepsDelegate getStepDelegate() {
    return Spoon.getInstance().delegates.steps;
  }
  private Spoon getSpoon() {
    return Spoon.getInstance();
  }

  public void setSelectionObject( Object selectionObject ) {
    this.selectionObject = selectionObject;
  }

  public void setSelectionObjectParent( Object selectionObjectParent ) {
    this.selectionObjectParent = selectionObjectParent;
  }


  public TreeItem getTargetItem() {
    return targetItem;
  }

  public void setTargetItem( TreeItem targetItem ) {
    this.targetItem = targetItem;
  }

  public void editStep() {
    final StepMeta stepMeta = (StepMeta) selectionObject;
    getSpoon().editStep( stepMeta.getParentTransMeta(), stepMeta );
  }

  public void dupeStep() {
    final StepMeta stepMeta = (StepMeta) selectionObject;
    getStepDelegate().dupeStep( stepMeta.getParentTransMeta(), stepMeta );
  }

  public void delStep() {
    final StepMeta stepMeta = (StepMeta) selectionObject;
    getStepDelegate().delStep( stepMeta.getParentTransMeta(), stepMeta );
  }

  public void shareObject( String str ) {
    switch( str ) {
      case ShareCodes.STEP:
        // TODO: just make public in spoon?
        final StepMeta stepMeta = (StepMeta) selectionObject;
        SharedObjects sharedObjects = stepMeta.getParentTransMeta().getSharedObjects();
        if ( sharedObjects != null ) {
          sharedObjects.storeObject( stepMeta );
//          try {
//            sharedObjects.saveToFile();
//          } catch ( IOException e ) {
//            // TODO Auto-generated catch block
//            logger.error(e);
//          } catch ( KettleException e ) {
//            // TODO Auto-generated catch block
//            logger.error(e);
//          }
        }
        break;
    }
    //TODO: need current trans/job
  }
  public void helpStep() {
    final StepMeta stepMeta = (StepMeta) selectionObject;
    PluginInterface stepPlugin =
        PluginRegistry.getInstance().findPluginWithId( StepPluginType.class, stepMeta.getStepID() );
    // TODO: there is no spoon
    HelpUtils.openHelpDialog( Spoon.getInstance().getShell(), stepPlugin );
  }
//  36     <menuitem id="step-inst-edit" label="${Spoon.Menu.Popup.STEPS.Edit}" command="spoon.editStep()" />
//  37     <menuitem id="step-inst-duplicate" label="${Spoon.Menu.Popup.STEPS.Duplicate}" command="spoon.dupeStep()" />
//  38     <menuitem id="step-inst-delete" label="${Spoon.Menu.Popup.STEPS.Delete}" command="spoon.delStep()" />
//  39     <menuitem id="step-inst-share" label="${Spoon.Menu.Popup.STEPS.Share}" command="spoon.shareObject('step-inst-share')" />
//  40     <menuseparator id="step-inst-separator-1"/>
//  41     <menuitem id="step-inst-help" label="${Spoon.Menu.Popup.STEPS.Help}" command="spoon.helpStep()" />

  //  Spoon methods used exclusively by view tab menu popup xul (and maybe some random plugins...):
  //    delClusterSchema()
  //    delConnection()
  //    deleteJobEntryCopies()
  //    delHop()
  //    delPartitionSchema()
  //    delSlaveServer()
  //    delStep()
  //    displayDbDependancies()
  //    dupeConnection()
  //    dupeJobEntry()
  //    editConnection()
  //    editJobEntry()
  //    editJobPropertiesPopup()
  //    editPartitionSchema()
  //    editSlaveServer()
  //    editTransformationPropertiesPopup()
  //    helpJobEntry()
  //    helpStep() <
  //    monitorClusterSchema()
  //    shareObject(String)
  //    sqlConnection()
  //  Spoon methods shared by view tab menu popup xul:
  //    dupeStep()
  //    editClusterSchema()
  //    editHop()
  //    editStep() <
  //    exploreDB()
}
