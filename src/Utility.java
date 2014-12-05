public class Utility {

    public static boolean isPlayerNearby(Location firstLocation, Location secondLocation) {

        float distance = firstLocation.distanceTo(secondLocation);
        return (distance < 9999991); //TODO: Change maximum distance value
    }
}
