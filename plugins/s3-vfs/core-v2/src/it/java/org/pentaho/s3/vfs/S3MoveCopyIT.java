package org.pentaho.s3.vfs;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.pentaho.s3.vfs.support.KettleS3Environment;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class S3MoveCopyIT {

  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
    .withSecureConnection( false )
    .withInitialBuckets( KettleS3Environment.BUCKET )
    .silent()
    .build();

  private static KettleS3Environment environment;
  private static String bucketUri;

  @TempDir
  Path localDir;

  @BeforeAll
  static void startEnvironment() throws Exception {
    environment = new KettleS3Environment( S3_MOCK.getServiceEndpoint() );
    bucketUri = environment.createDefaultConnection();
  }

  @Test
  void movesAFile() throws Exception {
    write( "move/source.txt", "moved content" );

    resolve( "move/source.txt" ).moveTo( resolve( "move/target.txt" ) );

    assertFalse( resolve( "move/source.txt" ).exists() );
    assertEquals( "moved content", read( resolve( "move/target.txt" ) ) );
  }

  @Test
  void movesAFolderWithItsChildren() throws Exception {
    write( "move-folder/from/a.txt", "a" );
    write( "move-folder/from/nested/b.txt", "b" );

    resolve( "move-folder/from" ).moveTo( resolve( "move-folder/to" ) );

    assertFalse( resolve( "move-folder/from" ).exists() );
    assertEquals( "a", read( resolve( "move-folder/to/a.txt" ) ) );
    assertEquals( "b", read( resolve( "move-folder/to/nested/b.txt" ) ) );
  }

  @Test
  void copiesWithinS3() throws Exception {
    write( "copy/source.txt", "copied content" );

    resolve( "copy/target.txt" ).copyFrom( resolve( "copy/source.txt" ), Selectors.SELECT_SELF );

    assertTrue( resolve( "copy/source.txt" ).exists() );
    assertEquals( "copied content", read( resolve( "copy/target.txt" ) ) );
  }

  @Test
  void copiesFromTheLocalFileSystem() throws Exception {
    Path local = Files.writeString( localDir.resolve( "local.txt" ), "from disk" );
    FileObject source = environment.vfs().getFileObject( local.toUri().toString() );

    resolve( "copy/uploaded.txt" ).copyFrom( source, Selectors.SELECT_SELF );

    assertEquals( "from disk", read( resolve( "copy/uploaded.txt" ) ) );
  }

  private void write( String path, String content ) throws Exception {
    try ( OutputStream out = resolve( path ).getContent().getOutputStream() ) {
      out.write( content.getBytes( StandardCharsets.UTF_8 ) );
    }
  }

  private String read( FileObject file ) throws Exception {
    try ( InputStream in = file.getContent().getInputStream() ) {
      return new String( in.readAllBytes(), StandardCharsets.UTF_8 );
    }
  }

  private FileObject resolve( String path ) throws Exception {
    return environment.vfs().getFileObject( bucketUri + "/" + path );
  }
}
