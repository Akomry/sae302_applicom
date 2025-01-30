package rtgre.modeles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Vector;

import static rtgre.chat.ChatApplication.LOGGER;

public class PostVector extends Vector<Post> {

    public Post getPostById(UUID uuid) {
        for (Post post : this) {
            if (post.id == uuid) {
                return post;
            }
        }
        return null;
    }

    public Vector<Post> getPostsSince(long timestamp) {
        Vector<Post> posts = new Vector<>();
        for (Post post : this) {
            if (post.timestamp > timestamp) {
                posts.add(post);
            }
        }
        return posts;
    }

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
