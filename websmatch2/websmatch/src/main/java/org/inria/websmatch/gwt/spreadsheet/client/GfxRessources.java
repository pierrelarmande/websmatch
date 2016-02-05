package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GfxRessources extends ClientBundle {
	  public static final GfxRessources INSTANCE =  GWT.create(GfxRessources.class);

	  @Source("images/leftarrow.png")
	  ImageResource leftArrow();

	  @Source("images/rightarrow.png")
	  ImageResource rightArrow();
	  
	  @Source("images/ajax-loader.gif")
	  ImageResource loader();
	  
	  @Source("images/bottomleft-inner.png")
	  ImageResource bottomLeftInner();
	  
	  @Source("images/bottomleft.png")
	  ImageResource bottomLeft();
	  
	  @Source("images/bottomright-inner.png")
	  ImageResource bottomRightInner();
	  
	  @Source("images/bottomright.png")
	  ImageResource bottomRight();
	  
	  @Source("images/topleft-inner.png")
	  ImageResource topLeftInner();
	  
	  @Source("images/topleft.png")
	  ImageResource topLeft();
	  
	  @Source("images/topright-inner.png")
	  ImageResource topRightInner();
	  
	  @Source("images/topright.png")
	  ImageResource topRight();
	  
	  @Source("images/INRIA_CHERCHEURS_UK_RVB.jpg")
	  ImageResource logo();
	  
	  @Source("images/Lirmm-logo.gif")
	  ImageResource lirmm();
	  
	  @Source("images/UM2.png")
	  ImageResource UM2();
	  
	  @Source("images/ibc.png")
	  ImageResource ibc();
	  
	  @Source("images/plus.png")
	  ImageResource plus();
	  
	  @Source("images/moins.png")
	  ImageResource moins();
	  
	  @Source("images/dialog-warning.png")
	  ImageResource warning();
	  
	  @Source("images/ikaruga-warninga.jpg")
	  ImageResource ikWarning();
	  
	  @Source("images/trashed.png")
	  ImageResource trashed();
}
