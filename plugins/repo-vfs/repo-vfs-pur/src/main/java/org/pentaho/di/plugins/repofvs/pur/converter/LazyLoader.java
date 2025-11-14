package org.pentaho.di.plugins.repofvs.pur.converter;

import java.util.function.Supplier;

/**
 * Calls given supplier once (if successful) and caches the result. Thread-safe.
 */
public class LazyLoader<T> implements Supplier<T> {
  private final Supplier<T> loader;
  private T value;

  public LazyLoader( Supplier<T> loader ) {
    this.loader = loader;
  }

  @Override
  public T get() {
    if ( value == null ) {
      synchronized( this ) {
        if ( value == null ) {
          value = loader.get();
        }
      }
    }
    return value;
  }


}
