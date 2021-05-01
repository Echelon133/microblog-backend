package ml.echelon133.microblog.notification;

import ml.echelon133.microblog.post.QuotePost;
import ml.echelon133.microblog.user.User;
import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "NOTIFIES")
public class QuoteNotification extends Notification {

    public QuoteNotification(QuotePost notifyingPost, User notifiedUser) {
        super("quote", notifyingPost, notifiedUser);
    }
}
