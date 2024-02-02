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
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.MimeBodyPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.NotFoundException;
import com.sun.mail.imap.IMAPFolder;

public class EmailListener extends MessageCountAdapter {
	private Session session;
	private String username;
	private String password;

	private static final Logger logger = LoggerFactory.getLogger(EmailListener.class);

	public EmailListener(Session session, String username, String password) {
		this.session = session;
		this.username = username;
		this.password = password;
	}

	public void startListening() throws MessagingException, InterruptedException, IOException {
		Store store = session.getStore("imaps");
		store.connect(username, password);

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
						logger.info("New email received Subject: " + message.getSubject());
						logger.info("AllRecipients : " + message.getAllRecipients());
						logger.info("New email From " + message.getFrom());

						try {
							if (message.getContent() instanceof String) {
								String bodyText = extractTextFromMessage(message);
								List<String> urlListFromMessageBody = readMessageBodyText(bodyText);
								logger.info("Email Body:\n" + bodyText);

							} else if (message.getContent() instanceof Multipart) {
								Multipart multipart = (Multipart) message.getContent();
								System.out.println("multipart.getCount()" + multipart.getCount());
								for (int i = 0; i < multipart.getCount(); i++) {
									System.out.println(" i value " + i);
									BodyPart bodyPart = multipart.getBodyPart(i);

									if (bodyPart.getDisposition() != null) {
										String bodyText = extractTextFromMessage(message);
										logger.info("====>>>" + bodyText + "<<<=====");

										List<String> urlListFromMessageBody = readMessageBodyText(bodyText);
										if (urlListFromMessageBody.size() > 0) {
											// validateURL(urlListFromMessageBody);
										}
									}
									if (bodyPart.getDisposition() != null) {// For attachment
										readStoreAttachment(bodyPart);
									}
								}
							}

						} catch (IOException | NotFoundException e) {
						   logger.error("Exception in EmailListener.startListening"+e.getMessage()+ " Cause : "+e.getCause());						
						}
					} catch (MessagingException e) {
						   logger.error("Exception in EmailListener.startListening"+e.getMessage()+ " Cause : "+e.getCause());						
					}
				}
			}

		});

		// Start the IDLE Loop
		while (!Thread.interrupted()) {
			try {
				logger.info("Starting IDLE");
				inbox.idle();
			} catch (MessagingException e) {
				logger.info("Messaging exception during IDLE");				
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
			logger.debug("Found URL: " + url);
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
			logger.debug("The file is an image.");
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
				if (bodyPart.isMimeType("text/plain")) {
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
			if (bodyPart.isMimeType("text/plain")) {
				textContent.append(bodyPart.getContent());
			} else if (bodyPart.getContent() instanceof Multipart) {
				// Recursively extract text from nested multipart
				textContent.append(extractTextFromMultipart((Multipart) bodyPart.getContent()));
			}
		}

		return textContent.toString();
	}
	/*
	 * String validateURL(List<String> urlList) throws IOException { for (String url
	 * : urlList) { AsyncHttpClient client = new DefaultAsyncHttpClient();
	 * client.prepare("POST",
	 * "https://www.virustotal.com/api/v3/urls").setHeader("accept",
	 * "application/json") .setHeader("x-apikey",
	 * "86138a91c63d79dcaa45a30e1a912f8264603e9986282339706df2d9fe2fb21f")
	 * .setHeader("content-type",
	 * "application/x-www-form-urlencoded").setBody("url=" + url).execute()
	 * .toCompletableFuture().thenAccept(System.out::println).join();
	 * client.close(); } return " URLS Tested";
	 * 
	 * }
	 */
}