import java.util.Date;

public class UserData {
    String databaseId;
    String username;

    Location currentLocation;
    Date updateDate;

    public UserData(String databaseId, String username, Location currentLocation, Date updateDate) {
        this.databaseId = databaseId;
        this.username = username;
        this.currentLocation = currentLocation;
        this.updateDate = updateDate;
    }
}
