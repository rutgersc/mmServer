package minor;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {

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
            connection.prepareStatement("");
            connection.close();

        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    public boolean checkPassword(){

        String query = "SELECT ";

        Connection connection;

        try {
            connection = dataSource.getConnection();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }


        return true;
    }

    public void setUUID() {

    }
}
