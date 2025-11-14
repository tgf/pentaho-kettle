package org.pentaho.di.plugins.repofvs.pur.converter;

import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectedNodeConverter implements Supplier<Converter> {

  private final static Logger log = LoggerFactory.getLogger( ReflectedNodeConverter.class );

  private final String className;
  private final IUnifiedRepository pur;
  private final Supplier<ClassLoader> purPluginClassLoader;
  private final LazyLoader<Converter> loader = new LazyLoader<>( this::getByReflection );

  public ReflectedNodeConverter( String className, IUnifiedRepository pur, Supplier<ClassLoader> purPluginClassLoader ) {
    this.className = className;
    this.pur = pur;
    this.purPluginClassLoader = purPluginClassLoader;
  }

  private Converter getByReflection() {
    try {
      ClassLoader classLoader = purPluginClassLoader.get();
      Class<?> clazz = classLoader.loadClass( className );

      try ( var sw = new WithClassLoader( classLoader ) ) {
        Converter converter = (Converter) clazz.getDeclaredConstructor( IUnifiedRepository.class ).newInstance( pur );
        return new CLDelegatingConverter( classLoader, converter );
      }

    } catch ( Exception e ) {
      log.error( "Unable to load converter {}", className, e );
      return null;
    }
  }

  @Override
  public Converter get() {
    return loader.get();
  }


}
