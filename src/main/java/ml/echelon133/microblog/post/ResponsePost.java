package ml.echelon133.microblog.post;

import ml.echelon133.microblog.user.User;
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
