package org.inria.websmatch.gwt.spreadsheet.client.exceptions;

import java.io.Serializable;

public class ParserException extends Exception implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -5470885045888058715L;
    
    public ParserException(){
	super();
    }

    public ParserException(String s){
	super(s);
    }
    
}
