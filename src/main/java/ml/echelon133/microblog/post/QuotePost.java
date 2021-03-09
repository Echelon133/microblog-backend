package ml.echelon133.microblog.post;

import ml.echelon133.microblog.user.User;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class QuotePost extends Post {

    @Relationship(type = "QUOTES")
    private Post quotes;

    public QuotePost(User author, String content, Post quotes) {
        super(author, content);
        this.quotes = quotes;
    }
}
