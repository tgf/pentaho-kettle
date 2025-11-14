package org.pentaho.di.plugins.repofvs.pur.vfs;

import org.pentaho.di.plugins.repofvs.pur.RepositoryLoader;
import org.pentaho.di.plugins.repofvs.pur.RepositoryLoader.RepositoryLoadException;
import org.pentaho.di.plugins.repofvs.pur.converter.RepoContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VFS Provider based on {@link IUnifiedRepository}
 */
public class PurProvider extends AbstractOriginatingFileProvider {

  public interface ContentConverterHandlerFactory {
    IRepositoryContentConverterHandler getContentHandler( FileSystemOptions fileSystemOptions,
                                                          IUnifiedRepository repo );
  }

  public static final String SCHEME_LOCAL = "purl";
  public static final String SCHEME_REMOTE = "purr";

  private static final Logger log = LoggerFactory.getLogger( PurProvider.class );

  public static final Collection<Capability> capabilities = Collections.unmodifiableCollection( Arrays.asList(
    Capability.GET_TYPE,
    Capability.GET_LAST_MODIFIED,
    Capability.LIST_CHILDREN,
    Capability.READ_CONTENT,
    Capability.WRITE_CONTENT,
    Capability.CREATE,
    Capability.FS_ATTRIBUTES,
    Capability.URI ) );

  private final RepositoryLoader purLoader;
  private final ContentConverterHandlerFactory handlerLoader;

  public static PurProvider createServerLocalProvider() {
    return new PurProvider(
      opts -> PentahoSystem.get( IUnifiedRepository.class ),
      (opts, pur) -> PentahoSystem.get( IRepositoryContentConverterHandler.class ) );
  }

  public static PurProvider createClientRemoteProvider( RepositoryLoader repoLoader ) {
    return new PurProvider( repoLoader,
      (opts, pur) -> new RepoContentConverterHandler( pur ) );

  }

  public PurProvider( RepositoryLoader repoLoader, ContentConverterHandlerFactory handlerLoader ) {
    this.purLoader = repoLoader;
    this.handlerLoader = handlerLoader;
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return capabilities;
  }

  @Override
  protected PurFileSystem doCreateFileSystem( FileName rootFileName, FileSystemOptions fileSystemOptions )
    throws FileSystemException {
    log.debug( "creating filesystem" );
    IUnifiedRepository repo;
    try {
      repo = purLoader.loadRepository( fileSystemOptions );
    } catch ( RepositoryLoadException e ) {
      throw new FileSystemException( e );
    }
    if ( repo == null ) {
      log.error( "no repository!" );
    } else {
      log.info( "filesystem created" );
    }
    var contentHandler = handlerLoader.getContentHandler( fileSystemOptions, repo );
    return new PurFileSystem( rootFileName, fileSystemOptions, repo, contentHandler );
  }

}
