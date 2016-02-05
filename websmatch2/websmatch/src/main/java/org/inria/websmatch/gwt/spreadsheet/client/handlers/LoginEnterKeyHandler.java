package org.inria.websmatch.gwt.spreadsheet.client.handlers;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;

public class LoginEnterKeyHandler implements KeyDownHandler {
    
    private Button signInButton;
    
    public LoginEnterKeyHandler(Button loginButton){
	signInButton = loginButton;
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {	
	if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
	    signInButton.click();
	}	
    }

}
