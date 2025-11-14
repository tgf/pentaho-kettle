package org.pentaho.di.plugins.repofvs.pur.converter;

import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.ConverterException;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;

import java.io.InputStream;
import java.io.Serializable;

/** Converter that delegates all calls with the target class loader */
public class CLDelegatingConverter implements Converter {

  private final ClassLoader classLoader;
  private final Converter delegate;

  public CLDelegatingConverter( ClassLoader classLoader, Converter delegate ) {
    this.classLoader = classLoader;
    this.delegate = delegate;
  }

  @Override
  public IRepositoryFileData convert( InputStream inputStream, String charset, String mimeType )
    throws ConverterException {
    try( var sw = switchClassLoader() ) {
      return delegate.convert( inputStream, charset, mimeType );
    }
  }

  @Override
  public InputStream convert( IRepositoryFileData data ) {
    try( var sw = switchClassLoader() ) {
      return delegate.convert( data );
    }
  }

  @Override
  public InputStream convert( Serializable fileId ) {
    try ( var sw = switchClassLoader() ) {
      return delegate.convert( fileId );
    }
  }

  private WithClassLoader switchClassLoader() {
    return new WithClassLoader( classLoader );
  }
}
