package minor.matchmaker;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import minor.Location;

public class PlayerData {
    public int databaseId;
    public String username;
    public Timestamp lastLoginTimestamp;
    public int totalGamesPlayed;
    public int totalWins;
    public String hashedPassword;
    public String sessionUUID;
    public boolean isGuest;

    Location location;
    Date updateDate;

    public PlayerData() {
        this("", new Location(""), new Date());
    }
    public PlayerData(String username, Location currentLocation, Date updateDate) {
        this.username = username;
        this.location = currentLocation;
        this.updateDate = updateDate;
    }
}
