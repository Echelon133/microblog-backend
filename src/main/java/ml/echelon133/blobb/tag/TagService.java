package ml.echelon133.blobb.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class TagService implements ITagService {

    private TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public Tag findByUuid(UUID uuid) throws TagDoesntExistException {
        Optional<Tag> tag = tagRepository.findById(uuid);
        if (tag.isPresent()) {
            return tag.get();
        }
        throw new TagDoesntExistException(uuid);
    }

    @Override
    public Tag findByName(String name) throws TagDoesntExistException {
        Optional<Tag> tag = tagRepository.findByName(name);
        if (tag.isPresent()) {
            return tag.get();
        }
        throw new TagDoesntExistException(name);
    }
}
