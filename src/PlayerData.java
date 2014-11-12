import java.util.Date;

public class PlayerData {
    String databaseId;
    String username;

    Location location;
    Date updateDate;

    public PlayerData(String databaseId, String username, Location currentLocation, Date updateDate) {
        this.databaseId = databaseId;
        this.username = username;
        this.location = currentLocation;
        this.updateDate = updateDate;
    }
}
