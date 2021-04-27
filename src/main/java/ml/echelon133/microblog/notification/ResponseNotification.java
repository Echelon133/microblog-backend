package ml.echelon133.microblog.notification;

import ml.echelon133.microblog.post.ResponsePost;
import ml.echelon133.microblog.user.User;
import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "NOTIFIES")
public class ResponseNotification extends Notification {

    public ResponseNotification(ResponsePost notifyingPost, User notifiedUser) {
        super("response", notifyingPost, notifiedUser);
    }
}
