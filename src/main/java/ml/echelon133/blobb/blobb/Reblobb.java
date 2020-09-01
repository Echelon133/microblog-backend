package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.user.User;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Reblobb extends Blobb {

    @Relationship(type = "REBLOBBS")
    private Blobb reblobbs;

    public Reblobb(User author, String content, Blobb reblobbs) {
        super(author, content);
        this.reblobbs = reblobbs;
    }
}
