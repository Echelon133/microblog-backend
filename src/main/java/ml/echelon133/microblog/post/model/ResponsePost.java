package ml.echelon133.microblog.post.model;

import ml.echelon133.microblog.user.model.User;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class ResponsePost extends Post {

    @Relationship(type = "RESPONDS")
    private Post respondsTo;

    public ResponsePost(User author, String content, Post respondsTo) {
        super(author, content);
        this.respondsTo = respondsTo;
    }
}
