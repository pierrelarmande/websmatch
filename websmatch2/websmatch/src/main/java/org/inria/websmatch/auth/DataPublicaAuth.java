package org.inria.websmatch.auth;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class DataPublicaAuth extends Authenticator {
    
    private String user = "sherlock";
    private String pass = "crawl";

    public PasswordAuthentication getPasswordAuthentication () {
        return new PasswordAuthentication (user, pass.toCharArray());
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}

