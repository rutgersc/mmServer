package minor;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import minor.matchmaker.PlayerData;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.UUID;

/**
 * Created by Rutger on 12-01-2015.
 */
public class OfflineDatabase implements IntervalDatabase {

    public OfflineDatabase() {
    }


    @Override
    public MysqlDataSource getDataSource() {
        return null;
    }

    @Override
    public PlayerData getUser(String name) throws SQLException {

        PlayerData playerData = new PlayerData();
        // playerData.databaseId = rs.getInt(1);
        playerData.username = name;
        playerData.lastLoginTimestamp = new Timestamp(111);
        playerData.totalGamesPlayed = 0;
        playerData.totalWins = 0;
        playerData.sessionUUID = UUID.randomUUID().toString();

        return playerData;
    }

    @Override
    public boolean checkPassword(String password, PlayerData playerData) throws SQLException {
        return true;
    }

    @Override
    public void setUUID(String uuid, PlayerData playerData) throws SQLException {
    }
}
