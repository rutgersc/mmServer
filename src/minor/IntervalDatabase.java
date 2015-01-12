package minor;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import minor.matchmaker.PlayerData;

import javax.activation.DataSource;
import java.sql.SQLException;

public interface IntervalDatabase {

    public MysqlDataSource getDataSource();

    public PlayerData getUser(String name) throws SQLException;

    public boolean checkPassword(String password, PlayerData playerData) throws SQLException;

    public void setUUID(String uuid, PlayerData playerData) throws SQLException;
}