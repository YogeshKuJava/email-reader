package email.reader;

import java.io.IOException;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class ReceiveEmailDemoApplication implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(ReceiveEmailDemoApplication.class);

	@Autowired
	EmailListener emailListener;

	public static void main(String[] args) {
	    ConfigurableApplicationContext context = new SpringApplicationBuilder(ReceiveEmailDemoApplication.class).headless(false).run(args);
	}

	@Override
	public void run(String... args) {
		try {
			emailListener.startListening();
		} catch (MessagingException e) {
			logger.error("Exception in run " + e.getMessage() + " Cause : " + e.getCause());
		} catch (InterruptedException e) {
			logger.error("Exception in run " + e.getMessage() + " Cause : " + e.getCause());
		} catch (IOException e) {
			logger.error("Exception while Listening in run " + e.getMessage() + " Cause : " + e.getCause());
		}
	}
}