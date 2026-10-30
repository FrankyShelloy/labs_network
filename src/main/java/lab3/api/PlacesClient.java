package lab3.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab3.dto.Place;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class PlacesClient {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static CompletableFuture<List<Place>> get(double lat, double lon) {
        String url = String.format(Locale.US, "https://ru.wikipedia.org/w/api.php?action=query&list=geosearch&gsradius=5000&gscoord=%f%%7C%f&format=json", lat, lon);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "NT-lab3/1.0 (student lab; https://github.com/frankyshelloy) Java-HttpClient")
                .GET().build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                    }
                    return response.body();
                })
                .thenApply(json -> {
                    try {
                        List<Place> places = new ArrayList<>();
                        for (JsonNode node : MAPPER.readTree(json).at("/query/geosearch")) {
                            places.add(new Place(node.get("pageid").asInt(), node.get("title").asText()));
                        }
                        return places;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}