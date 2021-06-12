package ml.echelon133.microblog.notification.model;

import ml.echelon133.microblog.post.model.Post;
import ml.echelon133.microblog.user.model.User;
import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "NOTIFIES")
public class MentionNotification extends Notification {

    public MentionNotification(Post post, User user) {
        super("mention", post, user);
    }
}
