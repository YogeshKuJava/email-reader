package email.reader;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.imap.IMAPFolder;

public class KeepAliveRunnable implements Runnable {
	
    private static final Logger logger = LoggerFactory.getLogger(KeepAliveRunnable.class);
    
    private static final long KEEP_ALIVE_FREQ = 300000; // 5 minutes
    private IMAPFolder folder;
    public KeepAliveRunnable(IMAPFolder folder) {
        this.folder = folder;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(KEEP_ALIVE_FREQ);

                // Perform a NOOP to keep the connection alive
                logger.info("Performing a NOOP to keep the connection alive");
                folder.doCommand(protocol -> {
                    protocol.simpleCommand("NOOP", null);
                    return null;
                });
            } catch (InterruptedException e) {
            	logger.error("Unexpected exception while keeping alive the IDLE connection "+e.getMessage()+" Cause : "+e.getCause());
            } catch (MessagingException e) {
                // Shouldn't really happen...
            	logger.error("Unexpected exception while keeping alive the IDLE connection "+e.getMessage()+" Cause : "+e.getCause());
                //e.printStackTrace();
            }
        }
    }
}