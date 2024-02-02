package email.reader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ApiService {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

	private final WebClient webClient;

	public ApiService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("https://www.virustotal.com").build();

	}

	public CompletableFuture<String> callExternalApiAsync(String url) {
		System.out.println("Coming here .............................1111111111111111"+url);
		return webClient.post().uri("/api/v3/urls").header("accept", "application/json")
				.header("x-apikey", "86138a91c63d79dcaa45a30e1a912f8264603e9986282339706df2d9fe2fb21f")
				.header("content-type", "application/x-www-form-urlencoded").bodyValue("url=" + url).retrieve()
				.bodyToMono(String.class).toFuture();
	}
	
	public void validateURLS(List<String> urlList) {
		for(String url: urlList) {
			System.out.println("Coming here .............................222222222222222222222222-size ->"+urlList.size());

			logger.info(url, callExternalApiAsync(url).thenAccept(result -> System.out.println("Result of url : "+url+ "is : -->" + result)));
		}
	}
}