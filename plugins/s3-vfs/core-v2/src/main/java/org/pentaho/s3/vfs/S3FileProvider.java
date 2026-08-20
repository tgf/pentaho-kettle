package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class S3FileProvider extends AbstractOriginatingFileProvider {

  /**
   * The scheme this provider was designed to support.
   */
  public static final String SCHEME = "s3";

  /**
   * User Information.
   */
  public static final String ATTR_USER_INFO = "UI";

  /**
   * Authentication types.
   */
  public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES =
    new UserAuthenticationData.Type[] {
      UserAuthenticationData.USERNAME,
      UserAuthenticationData.PASSWORD
    };

  /**
   * The provider's capabilities.
   */
  protected static final Collection<Capability> capabilities =
    Collections.unmodifiableCollection( Arrays.asList(
      new Capability[] {
        Capability.CREATE,
        Capability.DELETE,
        Capability.RENAME,
        Capability.GET_TYPE,
        Capability.LIST_CHILDREN,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.WRITE_CONTENT,
        Capability.GET_LAST_MODIFIED,
        Capability.RANDOM_ACCESS_READ
      } ) );

  public S3FileProvider() {
    super();
    setFileNameParser( S3FileNameParser.getInstance() );
  }

  protected FileSystem doCreateFileSystem( final FileName name, final FileSystemOptions fileSystemOptions ) {
    return new S3FileSystem( name, fileSystemOptions );
  }

  public Collection<Capability> getCapabilities() {
    return capabilities;
  }

}
