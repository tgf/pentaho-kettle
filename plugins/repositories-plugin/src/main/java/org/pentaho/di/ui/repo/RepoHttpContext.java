package org.pentaho.di.ui.repo;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.ops4j.pax.web.service.spi.util.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.HttpContext;

/**
 * Fetches web resources from bundle
 */
public class RepoHttpContext implements HttpContext {

  private Bundle bundle = FrameworkUtil.getBundle( getClass() );
  private Map<String, String> mimeTypes = new Hashtable<>();;

  public Bundle getBundle() {
    return bundle;
  }

  public Map<String, String> getMimeTypes() {
    return mimeTypes;
  }
  public void setMimeTypes( Map<String, String> mimeTypes ) {
    this.mimeTypes = mimeTypes;
  }

  @Override
  public boolean handleSecurity( HttpServletRequest request, HttpServletResponse response ) throws IOException {
    // ?
    return true;
  }

  @Override
  public URL getResource( final String name ) {
    final String normalizedname = Path.normalizeResourcePath( name );
    return getBundle().getResource( normalizedname );
  }

  @Override public String getMimeType( String name ) {
    String extension = FilenameUtils.getExtension( name );
    return getMimeTypes().get( extension );
  }

}
