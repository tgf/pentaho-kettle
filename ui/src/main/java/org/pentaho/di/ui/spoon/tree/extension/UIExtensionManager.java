package org.pentaho.di.ui.spoon.tree.extension;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.core.FormDataBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The class manages the list of UIExtensions.
 */
public class UIExtensionManager {

  private final List<UIExtension> extensions = new ArrayList<>();

  public void addUIExtension( UIExtension extension ) {
    extensions.add( extension );
  }

  /**
   * It loops through the list of extensions and builds each of the extensions
   * @param main
   * @throws KettleException
   */
  public void buildUIExtensions( Composite main ) throws KettleException {
    try {
      Composite extensionsMain = new Composite( main, SWT.NONE );
      extensionsMain.setLayoutData( new FormDataBuilder().top().left().right().result() );
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 1;
      extensionsMain.setLayout( gridLayout );
      for ( UIExtension extension : extensions ) {
        Composite extensionContainer = new Composite( extensionsMain, SWT.NONE );
        extensionContainer.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        extension.buildExtension( extensionContainer );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Exception building UI extensions", e );
    }
  }
}
