package ml.echelon133.microblog.notification.model;

import ml.echelon133.microblog.post.model.ResponsePost;
import ml.echelon133.microblog.user.model.User;
import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "NOTIFIES")
public class ResponseNotification extends Notification {

    public ResponseNotification(ResponsePost notifyingPost, User notifiedUser) {
        super("response", notifyingPost, notifiedUser);
    }
}
