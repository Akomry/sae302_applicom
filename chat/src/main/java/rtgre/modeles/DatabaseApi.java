package rtgre.modeles;

import javax.xml.transform.Result;
import java.io.File;
import java.sql.*;
import java.util.UUID;
import org.sqlite.JDBC;

import static rtgre.chat.ChatApplication.LOGGER;

public class DatabaseApi {
    private Connection con;
    private Statement stmt;

    public DatabaseApi() {
        try {
            this.con = DriverManager.getConnection("jdbc:sqlite:target/dbase.db");
            this.stmt = con.createStatement();
            initDB(con);
            LOGGER.info("Database connected!");
            } catch (SQLException e) {
            LOGGER.severe("Can't connect to database! \n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initDB(Connection con) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS `posts` ("
                    + "	`id` text PRIMARY KEY NOT NULL,"
                    + "	`timestamp` long,"
                    + "	`from` text,"
                    + " `to` text,"
                    + " `body` text"
                    + ");";
            stmt = con.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.severe("Cannot initialize database!");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public ResultSet getPostById(UUID uuid) {
        String query = "SELECT * FROM posts WHERE id = " + uuid.toString();
        try {
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.severe("Can't get post by id!");
            return null;
        }
    }

    public ResultSet getPostsSince(long timestamp) {
        String query = "SELECT * FROM posts WHERE timestamp >= " + timestamp + "ORDER BY timestamp DESC";
        try {
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.severe("Can't get post since " + timestamp + "!");
            return null;
        }
    }

    public ResultSet getPosts() {
        String query = "SELECT * FROM posts";
        try {
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.severe("Can't get posts!");
            return null;
        }
    }

    public boolean addPost(Post post) {
        String query = "INSERT INTO posts VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, post.getId().toString());
            pstmt.setLong(2, post.getTimestamp());
            pstmt.setString(3, post.getFrom());
            pstmt.setString(4, post.getTo());
            pstmt.setString(5, post.getBody());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.severe("Can't add post!");
            return false;
        }
    }

    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            LOGGER.severe("Can't close database connection! Is database connected ?");
        }
    }

}
