package org.inria.websmatch.gwt.spreadsheet.server;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.inria.websmatch.gwt.spreadsheet.client.ReportingService;
import org.inria.websmatch.utils.L;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ReportingServiceImpl extends RemoteServiceServlet implements ReportingService {

    /**
     * 
     */
    private static final long serialVersionUID = -2430067241660406148L;

    private String useReporting;
    private String reportTo;
    private String smtp;

    public void init() {
	useReporting = getServletContext().getInitParameter("useReporting");
	reportTo = getServletContext().getInitParameter("reportTo");
	smtp = getServletContext().getInitParameter("smtp");
    }

    @Override
    public void insertReport(String user, String fileName, String description, boolean ccDetectionPb, boolean attrDetectionPb) {

	/*if (useReporting.equals("true") && (ccDetectionPb == true || attrDetectionPb == true || !description.equals(""))) {

	    int intCcDetect = (ccDetectionPb) ? 1 : 0;
	    int intMetaDetect = (attrDetectionPb) ? 1 : 0;

	    MySQLDBConnector connector = new MySQLDBConnector();

	    connector.connect();

	    Statement stat = connector.getStat();

	    try {
		stat.executeUpdate("INSERT INTO reports VALUES ('" + user + "','" + fileName + "','" + description + "','" + intCcDetect + "','"
			+ intMetaDetect + "',CURRENT_TIMESTAMP)");
		stat.close();
	    } catch (SQLException e) {
		L.Error(e.getMessage(),e);
	    }

	    connector.close();
	}*/

	if (useReporting.equals("true") && reportTo != null && smtp != null && (ccDetectionPb == true || attrDetectionPb == true || !description.equals(""))) {
	    try {
		// send mail
		Properties prop = System.getProperties();
		prop.put("mail.smtp.host", smtp);
		Session session = Session.getDefaultInstance(prop, null);
		Message message = new MimeMessage(session);
		// message.setFrom(new InternetAddress(reportTo));
		InternetAddress[] internetAddresses = new InternetAddress[1];
		internetAddresses[0] = new InternetAddress(reportTo);
		message.setRecipients(Message.RecipientType.TO, internetAddresses);
		message.setSubject("Report from " + user);
		message.setText("Problem with file " + fileName + "\nProblem with ccs : " + ccDetectionPb + "\nProblem with metadata detection : "
			+ attrDetectionPb + "\nDescription : " + description);
		message.setHeader("X-Mailer", "Java");
		message.setSentDate(new Date());
		Transport.send(message);
	    } catch (MessagingException e) {
		L.Error(e.getMessage(),e);
	    }
	}
    }
}
