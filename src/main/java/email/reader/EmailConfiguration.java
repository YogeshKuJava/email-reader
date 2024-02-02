package email.reader;

import java.util.Properties;

import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class EmailConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EmailConfiguration.class);

	
    @Value("${email.host}")
    private String emailHost;

    @Value("${email.port}")
    private String emailPort;

    @Value("${email.username}")
    private String emailUsername;

    @Value("${email.password}")
    private String emailPassword;

    @Bean
    public Session mailSession() {
        Properties props = new Properties();        
        logger.debug("emailHost---------->>>>>>>>>>"+emailHost);
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", emailHost);
        props.setProperty("mail.imaps.port", emailPort);

        // Create a new session with the properties
        Session session = Session.getInstance(props);
        session.setDebug(false); // Enable debug mode for troubleshooting

        return session;
    }

    @Bean
    public EmailListener emailListener() {
        return new EmailListener(mailSession(), emailUsername, emailPassword);
    }
}