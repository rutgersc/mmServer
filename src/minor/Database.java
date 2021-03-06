package minor;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import minor.matchmaker.PlayerData;
import java.sql.*;
import java.util.UUID;

public class Database implements IntervalDatabase {

    String user;
    String password;
    String serverName;
    String databaseName;
    String portNumber = "3306";

    MysqlDataSource dataSource;

    public Database(String user, String password, String serverName, String databaseName) {
        this.user = user;
        this.password = password;
        this.serverName = serverName;
        this.databaseName = databaseName;

        dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName(serverName);
        dataSource.setPortNumber(3306);
        dataSource.setDatabaseName(databaseName);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        Connection connection;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData mData = connection.getMetaData();
            System.out.println("Server name: "  + mData.getDatabaseProductName());
            System.out.println("Server version: "  + mData.getDatabaseProductVersion());
            connection.close();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public MysqlDataSource getDataSource() {
        return dataSource;
    }

    public PlayerData getUser(String name) throws SQLException {

        PlayerData playerData;
        String sql =
                "select id, name, lastLogin, totalGamesPlayed, wins, sessionUUID " +
                "from user " +
                "where `name` like '"+ name +"'";

        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement stmt = c.prepareStatement(sql);  ResultSet rs = stmt.executeQuery()) {
                rs.first();
                playerData = new PlayerData();
                playerData.databaseId = rs.getInt(1);
                playerData.username = rs.getString(2);
                playerData.lastLoginTimestamp = rs.getTimestamp(3);
                playerData.totalGamesPlayed = rs.getInt(4);
                playerData.totalWins = rs.getInt(5);
                playerData.sessionUUID = rs.getString(6);
            }
        }

        return playerData;
    }

    public boolean checkPassword(String password, PlayerData playerData) throws SQLException {

        boolean correctPassword;
        String sql =
                "select password from user " +
                "where id='" + playerData.databaseId + "'";

        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement stmt = c.prepareStatement(sql);  ResultSet rs = stmt.executeQuery()) {
                rs.first();
                String databasePassword = rs.getString(1);
                correctPassword = (password.equals(databasePassword));
            }
        }

        return correctPassword;
    }

    public void setUUID(String uuid, PlayerData playerData) throws SQLException {

        if(playerData.isGuest)
            return;

        if(playerData.sessionUUID.equals(uuid))
            return;

        String sql =
                "update user " +
                "set sessionUUID = '" + uuid + "' " +
                "where id=" + playerData.databaseId;

        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement stmt = c.prepareStatement(sql);  ) {
                int rs = stmt.executeUpdate();
            }
        }
    }

    public static void main(String [ ] args)
    {
        Database db = new Database("root", "test", "192.168.1.108", "interval");
        try {
            PlayerData user = db.getUser("rutger");

            String newUUID = UUID.randomUUID().toString();
            System.out.println("Hallo " + newUUID);
            db.setUUID(newUUID, user);

            //boolean isValid = db.checkPassword("test", user);
            //boolean isValid2 = db.checkPassword("test2", user);

            System.out.println( "..." );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
