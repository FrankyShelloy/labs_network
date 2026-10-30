package lab3.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab3.Config;
import lab3.dto.Weather;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class WeatherClient {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static CompletableFuture<Weather> get(double lat, double lon) {
        String url = String.format(Locale.US,
                "https://api.openweathermap.org/data/2.5/weather"
                        + "?lat=%f&lon=%f&units=metric&appid=%s",
                lat, lon, Config.openWeatherKey());
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(WeatherClient::checkedBody)
                .thenApply(WeatherClient::parse);
    }

    private static String checkedBody(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenWeather: HTTP " + response.statusCode()
                    + " " + response.body());
        }
        return response.body();
    }

    private static Weather parse(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            double temp = root.at("/main/temp").asDouble();
            double wind = root.at("/wind/speed").asDouble();
            return new Weather(temp, wind);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}