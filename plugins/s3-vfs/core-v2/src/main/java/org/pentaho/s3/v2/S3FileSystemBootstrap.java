package org.pentaho.s3.v2;

import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSLookupFilter;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.s3.vfs.S3FileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the S3 VFS file provider and connection provider (PVFS).
 */
@KettleLifecyclePlugin( id = "AmazonS3FileSystemBootstrap", name = "Amazon S3 FileSystem Bootstrap V2" )
public class S3FileSystemBootstrap implements KettleLifecycleListener {
  private static final Logger log = LoggerFactory.getLogger( S3FileSystemBootstrap.class );

  private final Supplier<ConnectionManager> connectionManager = ConnectionManager::getInstance;

  @Override
  public void onEnvironmentInit() throws LifecycleException {
    log.debug( "S3 VFS driver initializing" );
    try {
      registerFileProvider();
      registerConnectionProvider();
    } catch ( FileSystemException e ) {
      log.error( "Failed to load the S3 VFS driver", e );
    }
  }

  private void registerFileProvider() throws FileSystemException {
    FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
    if ( !Arrays.asList( fsm.getSchemes() ).contains( S3FileProvider.SCHEME )
      && fsm instanceof DefaultFileSystemManager dfsm ) {
      dfsm.addProvider( S3FileProvider.SCHEME, new S3FileProvider() );
    }
  }

  private void registerConnectionProvider() {
    ConnectionManager manager = connectionManager.get();
    if ( manager != null ) {
      manager.addConnectionProvider( S3FileProvider.SCHEME, new S3Provider() );
      manager.addLookupFilter( new VFSLookupFilter() );
    }
  }

  @Override
  public void onEnvironmentShutdown() {
    // noop
  }
}
