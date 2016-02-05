package org.inria.websmatch.gwt.spreadsheet.server;

import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.inria.websmatch.db.MySQLDBConfLoader;
import org.inria.websmatch.gwt.spreadsheet.client.LoginService;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.Sha1Utils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {

	/**
	 *
	 */
	private static final long serialVersionUID = 3085693328785165286L;
	private String user = "dp_admin";
	private String pass = "dp_pass";
	// Inria private String dbHost = "localhost";
	private String dbHost;// ="193.49.106.32";
	private int dbPort;// = 3306;
	private String dbName = "datapublica";

	// added for security
	public static HashMap<String, Integer> groupIds = new HashMap<>();

	@Override
	public String login(String username, String password) {
		if(true) return "debug";  // TODO: remove

		if(username != null){
			L.Debug(this.getClass().getSimpleName(),"Trying to login with username "+username,true);
		}

		if(password == null) return null;

		String sid = null;
		Integer user_id = null;

		// just try to see if user is in db with the good password
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			L.Error(e.getMessage(),e);
		}
		java.sql.Connection conn = null;
		try {

			// load conf
			MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

			dbHost = loader.getDbHost();
			dbPort = loader.getDbPort();

			conn = DriverManager.getConnection(
					"jdbc:mysql://" + this.getDbHost() + ":" + this.getDbPort()
							+ "/" + this.dbName, user, pass);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			L.Error(e.getMessage(),e);
		}

		/*java.sql.Statement stat = null;
	    try {
		stat = conn.createStatement();
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		L.Error(e.getMessage(),e);
	    }*/

		// firstly, we have to see if this file has already an id
		// String query = "SELECT password, id_user FROM users WHERE user_name ='" + username + "';";

		try {

			java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT password, id_user FROM users WHERE user_name = ? ;");

			pstmt.setString(1, username);

			// ResultSet result = stat.executeQuery(query);
			ResultSet result = pstmt.executeQuery();

			if(result.next()){
				try {
					{
						String hashed = result.getString("password");
						if(hashed.equals(Sha1Utils.hashToString(Sha1Utils.getHash(password)))){

							// ok set the sid
							// servlet usage
							if(this.getThreadLocalRequest() != null){
								HttpServletRequest request = this.getThreadLocalRequest();
								HttpSession session = request.getSession();
								sid = session.getId();
								user_id = result.getInt("id_user");
							}
							// other
							else{
								user_id = result.getInt("id_user");
								sid = username;
							}

						}
					}
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					L.Error(e.getMessage(),e);
				}
			}
			if(pstmt != null) pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			L.Error(e.getMessage(),e);
		}

		// then get the group
		if(sid != null){

			// query = "SELECT id_group FROM matching_results.user_grants WHERE id_user ='" + user_id + "';";
			try {

				java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT id_group FROM matching_results.user_grants WHERE id_user = ? ;");
				pstmt.setInt(1, user_id);

				//ResultSet result = stat.executeQuery(query);
				ResultSet result = pstmt.executeQuery();

				if(result.next()){
					// ok set the the group
					groupIds.put(sid,result.getInt("id_group"));
					// System.out.println("Group id : "+result.getInt("id_group"));		    
				}

				if(pstmt != null) pstmt.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				L.Error(e.getMessage(),e);
			}

		}

		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				L.Error(e.getMessage(),e);
			}
		}

		if(sid != null) L.Debug(this.getClass().getSimpleName(), username + " logged successfully.",true);

		return sid;

	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbPort(int dbPort) {
		this.dbPort = dbPort;
	}

	public int getDbPort() {
		return dbPort;
	}
}
