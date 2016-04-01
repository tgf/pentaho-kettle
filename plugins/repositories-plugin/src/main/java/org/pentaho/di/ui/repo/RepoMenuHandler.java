package org.pentaho.di.ui.repo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.ui.repository.IRepoConnectionCallback;
import org.pentaho.di.ui.repository.IXulDialogExtension;
import org.pentaho.di.ui.repository.controllers.RepositoriesController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;


public class RepoMenuHandler extends AbstractXulEventHandler implements IXulDialogExtension {

  private static final String MENU_EVENT_HANDLER = "repositoriesMenuEventHandler";
  private static final String REPOSITORIES_CONTROLLER = "repositoryLoginController";
  private static final String NEW_REPO_BTN = "repository-add";

  private RepositoriesController controller;

  private static Log logger = LogFactory.getLog( RepoMenuHandler.class );
//  private Spoon spoon;
//  private static Class<?> PKG = RepoMenuHandler.class; // for i18n purposes

  public RepoMenuHandler() {
    this.setName( MENU_EVENT_HANDLER );
  }

  public void extendXulDialog( XulDomContainer container ) {
    try {
      controller = (RepositoriesController) container.getEventHandler( REPOSITORIES_CONTROLLER );
      container.registerClassLoader( getClass().getClassLoader() );
      container.addEventHandler( this );
      XulButton addButton = (XulButton) container.getDocumentRoot().getElementById( NEW_REPO_BTN );
      addButton.setOnclick( getName() + ".openNewRepoDialog()" );
    } catch ( XulException e ) {
      logger.error(e);
    }
  }

  public void openNewRepoDialog() {
    openNewRepositoryDialog( controller.getShell(), controller.getRepoConnectionCallback() );
  }

  private void openNewRepositoryDialog( Shell shell, IRepoConnectionCallback callback ) {
    RepoConnectionDialog repoConnectionDialog = new RepoConnectionDialog( shell );
    repoConnectionDialog.open( callback );
  }


  public void openRepositoriesDialog() throws XulException  {
    //
    Spoon.getInstance().openRepository();
  }

}
