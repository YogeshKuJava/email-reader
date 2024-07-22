package email.reader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class ApiService {

	private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

	public void callExternalApiAsync(String url, String subject) {
		AsyncHttpClient client = new DefaultAsyncHttpClient();
		try {

			logger.info("in message " + subject + " for url " + url + " Respose is ");
			// Prepare the request
			Request request = client.preparePost("https://www.virustotal.com/api/v3/urls")
					.addHeader("accept", "application/json")
					.addHeader("x-apikey", "86138a91c63d79dcaa45a30e1a912f8264603e9986282339706df2d9fe2fb21f")
					.addHeader("content-type", "application/x-www-form-urlencoded").setBody("url=" + url).build();

			// Execute the request asynchronously
			ListenableFuture<Response> futureResponse = client.executeRequest(request);

			// Get the response and store it in a variable
			String responseBody = "";
			int responseStatusCode = 200;
			responseStatusCode = futureResponse.get().getStatusCode();
			// Print the response
			client.close();
			
			responseBody = futureResponse.get().getResponseBody().toString();
			JSONObject jsonObject = new JSONObject(responseBody);
			JSONObject data = jsonObject.getJSONObject("data");
		    String id = data.getString("id");
		    logger.info("id"+id);
		    String [] idSplit = id.split("-");
		    String scanId = idSplit[1] + "-" + idSplit[2];
		    
		    HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(URI.create("https://www.virustotal.com/vtapi/v2/url/report?apikey=86138a91c63d79dcaa45a30e1a912f8264603e9986282339706df2d9fe2fb21f&resource="+scanId))
					.method("GET", HttpRequest.BodyPublishers.noBody())
					.build();
			HttpResponse<String> httpResponse = null;
			try {
				httpResponse = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    
			logger.info("responseBody::: " + httpResponse.body());
			logger.info("Respose Status code " + responseStatusCode);
			client.close();
			if (responseStatusCode == 200) {
				logger.info("Valid URL : " + url + " in message " + subject, "Message");
			} else {
				JOptionPane.showMessageDialog(null, "Not a Valid URL : " + url + " in message " + subject, "Message",
						JOptionPane.WARNING_MESSAGE);

			}

		} catch (InterruptedException | ExecutionException | IOException e) {
			logger.error("Exception in calling virustotal api " + e.getMessage() + " Cause : " + e.getCause());
		}
	}

	public void validateURLS(List<String> urlList, String subject) {
		for (String url : urlList) {
			callExternalApiAsync(url, subject);
		}
	}
}