package email.reader;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.concurrent.CompletableFuture;

@Service
public class ApiService {

    private final WebClient webClient;

    public ApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.example.com").build();
        
    }

    public CompletableFuture<String> callExternalApiAsync() {
        return webClient.get()
                .uri("/data")
                .retrieve()
                .bodyToMono(String.class)
                .toFuture();
    }
}