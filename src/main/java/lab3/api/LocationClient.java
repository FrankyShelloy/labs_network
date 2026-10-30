package lab3.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab3.Config;
import lab3.dto.Location;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocationClient {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static CompletableFuture<List<Location>> search(String query) {
        String url = "https://graphhopper.com/api/1/geocode"
                + "?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&locale=ru&limit=5&key=" + Config.graphHopperKey();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", "JavaApp").GET().build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    try {
                        List<Location> locations = new ArrayList<>();
                        for (JsonNode hit : MAPPER.readTree(json).path("hits")) {
                            String name = Stream.of(
                                            hit.path("name").asText(""),
                                            hit.path("street").asText(""),
                                            hit.path("district").asText(""),
                                            hit.path("city").asText(""))
                                    .filter(s -> !s.isBlank())
                                    .collect(Collectors.joining(", "));
                            locations.add(new Location(
                                    name,
                                    hit.at("/point/lat").asDouble(),
                                    hit.at("/point/lng").asDouble()));
                        }
                        return locations;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}