package org.pentaho.s3.vfs;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.apache.commons.vfs2.FileObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pentaho.s3.vfs.support.KettleS3Environment;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class S3ReadWriteIT {

  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
    .withSecureConnection( false )
    .withInitialBuckets( KettleS3Environment.BUCKET )
    .silent()
    .build();

  private static KettleS3Environment environment;
  private static String bucketUri;

  @BeforeAll
  static void startEnvironment() throws Exception {
    environment = new KettleS3Environment( S3_MOCK.getServiceEndpoint() );
    bucketUri = environment.createDefaultConnection();
  }

  @Test
  void readsBackWhatWasWritten() throws Exception {
    FileObject file = resolve( "round-trip.txt" );
    byte[] content = "some content".getBytes( StandardCharsets.UTF_8 );

    try ( OutputStream out = file.getContent().getOutputStream() ) {
      out.write( content );
    }

    assertArrayEquals( content, read( resolve( "round-trip.txt" ) ) );
    assertEquals( content.length, resolve( "round-trip.txt" ).getContent().getSize() );
  }

  @Test
  void overwritingReplacesTheWholeContent() throws Exception {
    write( "overwrite.txt", "a long first version".getBytes( StandardCharsets.UTF_8 ) );
    write( "overwrite.txt", "short".getBytes( StandardCharsets.UTF_8 ) );

    assertEquals( "short", new String( read( resolve( "overwrite.txt" ) ), StandardCharsets.UTF_8 ) );
    assertEquals( 5, resolve( "overwrite.txt" ).getContent().getSize() );
  }

  @Test
  void writesMoreThanOnePart() throws Exception {
    byte[] megabyte = new byte[ 1024 * 1024 ];
    Arrays.fill( megabyte, (byte) 'x' );

    try ( OutputStream out = resolve( "multipart.bin" ).getContent().getOutputStream() ) {
      for ( int i = 0; i < 6; i++ ) {
        out.write( megabyte );
      }
    }

    assertEquals( 6L * megabyte.length, resolve( "multipart.bin" ).getContent().getSize() );
    assertEquals( 6L * megabyte.length, read( resolve( "multipart.bin" ) ).length );
  }

  private void write( String path, byte[] content ) throws Exception {
    try ( OutputStream out = resolve( path ).getContent().getOutputStream() ) {
      out.write( content );
    }
  }

  private byte[] read( FileObject file ) throws Exception {
    try ( InputStream in = file.getContent().getInputStream() ) {
      return in.readAllBytes();
    }
  }

  private FileObject resolve( String path ) throws Exception {
    return environment.vfs().getFileObject( bucketUri + "/" + path );
  }
}
