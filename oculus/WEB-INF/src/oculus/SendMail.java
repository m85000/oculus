package oculus;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Send yourself an email from your gmail account 
 *
 * @author brad.zdanivsk@gmail.com
 */
public class SendMail {

	// take this from properties on startup 
	private static final int SMTP_HOST_PORT = 587;
	private static final String SMTP_HOST_NAME = "smtp.gmail.com";
	private static final String SMTP_AUTH_USER = "xxx@gmail.com";
	private static final String SMTP_AUTH_PWD = "xxx";

	/* test driver */
	public static void main(String[] args) throws Exception {
		
		if (sendMessage("Oculus Event", "testing..."))
			System.out.println("email sent");
		else
			System.out.println("email failed, check your settings");
	}

	/**
	 * 
	 * Send yourself an error message from the robot. This method requires a
	 * Gmail user account.
	 * 
	 * @param text
	 *            is the message body to form the email body
	 * @return True if mail was sent successfully
	 */
	public static boolean sendMessage(String sub, String text) {
		try {
			Properties props = new Properties();
			props.put("mail.smtps.host", SMTP_HOST_NAME);
			props.put("mail.smtps.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");

			Session mailSession = Session.getDefaultInstance(props);
			Transport transport = mailSession.getTransport("smtp");
			
			/* turn off on deply */
			mailSession.setDebug(true);

			MimeMessage message = new MimeMessage(mailSession);
			message.setSubject(sub);
			message.setContent(text, "text/plain");
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(SMTP_AUTH_USER));

			transport.connect(SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);
			transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		// all good
		return true;
	}
}