package rtgre.modeles;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

import org.sqlite.JDBC;

import static rtgre.chat.ChatApplication.LOGGER;

/**
 * Classe modélisant la connexion à la base de données des posts.
 */
public class DatabaseApi {
    /** Connexion à la base de données */
    private Connection con;
    /** Curseur "statement" à exécuter */
    private Statement stmt;

    /**
     * Constructeur par défaut : connecte la base de donnée et et créer un statement
     */
    public DatabaseApi() {
        try {
            this.con = DriverManager.getConnection("jdbc:sqlite:target/dbase.db");
            this.stmt = con.createStatement();
            initDB(con);
            LOGGER.info("Database connected!");
            } catch (SQLException e) {
            LOGGER.severe("Can't connect to database! \n");
            LOGGER.log(Level.FINE, e.getMessage(), e);
        }
    }

    /**
     * Crée la base de données si elle n'existe pas déjà
     * @param con La connexion à la base de données
     */
    private void initDB(Connection con) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS `posts` ("
                    + "	`id` text PRIMARY KEY NOT NULL,"
                    + "	`timestamp` long,"
                    + "	`from` text,"
                    + " `to` text,"
                    + " `body` text,"
                    + " `connected` boolean"
                    + ");";
            stmt = con.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.severe("Cannot initialize database!");
            LOGGER.log(Level.FINE, e.getMessage(), e);
        }
    }

    /**
     * Récupère un post selon son UUID
     * @param uuid l'UUID du post
     * @return Une liste de résultats contenant le Post
     */
    public ResultSet getPostById(UUID uuid) {
        String query = "SELECT * FROM posts WHERE id = " + uuid.toString();
        try {
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.severe("Can't get post by id!");
            LOGGER.log(Level.FINE, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Récupère tous les posts dont le timestamp est supérieur à celui donné
     * @param timestamp Le timestamp de comparaison
     * @return Une liste de résultats contenant les posts, triés dans l'ordre chronologique
     */
    public ResultSet getPostsSince(long timestamp) {
        String query = "SELECT * FROM posts WHERE timestamp >= " + timestamp + "ORDER BY timestamp DESC";
        try {
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.severe("Can't get post since " + timestamp + "!");
            LOGGER.log(Level.FINE, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Récupère tous les posts de la base de données
     * @return Une liste de résultats contenant tous les posts en base
     */
    public ResultSet getPosts() {
        String query = "SELECT * FROM posts";
        try {
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.severe("Can't get posts!");
            return null;
        }
    }

    /**
     * Ajoute un post dans la base
     * @param post Le poste à ajouter
     * @return `true` si le post a bien été ajouté, `false` si une erreur est survenue
     */
    public boolean addPost(Post post) {
        String query = "INSERT INTO posts VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, post.getId().toString());
            pstmt.setLong(2, post.getTimestamp());
            pstmt.setString(3, post.getFrom());
            pstmt.setString(4, post.getTo());
            pstmt.setString(5, post.getBody());
            pstmt.setBoolean(6, post.isEditable());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.severe("Can't add post!");
            return false;
        }
    }

    /**
     * Enlève un post de la base de données
     * @param post Le post à retirer
     * @return `true` si le post a bien été retiré, `false` si une erreur est survenue
     */
    public boolean removePost(Post post) {
        String query = "DELETE FROM posts WHERE id=?";
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, post.getId().toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.severe("Cannot remove post!");
            LOGGER.log(Level.FINE, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Ferme la connexion à la base de données
     */
    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
            LOGGER.severe("Can't close database connection! Is database connected ?");
        }
    }

}
