package curso.api.rest.service;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class ServiceEnviaEmail {
	
	private String userName = "danilojavaweb@gmail.com";
	private String senha = "program254";
	
	public void enviarEmail(String assunto, String emailDestino, String mensagem) throws Exception {
		
		Properties properties = new Properties();
		properties.put("mail.smtp.ssl.trust", "*");
		properties.put("mail.smtp.auth", "true"); /* Autorização */
		properties.put("mail.smtp.starttls", "true"); /* Autenticação */
		properties.put("mail.smtp.host", "smtp.gmail.com"); /* servidor do  google */
		properties.put("mail.smtp.port", "465"); /* Porta do servidor */
		properties.put("mail.smtp.socketFactory.port", "465"); /* Expecifica porta socket */
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); /* Class de conexão socket */
		
		Session session = Session.getInstance(properties, new Authenticator() {
			
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				
				return new PasswordAuthentication(userName, senha);
			}
			
		});
		
		Address[] toUser = InternetAddress.parse(emailDestino);
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(userName)); /* Quem está enviando - Nós */
		message.setRecipients(Message.RecipientType.TO, toUser); /* Para quem vai o email - Quem irá receber */
		message.setSubject(assunto); /* Assunto do Email */
		message.setText(mensagem);
		
		Transport.send(message);
	}
	
}
