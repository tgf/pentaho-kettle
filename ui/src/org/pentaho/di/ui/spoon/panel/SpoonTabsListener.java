package org.pentaho.di.ui.spoon.panel;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

// TODO: cf org.pentaho.xul.swt.tab.TabListener
// TODO: AbstractGraph?
// TODO: +browser, select/deselected
public interface SpoonTabsListener {

  default void transformationOpen( TransMeta transMeta ) {
  }
  default void jobOpen( JobMeta transMeta ) {
  }
  default void transformationClose( TransMeta transMeta ) {
  }
  default void jobClose( JobMeta transMeta ) {
  }

}
