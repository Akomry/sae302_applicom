package rtgre.modeles;

import java.util.UUID;
import java.util.Vector;

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

}
