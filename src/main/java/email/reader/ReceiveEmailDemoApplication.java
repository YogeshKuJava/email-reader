package email.reader;

import java.io.IOException;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReceiveEmailDemoApplication implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(ReceiveEmailDemoApplication.class);

	@Autowired
	EmailListener emailListener;

	public static void main(String[] args) {
		SpringApplication.run(ReceiveEmailDemoApplication.class, args);

	}

	@Override
	public void run(String... args) {
		// ApplicationContext context = new
		// AnnotationConfigApplicationContext(EmailConfiguration.class);
		// EmailListener emailListener = context.getBean(EmailListener.class);
		try {
			emailListener.startListening();
		} catch (MessagingException e) {
			logger.error("Exception in ReceiveEmailDemoApplication.run " + e.getMessage() + " Cause : " + e.getCause());
		} catch (InterruptedException e) {
			logger.error("Exception in ReceiveEmailDemoApplication.run " + e.getMessage() + " Cause : " + e.getCause());
		} catch (IOException e) {
			logger.error("Exception while Listening in ReceiveEmailDemoApplication.run " + e.getMessage() + " Cause : "
					+ e.getCause());
		}
	}
}