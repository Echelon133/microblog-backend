package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.user.User;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class ResponseBlobb extends Blobb {

    @Relationship(type = "RESPONDS")
    private Blobb respondsTo;

    public ResponseBlobb(User author, String content, Blobb respondsTo) {
        super(author, content);
        this.respondsTo = respondsTo;
    }
}
