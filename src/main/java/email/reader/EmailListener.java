package email.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.MimeBodyPart;

import com.google.zxing.NotFoundException;
import com.sun.mail.imap.IMAPFolder;

public class EmailListener extends MessageCountAdapter {
	private Session session;
	private String username;
	private String password;

	public EmailListener(Session session, String username, String password) {
		this.session = session;
		this.username = username;
		this.password = password;
	}

	public void startListening() throws MessagingException, InterruptedException, IOException {
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", 993, "yogeshyadcar@gmail.com", "ilpizitgefpdzjby");

		IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
		inbox.open(Folder.READ_WRITE);

		// Create a new thread to keep the connection alive
		Thread keepAliveThread = new Thread(new KeepAliveRunnable(inbox), "IdleConnectionKeepAlive");
		keepAliveThread.start();

		inbox.addMessageCountListener(new MessageCountAdapter() {
			@Override
			public void messagesAdded(MessageCountEvent event) {
				// Process the newly added messages
				Message[] messages = event.getMessages();
				for (Message message : messages) {
					try {
						// Implement your email processing logic here
						System.out.println("New email received Subject: " + message.getSubject());
						System.out.println("AllRecipients : " + message.getAllRecipients());
						System.out.println("New email From " + message.getFrom());

						try {
							if (message.getContent() instanceof String) {
					            String bodyText = extractTextFromMessage(message);
								readMessageBodyText(bodyText);
					            System.out.println("Email Body:\n" + bodyText);
								
							} else if (message.getContent() instanceof Multipart) {
								Multipart multipart = (Multipart) message.getContent();
								System.out.println("multipart.getCount()"+multipart.getCount());
								for (int i = 0; i < multipart.getCount(); i++) {
									System.out.println(" i value "+i);
									BodyPart bodyPart = multipart.getBodyPart(i);
									System.out.println("bodyPart.getContent()------>>>..."+bodyPart.getContent().getClass().getTypeName());
									System.out.println("bodyPart.isMimeType()------>>>..."+bodyPart.getDisposition());

									if (bodyPart.getDisposition() != null) {
							            String bodyText = extractTextFromMessage(message);
							            System.out.println("Email Body:\n" + bodyText);
										readMessageBodyText(bodyText);
									}
									if (bodyPart.getDisposition() != null
											&& bodyPart.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {// For attachment
										readStoreAttachment(bodyPart);
									}
								}							
							}

						} catch (IOException | NotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
			}

		});

		// Start the IDLE Loop
		while (!Thread.interrupted()) {
			try {
				System.out.println("Starting IDLE");
				inbox.idle();
			} catch (MessagingException e) {
				System.out.println("Messaging exception during IDLE");
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		// Interrupt and shutdown the keep-alive thread
		if (keepAliveThread.isAlive()) {
			keepAliveThread.interrupt();
		}
	}

	private static boolean isImage(File file) {
		try {
			// Try to read the image
			ImageIO.read(file);
			return true;
		} catch (IOException e) {
			// IOException will be thrown if the file is not an image or cannot be read
			return false;
		}
	}

	public List<String> readMessageBodyText(String bodyText) throws IOException, MessagingException {
		List<String> urlList = new ArrayList<String>();

		String urlPattern = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		Pattern pattern = Pattern.compile(urlPattern);
		Matcher matcher = pattern.matcher(bodyText);

		// Find and print URLs in the text
		while (matcher.find()) {
			String url = matcher.group();
			System.out.println("Found URL: " + url);
			urlList.add(url);
		}
		return urlList;
	}

	public void readStoreAttachment(BodyPart bodyPart) throws MessagingException, IOException, NotFoundException {

		MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
		System.out.println("Attachment Name: " + mimeBodyPart.getFileName());
		String fileName = "./documents/" + mimeBodyPart.getFileName();
		// Save the attachment to a file
		mimeBodyPart.saveFile(fileName);
		File imageFileName = new File(fileName);
		if (isImage(imageFileName)) {
			System.out.println("The file is an image.");
			new QRExtractor().readQRCode(fileName);
		}
	}
	
    private static String extractTextFromMessage(Message message) throws MessagingException, IOException {
        Object content = message.getContent();

        if (content instanceof Multipart) {
            StringBuilder textContent = new StringBuilder();
            Multipart multipart = (Multipart) content;

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                // Check if the part is text/plain or text/html
                if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/html")) {
                    textContent.append(bodyPart.getContent());
                } else if (bodyPart.getContent() instanceof Multipart) {
                    // Recursively extract text from nested multipart
                    textContent.append(extractTextFromMultipart((Multipart) bodyPart.getContent()));
                }
            }

            return textContent.toString();
        } else if (content instanceof String) {
            return (String) content;
        }

        return "";
    }

    private static String extractTextFromMultipart(Multipart multipart) throws MessagingException, IOException {
        StringBuilder textContent = new StringBuilder();

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            // Check if the part is text/plain or text/html
            if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/html")) {
                textContent.append(bodyPart.getContent());
            } else if (bodyPart.getContent() instanceof Multipart) {
                // Recursively extract text from nested multipart
                textContent.append(extractTextFromMultipart((Multipart) bodyPart.getContent()));
            }
        }

        return textContent.toString();
    }

}