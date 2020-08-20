package ml.echelon133.blobb.tag;

import java.util.UUID;

public interface ITagService {
    Tag findById(UUID uuid) throws TagDoesntExistException;
    Tag findByName(String name) throws TagDoesntExistException;
}
