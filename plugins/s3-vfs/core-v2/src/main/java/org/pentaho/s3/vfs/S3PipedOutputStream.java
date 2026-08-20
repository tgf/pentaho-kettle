package org.pentaho.s3.vfs;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.i18n.BaseMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Custom OutputStream that enables chunked uploads into S3
 */
public class S3PipedOutputStream extends PipedOutputStream {

  private static final Class<?> PKG = S3PipedOutputStream.class;
  private static final Logger logger = LoggerFactory.getLogger( S3PipedOutputStream.class );

  /**
   * set to aws multipart minimum 5MB.
   */
  private static final int DEFAULT_PART_SIZE = 5 * 1024 * 1024;
  private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool( 1 );
  private boolean initialized = false;
  private boolean blockedUntilDone = true;
  private PipedInputStream pipedInputStream;
  private S3AsyncTransferRunner s3AsyncTransferRunner;
  private S3FileSystem fileSystem;
  private Future<Boolean> result = null;
  private String bucketId;
  private String key;
  /**
   * AWS Multipart part size.
   */
  private int partSize;

  public S3PipedOutputStream( S3FileSystem fileSystem, String bucketId, String key ) throws IOException {
    this( fileSystem, bucketId, key, DEFAULT_PART_SIZE );
  }

  public S3PipedOutputStream( S3FileSystem fileSystem, String bucketId, String key, int partSize ) throws IOException {
    this.pipedInputStream = new PipedInputStream();

    try {
      this.pipedInputStream.connect( this );
    } catch ( IOException e ) {
      // FATAL, unexpected
      throw new IOException( "could not connect to pipedInputStream", e );
    }

    this.s3AsyncTransferRunner = new S3AsyncTransferRunner();
    this.bucketId = bucketId;
    this.key = key;
    this.fileSystem = fileSystem;
    this.partSize = partSize;
  }

  private void initializeWrite() {
    if ( !initialized ) {
      initialized = true;
      result = this.executor.submit( s3AsyncTransferRunner );
    }
  }

  public boolean isBlockedUntilDone() {
    return blockedUntilDone;
  }

  public void setBlockedUntilDone( boolean blockedUntilDone ) {
    this.blockedUntilDone = blockedUntilDone;
  }

  @Override
  public void write( int b ) throws IOException {
    initializeWrite();
    super.write( b );
  }

  @Override
  public void write( byte[] b, int off, int len ) throws IOException {
    initializeWrite();
    super.write( b, off, len );
  }

  /*
   * TODO: a failed upload is currently silent.
   *
   * S3AsyncTransferRunner.call() catches everything and returns FALSE, and the wait below only
   * polls result.isDone() - the Future's value is never read. So an upload that failed (or was
   * aborted) still lets close() return normally, and callers such as
   * S3FileObject.copySingleFileFrom then treat the transfer as successful. This matters more now
   * that S3FileSystem.upload() routes through this class instead of the SDK v1 TransferManager.
   *
   * Fix by replacing the poll with result.get() and throwing an IOException when the runner
   * reports failure - which also means call() should carry the cause rather than a boolean.
   * Kept separate because it turns currently-silent failures into hard errors, and that needs
   * its own testing.
   */
  @Override
  public void close() throws IOException {
    super.close();

    if ( initialized && isBlockedUntilDone() ) {
      while ( !result.isDone() ) {
        try {
          Thread.sleep( 100 );
        } catch ( InterruptedException e ) {
          logger.error( BaseMessages.getString( PKG, "ERROR.S3MultiPart.ExceptionCaught" ), e );
          Thread.currentThread().interrupt();
        }
      }
    }
    this.executor.shutdown();
  }

  class S3AsyncTransferRunner implements Callable<Boolean> {

    public Boolean call() throws Exception {
      boolean returnVal = true;
      List<CompletedPart> completedParts = new ArrayList<>();

      // Step 1: Initialize
      CreateMultipartUploadRequest initRequest =
        CreateMultipartUploadRequest.builder().bucket( bucketId ).key( key ).build();

      CreateMultipartUploadResponse initResponse = null;

      // NOTE: byte[] max size is ~2GB < 5GB = aws api max part size
      try ( ByteArrayOutputStream baos = new ByteArrayOutputStream( partSize );
            BufferedInputStream bis = new BufferedInputStream( pipedInputStream, partSize ) ) {
        initResponse = fileSystem.getS3Client().createMultipartUpload( initRequest );
        // Step 2: Upload parts.
        byte[] tmpBuffer = new byte[ partSize ];
        int read = 0;
        long offset = 0;
        long totalRead = 0;
        int partNum = 1;

        logger.info( BaseMessages.getString( PKG, "INFO.S3MultiPart.Start" ) );
        while ( ( read = bis.read( tmpBuffer ) ) >= 0 ) {

          // if something was actually read
          if ( read > 0 ) {
            baos.write( tmpBuffer, 0, read );
            totalRead += read;
          }

          if ( totalRead > partSize ) {
            logger.info( BaseMessages.getString( PKG, "INFO.S3MultiPart.Upload", partNum, offset,
              Long.toString( totalRead ) ) );
            completedParts.add( uploadPart( initResponse.uploadId(), partNum++, baos.toByteArray(), totalRead ) );

            offset += totalRead;
            totalRead = 0; // reset part size counter
            baos.reset(); // reset output stream to 0
          }
        }

        // Step 2.1 upload last part
        logger.info( BaseMessages.getString( PKG, "INFO.S3MultiPart.Upload", partNum, offset, totalRead ) );
        completedParts.add( uploadPart( initResponse.uploadId(), partNum, baos.toByteArray(), totalRead ) );

        // Step 3: Complete.
        logger.info( BaseMessages.getString( PKG, "INFO.S3MultiPart.Complete" ) );
        fileSystem.getS3Client().completeMultipartUpload( CompleteMultipartUploadRequest.builder()
          .bucket( bucketId ).key( key ).uploadId( initResponse.uploadId() )
          .multipartUpload( CompletedMultipartUpload.builder().parts( completedParts ).build() )
          .build() );
      } catch ( Exception e ) {
        logger.error( BaseMessages.getString( PKG, "ERROR.S3MultiPart.ExceptionCaught" ), e );
        if ( initResponse == null ) {
          close();
        } else {
          fileSystem.getS3Client().abortMultipartUpload( AbortMultipartUploadRequest.builder()
            .bucket( bucketId ).key( key ).uploadId( initResponse.uploadId() ).build() );
          logger.error( BaseMessages.getString( PKG, "ERROR.S3MultiPart.Aborted" ) );
        }
        returnVal = false;
      }

      return returnVal;
    }

    private CompletedPart uploadPart( String uploadId, int partNum, byte[] part, long length )
      throws FileSystemException {
      UploadPartRequest uploadRequest = UploadPartRequest.builder()
        .bucket( bucketId ).key( key )
        .uploadId( uploadId ).partNumber( partNum )
        .contentLength( length )
        .build();

        // TODO: why do we need a bogus skip on this S3WindowedSubstream impl?
      String eTag = fileSystem.getS3Client()
        .uploadPart( uploadRequest, RequestBody.fromInputStream( new S3WindowedSubstream( part ), length ) )
        .eTag();

      return CompletedPart.builder().partNumber( partNum ).eTag( eTag ).build();
    }
  }
}
