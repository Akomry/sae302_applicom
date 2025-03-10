package rtgre.modeles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Vector;

import static rtgre.chat.ChatApplication.LOGGER;

/**
 * Classe modélisant une liste de posts
 */
public class PostVector extends Vector<Post> {

    /**
     * Extrait un post en fonction de son identifiant
     * @param uuid L'identifiant du post recherché
     * @return Le post correspondant
     */
    public Post getPostById(UUID uuid) {
        for (Post post : this) {
            if (post.id == uuid) {
                return post;
            }
        }
        return null;
    }

    /**
     * Renvoie la liste des posts, qui ont été créé à partir d'un timestamp donné
     * @param timestamp Le timestamp à partir duquel extraire les posts
     * @return La liste des posts extraits
     */
    public Vector<Post> getPostsSince(long timestamp) {
        Vector<Post> posts = new Vector<>();
        for (Post post : this) {
            if (post.timestamp > timestamp) {
                posts.add(post);
            }
        }
        return posts;
    }

    /**
     * Charge la liste des posts depuis la base de données
     */
    public void loadPosts() {
        try {
            DatabaseApi database = new DatabaseApi();
            ResultSet postResult = database.getPosts();
            while (postResult.next()) {
                this.add(new Post(
                        UUID.fromString(postResult.getString("id")),
                        postResult.getLong("timestamp"),
                        postResult.getString("from"),
                        postResult.getString("to"),
                        postResult.getString("body")
                ));
            }
        } catch (SQLException e) {
            LOGGER.severe("Cannot load posts!");
        }
    }
}
