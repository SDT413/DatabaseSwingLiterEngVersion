import java.sql.*;

public class SQL_Connection {
    private Connection connection;
    private String query;
    private Statement statement;
    private ResultSet resultSet;
    private ResultSet tablesSet;

    public SQL_Connection() {
        try {
            // Load the JDBC driver.
            Class.forName("org.sqlite.JDBC");
            // Connect to the database.
           connection = DriverManager.getConnection("jdbc:sqlite:test.db");
           tablesSet = connection.createStatement().executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void DoQuery() throws SQLException {
        statement = connection.createStatement();
        resultSet = statement.executeQuery(query);
    }
    public void SDQuery(String query) throws SQLException {
        this.query = query;
        statement = connection.createStatement();
        resultSet = statement.executeQuery(query);
    }
    public void DoUpdate() throws SQLException {
        statement = connection.createStatement();
        statement.executeUpdate(query);
    }
    public void SDUpdate(String query) throws SQLException {
        this.query = query;
        statement = connection.createStatement();
        statement.executeUpdate(query);
    }
    public ResultSet getResultSet() {
        return resultSet;
    }
    public ResultSet getTablesSet() throws SQLException {
       return tablesSet;
    }
    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }
    public ResultSet CreateResultSet(String query) throws SQLException {
       return connection.createStatement().executeQuery(query);
    }
    public PreparedStatement CreatePreparedStatement(String query) throws SQLException {
       return connection.prepareStatement(query);
    }
    public void close() throws SQLException {
        connection.close();
    }
}
