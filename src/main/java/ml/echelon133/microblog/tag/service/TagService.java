package ml.echelon133.microblog.tag.service;

import ml.echelon133.microblog.tag.model.RecentPost;
import ml.echelon133.microblog.tag.model.Tag;
import ml.echelon133.microblog.tag.repository.TagRepository;
import ml.echelon133.microblog.tag.exception.TagDoesntExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class TagService implements ITagService {

    private TagRepository tagRepository;
    private Clock clock = Clock.systemDefaultZone();

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

    @Override
    public List<Tag> findMostPopular(Long limit, PopularSince since) throws IllegalArgumentException {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }

        Date before;
        Date now = Date.from(Instant.now(clock));
        switch (since) {
            case ONE_HOUR:
                before = Date.from(now.toInstant().minus(1, HOURS));
                break;
            case DAY:
                before = Date.from(now.toInstant().minus(1, DAYS));
                break;
            default:
                before = Date.from(now.toInstant().minus(7, DAYS));
        }
        return tagRepository.findMostPopularTags_Between(before, now, limit);
    }

    @Override
    public List<RecentPost> findRecentPostsTagged(UUID tagUuid, Long skip, Long limit) throws TagDoesntExistException {
        if (!tagRepository.existsById(tagUuid)) {
            throw new TagDoesntExistException(tagUuid);
        }

        if (limit < 0 || skip < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }

        return tagRepository.findRecentPostsTagged(tagUuid, skip, limit);
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
