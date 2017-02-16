package cloudinary.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

public class SendAttachmentInEmail {
	@Autowired
    private static MessageSource messageSource;
	
	public static void sendMail(String title, String opt, List<String> filePath) throws IOException, URISyntaxException {
		
		final String username = messageSource.getMessage("email.send.adress",null,null);
		final String password = messageSource.getMessage("email.send.password",null,null);

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("from-email@gmail.com"));
			
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(messageSource.getMessage("email.recipient",null,null)));
			message.setSubject(new Formatter().format(messageSource.getMessage("email.title",null,null), title).toString());

			// Create a multipar message
	        Multipart multipart = new MimeMultipart();
			// Create the message part
	        BodyPart messageBodyPart = new MimeBodyPart();

	         // Now set the actual message
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//dd/MM/yyyy
			Date now = new Date();
			String strDate = sdfDate.format(now);
			String emailBody = "Process status \r\n";
			emailBody = emailBody + "Process Date : " + strDate + "\r\n"; 
			emailBody = emailBody + opt;
	         
	        messageBodyPart.setText(emailBody);
	        multipart.addBodyPart(messageBodyPart);
	         
	        for (int i=0; i < filePath.size(); i++) {
				 Path p = Paths.get(filePath.get(i));
				 String fileName = p.getFileName().toString();
				 
				 messageBodyPart = new MimeBodyPart();
				 DataSource source = new FileDataSource(filePath.get(i));
				 messageBodyPart.setDataHandler(new DataHandler(source));
				 messageBodyPart.setFileName(fileName);
				 multipart.addBodyPart(messageBodyPart);
	        }

	         // Send the complete message parts
	         message.setContent(multipart);
			
			Transport.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}