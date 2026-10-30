package lab3.dto;
import java.util.List;
public record FinalResult(Location location, Weather weather, List<PlaceInfo> places) {}