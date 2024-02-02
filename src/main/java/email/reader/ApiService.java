package email.reader;

import java.io.IOException;
import java.util.List;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

	private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

	public void callExternalApiAsync(String url) {
		AsyncHttpClient client = new DefaultAsyncHttpClient();
		try {
			logger.info("For url " + url + " Respose is ");
			client.prepare("POST", "https://www.virustotal.com/api/v3/urls").setHeader("accept", "application/json")
					.setHeader("x-apikey", "86138a91c63d79dcaa45a30e1a912f8264603e9986282339706df2d9fe2fb21f")
					.setHeader("content-type", "application/x-www-form-urlencoded").setBody("url=" + url).execute()
					.toCompletableFuture().thenAccept(System.out::println).join();

			client.close();
		} catch (IOException e) {
			logger.error("Exception in calling virustotal api " + e.getMessage() + " Cause : " + e.getCause());
		}
	}

	public void validateURLS(List<String> urlList) {
		for (String url : urlList) {
			callExternalApiAsync(url);
		}
	}
}