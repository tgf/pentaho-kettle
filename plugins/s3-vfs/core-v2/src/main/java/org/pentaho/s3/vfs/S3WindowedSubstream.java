package org.pentaho.s3.vfs;

import java.io.ByteArrayInputStream;

/**
 * Custom InputStream that only virtually skips data to mimic a long running stream
 *
 * @author asimoes
 * @since 09-11-2017
 */
public class S3WindowedSubstream extends ByteArrayInputStream {

  public S3WindowedSubstream( byte[] buf ) {
    super( buf );
  }

  @Override public synchronized long skip( long n ) {
    // virtual skip
    return n;
  }
}
