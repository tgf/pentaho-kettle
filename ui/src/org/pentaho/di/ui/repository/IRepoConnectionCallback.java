package org.pentaho.di.ui.repository;

import java.util.Map;

/**
 * Created by bmorrise on 2/23/16.
 */
public interface IRepoConnectionCallback {
  void invoke( Map<String, Object> map );
}

