package lab3.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab3.dto.PlaceInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class DescriptionClient {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static CompletableFuture<PlaceInfo> get(int pageId, String title) {
        String url = "https://ru.wikipedia.org/w/api.php?action=query&prop=extracts&exintro=true&explaintext=true&pageids=" + pageId + "&format=json";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "NT-lab3/1.0 (student lab; https://github.com/frankyshelloy) Java-HttpClient")
                .GET().build();


        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    try {
                        var node = MAPPER.readTree(json).at("/query/pages/" + pageId);
                        String desc = node.has("extract") ? node.get("extract").asText() : "Нет описания";
                        return new PlaceInfo(title, desc);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}