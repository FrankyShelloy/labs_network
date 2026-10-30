package lab3;

import lab3.api.DescriptionClient;
import lab3.api.LocationClient;
import lab3.api.PlacesClient;
import lab3.api.WeatherClient;
import lab3.dto.FinalResult;
import lab3.dto.Location;
import lab3.dto.Place;
import lab3.dto.PlaceInfo;
import lab3.dto.Weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AsyncApp {

    public static CompletableFuture<FinalResult> getDetails(Location location) {
        CompletableFuture<Weather> weather =
                WeatherClient.get(location.lat(), location.lon());
        CompletableFuture<List<PlaceInfo>> places = loadPlaces(location);

        return weather.thenCombine(places, (w, p) -> new FinalResult(location, w, p));
    }

    private static CompletableFuture<List<PlaceInfo>> loadPlaces(Location location) {
        return PlacesClient.get(location.lat(), location.lon())
                .thenCompose(AsyncApp::loadDescriptions);
    }

    private static CompletableFuture<List<PlaceInfo>> loadDescriptions(List<Place> places) {
        List<CompletableFuture<PlaceInfo>> futures = new ArrayList<>();
        for (Place place : places) {
            futures.add(DescriptionClient.get(place.id(), place.title()));
        }
        return collectAll(futures);
    }

    private static <T> CompletableFuture<List<T>> collectAll(
            List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDone =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
        return allDone.thenApply(done -> resultsOf(futures));
    }

    private static <T> List<T> resultsOf(List<CompletableFuture<T>> futures) {
        List<T> results = new ArrayList<>();
        for (CompletableFuture<T> future : futures) {
            results.add(future.join());
        }
        return results;
    }

    public static void main(String[] args) {
        try {
            run(new Scanner(System.in));
        } catch (CompletionException e) {
            System.out.println("Ошибка: " + e.getCause().getMessage());
        }
    }

    private static void run(Scanner scanner) {
        String query = readQuery(scanner);
        if (query.isEmpty()) {
            System.out.println("Пустой запрос");
            return;
        }

        List<Location> locations = LocationClient.search(query).join();
        if (locations.isEmpty()) {
            System.out.println("Ничего не найдено");
            return;
        }

        printLocations(locations);
        Location chosen = chooseLocation(scanner, locations);

        FinalResult result = getDetails(chosen).join();
        printResult(result);
    }

    private static String readQuery(Scanner scanner) {
        System.out.print("Введите название места: ");
        return scanner.nextLine().trim();
    }

    private static void printLocations(List<Location> locations) {
        System.out.println("\nНайденные локации:");
        for (int i = 0; i < locations.size(); i++) {
            System.out.println((i + 1) + ". " + locations.get(i).name());
        }
    }

    private static Location chooseLocation(Scanner scanner, List<Location> locations) {
        while (true) {
            System.out.print("Выберите номер (1-" + locations.size() + "): ");
            int number = parseNumber(scanner.nextLine().trim());

            if (number >= 1 && number <= locations.size()) {
                return locations.get(number - 1);
            }
            System.out.println("Некорректный номер, попробуйте ещё раз");
        }
    }

    private static int parseNumber(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }



    private static void printResult(FinalResult result) {
        Weather weather = result.weather();
        System.out.println("\nИТОГ");
        System.out.println("Температура: " + weather.temp()
                + "°C, Ветер: " + weather.wind() + " м/с");

        for (PlaceInfo place : result.places()) {
            System.out.println("- " + place.title() + ": "
                    + shorten(place.description(), 100));
        }
    }

    private static String shorten(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}