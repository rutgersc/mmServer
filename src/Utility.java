public class Utility {

    public static boolean isPlayerNearby(Location firstLocation, Location secondLocation) {

        float distance = firstLocation.distanceTo(secondLocation);
        return (distance < 9001); //TODO: Change maximum distance value
    }
}
