package email.reader;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ReceiveEmailDemoApplication implements CommandLineRunner {

	 public static void main(String[] args) {
	  SpringApplication.run(ReceiveEmailDemoApplication.class, args);
	 }

	 @Override
	 public void run(String... args) throws Exception {
	  ApplicationContext context = new AnnotationConfigApplicationContext(EmailConfiguration.class);
	  EmailListener emailListener = context.getBean(EmailListener.class);
	  emailListener.startListening();
	 }
	}