package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.inria.websmatch.gwt.spreadsheet.client.composites.DetectionQualityComposite;
import org.inria.websmatch.gwt.spreadsheet.client.composites.Login;
import org.inria.websmatch.gwt.spreadsheet.client.composites.MainFrame;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SpreadsheetComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.WaitingPopup;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @todo Use decorated tabpanel with animated true
 */
public class SpreadsheetViewer implements EntryPoint {

	// the services RPC
	private LoginServiceAsync loginService = (LoginServiceAsync) GWT.create(LoginService.class);

	private String sid = null;
	public static String username = null;

	private VerticalPanel horizontalPanel;

	private VerticalPanel verticalPanel;

	private Label welcomeToMyLabel;

	private Login login;

	private boolean compMode = false;

	public static boolean _MONGO = true;
	// public static String _MDB = "test";

	// public static String providerUri = "http://websmatch.lirmm.fr/dataprovider";
	// public static String providerUri = "http://localhost:8320";
	public static String providerUri = "http://constraint.lirmm.fr:8320";

	public void onModuleLoad() {

		// if we are on datapublica mode
		String datapublica = Window.Location.getParameter("datapublica");
		String fileName = Window.Location.getParameter("fileName");
		String user_id = Window.Location.getParameter("user_id");
		String callback_url = Window.Location.getParameter("callback_url");
		String doc_url = Window.Location.getParameter("doc_url");
		String crawl_id = Window.Location.getParameter("crawl_id");
		String publication_id = Window.Location.getParameter("publication_id");
		String post_url = Window.Location.getParameter("post_url");
		String with_data = Window.Location.getParameter("with_data");


		if(datapublica != null && datapublica.equals("true")){

			GWT.log("callback_url : "+callback_url);
			GWT.log("post_url : "+post_url);
			GWT.log("datapublica : "+datapublica);
			GWT.log("File : "+fileName);

			RootLayoutPanel rootPanel = RootLayoutPanel.get();
			rootPanel.setStyleName("body");
			rootPanel.setSize("100%", "100%");

			// add the spreadsheet table
			FlowPanel fp = new FlowPanel();

			SpreadsheetComposite spreadsheet = new SpreadsheetComposite(true,fileName,true,false,null);
			spreadsheet.setUsername(user_id);

			//
			HashMap<String,String> dp = new HashMap<>();
			dp.put( "post_url",post_url);
			dp.put( "callback_url",callback_url);
			dp.put( "doc_url",doc_url);
			dp.put( "crawl_id",crawl_id);
			dp.put( "publication_id",publication_id);
			dp.put( "user_id",user_id);
			dp.put("fileName", fileName);
			dp.put("with_data", with_data);
			spreadsheet.setMetas(dp);
			//

			fp.add(spreadsheet);

			rootPanel.add(fp);

			WaitingPopup.getInstance().show();
			spreadsheet.parseSpreadsheet(fileName, true,null);

		}

		else{
			RootLayoutPanel rootPanel = RootLayoutPanel.get();
			rootPanel.setStyleName("body");
			rootPanel.setSize("100%", "100%");

			// set with the cookie
			if (sid == null)
				sid = Cookies.getCookie("spreadsheetviewer_sid");
			if (username == null)
				username = Cookies.getCookie("spreadsheetviewer_user");

			// if not already logged, log in please
			if (sid == null) {
				horizontalPanel = new VerticalPanel();
				horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				rootPanel.add(horizontalPanel);
				rootPanel.setWidgetTopHeight(horizontalPanel, 0.0, Unit.PX, 800.0, Unit.PX);
				horizontalPanel.setSize("100%", "100%");

				verticalPanel = new VerticalPanel();
				horizontalPanel.add(verticalPanel);
				verticalPanel.setWidth("100%");

				welcomeToMyLabel = new Label("WebSmatch login page");
				welcomeToMyLabel.setStyleName("loginPanel-Title");
				welcomeToMyLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				welcomeToMyLabel.setSize("100%", "32px");
				verticalPanel.add(welcomeToMyLabel);

				login = new Login(new String[] { "" });
				completeLogin();

			}

			else {

				showUserInfos();

				final MainFrame f = new MainFrame(username, sid);
				rootPanel.add(f);

				// load files
				WaitingPopup.getInstance().show();

				DetectionQualityServiceAsync storeService = GWT.create(DetectionQualityService.class);

				storeService.getDetectectionQualityList(false, username, new AsyncCallback<List<DetectionQualityData>>() {

					@Override
					public void onFailure(Throwable caught) {
						WaitingPopup.getInstance().hide();
						Window.alert("Can't load files.");
					}

					@Override
					public void onSuccess(List<DetectionQualityData> result) {

						DetectionQualityComposite detec = new DetectionQualityComposite(result, f);
						detec.setWidth("100%");
						detec.setHeight(f.getOffsetHeight() - 70 + "px");
						f.setMainWidget(detec);

						WaitingPopup.getInstance().hide();
					}
				});
				//
			}

	/*    }
	};

	VisualizationUtils.loadVisualizationApi(onLoadCallback, OrgChart.PACKAGE);*/
		}
	}

	private void completeLogin() {

		horizontalPanel.add(login);
		login.setHeight("218px");

		final TextBox textBoxUsername = login.getTextBoxUsername();
		final PasswordTextBox textBoxPassword = login.getTextBoxPassword();
		final VerticalPanel flog = horizontalPanel;
		final SpreadsheetViewer ep = this;

		// add the click handler
		login.getSignInButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (textBoxUsername.getText().length() == 0 || textBoxPassword.getText().length() == 0) {
					Window.alert("Username or password is empty.");
				} else {
					// currentApp =
					// login.getAppListBox().getItemText(login.getAppListBox().getSelectedIndex());
					// remove the widget
					flog.removeFromParent();
					// login
					login(textBoxUsername.getText(), textBoxPassword.getText(), login.getRememberMeOnCheckBox().getValue(), ep);
				}
			}
		});

		Grid logos = new Grid(1, 4);
		logos.setCellSpacing(100);
		logos.setWidget(0, 0, new Image(GfxRessources.INSTANCE.logo()));
		logos.setWidget(0, 1, new Image(GfxRessources.INSTANCE.lirmm()));
		logos.setWidget(0, 2, new Image(GfxRessources.INSTANCE.UM2()));
		logos.setWidget(0, 3, new Image(GfxRessources.INSTANCE.ibc()));
		logos.addStyleName("centerFp");

		horizontalPanel.add(logos);
	}

	// methods using the rpc services
	private void login(final String userName, String pass, final boolean rememberMe, final EntryPoint ep) {
		// login service
		loginService.login(userName, pass, new AsyncCallback<String>() {

			@Override
			public void onSuccess(String result) {
				// set the root values
				sid = result;
				username = userName;
				if (sid != null) {

					if (rememberMe) {
						final long DURATION = 1000 * 60 * 60 * 24 * 1; // duration
						// 1
						// day

						Date expires = new Date(System.currentTimeMillis() + DURATION);
						Cookies.setCookie("spreadsheetviewer_sid", sid, expires, null, "/", false);
						Cookies.setCookie("spreadsheetviewer_user", userName, expires, null, "/", false);
					}
				}

				ep.onModuleLoad();
			}

			@Override
			public void onFailure(Throwable caught) {
				ep.onModuleLoad();
				Window.alert("Login service error. Please contact the administrator.\nError: "+caught.getMessage());
			}
		});
	}

	private void showUserInfos() {
		GWT.log("Client app sid : " + sid);
		GWT.log("Client app user : " + username);
	}

	public boolean isCompMode() {
		return compMode;
	}

}
