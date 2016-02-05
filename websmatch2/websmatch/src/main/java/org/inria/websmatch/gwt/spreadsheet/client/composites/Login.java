package org.inria.websmatch.gwt.spreadsheet.client.composites;

import org.inria.websmatch.gwt.spreadsheet.client.handlers.LoginEnterKeyHandler;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Login extends Composite {

    private Button signInButton;
    private CheckBox rememberMeOnCheckBox;
    // private CheckBox debugComparatorCheckBox;
    private ListBox appListBox;
    private PasswordTextBox textBoxPassword;
    private TextBox textBoxUsername;
    private Label passwordLabel;
    private Label usernameLabel;
    private FlexTable flexTable;
    private Label signInToLabel;
    private VerticalPanel verticalPanel;
    private Label lblYouCanUse;

    public Login(String[] appList) {

	AbsolutePanel fpanel = new AbsolutePanel();
	initWidget(fpanel);
	fpanel.setSize("326px", "218px");

	verticalPanel = new VerticalPanel();
	verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	verticalPanel.setSize("326px", "218px");

	fpanel.add(verticalPanel, 0, 0);

	signInToLabel = new Label("Sign in to your account.");
	verticalPanel.add(signInToLabel);
	signInToLabel.setSize("100%", "40px");

	flexTable = new FlexTable();
	verticalPanel.add(flexTable);

	usernameLabel = new Label("Username:");
	flexTable.setWidget(0, 0, usernameLabel);

	passwordLabel = new Label("Password:");
	flexTable.setWidget(1, 0, passwordLabel);

	textBoxUsername = new TextBox();
	flexTable.setWidget(0, 1, textBoxUsername);

	textBoxPassword = new PasswordTextBox();
	flexTable.setWidget(1, 1, textBoxPassword);

	rememberMeOnCheckBox = new CheckBox();
	flexTable.setWidget(2, 1, rememberMeOnCheckBox);
	rememberMeOnCheckBox.setText("Remember me on this computer.");

	/*
	 * debugComparatorCheckBox = new CheckBox(); flexTable.setWidget(3, 1,
	 * debugComparatorCheckBox);
	 * debugComparatorCheckBox.setText("Use debug comparator tool.");
	 */

	appListBox = new ListBox();
	// main app
	appListBox.addItem("Excel importer");
	for (int i = 0; i < appList.length; i++)
	    appListBox.addItem(appList[i]);
	flexTable.setWidget(3, 1, appListBox);
	appListBox.setVisible(false);

	signInButton = new Button();
	flexTable.setWidget(4, 1, signInButton);

	signInButton.setText("Sign In");

	lblYouCanUse = new Label("You can use \"test\" \"test\" to sign in.");
	lblYouCanUse.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	lblYouCanUse.setStyleName("gwt-Label-info");
	verticalPanel.add(lblYouCanUse);
	lblYouCanUse.setWidth("100%");

	// add the enter handler
	LoginEnterKeyHandler handler = new LoginEnterKeyHandler(signInButton);
	textBoxUsername.addKeyDownHandler(handler);
	textBoxPassword.addKeyDownHandler(handler);
	// also on the checkboxes
	rememberMeOnCheckBox.addKeyDownHandler(handler);
	// debugComparatorCheckBox.addKeyDownHandler(handler);
	appListBox.addKeyDownHandler(handler);

	setStyleName("loginPanel");
	setSize("326px", "218px");
    }

    public Button getSignInButton() {
	return signInButton;
    }

    public void setSignInButton(Button signInButton) {
	this.signInButton = signInButton;
    }

    public PasswordTextBox getTextBoxPassword() {
	return textBoxPassword;
    }

    public void setTextBoxPassword(PasswordTextBox textBoxPassword) {
	this.textBoxPassword = textBoxPassword;
    }

    public TextBox getTextBoxUsername() {
	return textBoxUsername;
    }

    public void setTextBoxUsername(TextBox textBoxUsername) {
	this.textBoxUsername = textBoxUsername;
    }

    public CheckBox getRememberMeOnCheckBox() {
	return rememberMeOnCheckBox;
    }

    public void setRememberMeOnCheckBox(CheckBox rememberMeOnCheckBox) {
	this.rememberMeOnCheckBox = rememberMeOnCheckBox;
    }

    public ListBox getAppListBox() {
	return appListBox;
    }

    public void setAppListBox(ListBox appListBox) {
	this.appListBox = appListBox;
    }

    /*
     * public CheckBox getDebugComparatorCheckBox() { return
     * debugComparatorCheckBox; }
     * 
     * public void setDebugComparatorCheckBox(CheckBox debugComparatorCheckBox)
     * { this.debugComparatorCheckBox = debugComparatorCheckBox; }
     */

}
