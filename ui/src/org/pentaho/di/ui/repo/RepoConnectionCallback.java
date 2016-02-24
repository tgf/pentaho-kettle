package org.pentaho.di.ui.repo;

import java.util.Map;

/**
 * Created by bmorrise on 2/23/16.
 */
public interface RepoConnectionCallback {
  void invoke( Map<String, Object> map );
}

