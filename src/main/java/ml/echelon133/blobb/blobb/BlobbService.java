package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.tag.ITagService;
import ml.echelon133.blobb.tag.Tag;
import ml.echelon133.blobb.tag.TagDoesntExistException;
import ml.echelon133.blobb.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class BlobbService implements IBlobbService {

    private BlobbRepository blobbRepository;
    private ITagService tagService;
    private Clock clock = Clock.systemDefaultZone();
    private Pattern hashtagPattern = Pattern.compile("(#[a-zA-Z0-9]{2,20})");

    @Autowired
    public BlobbService(BlobbRepository blobbRepository,
                        ITagService tagService) {
        this.blobbRepository = blobbRepository;
        this.tagService = tagService;
    }

    private void throwIfBlobbDoesntExist(UUID uuid) throws BlobbDoesntExistException {
        if (!blobbRepository.existsById(uuid)) {
            throw new BlobbDoesntExistException(uuid);
        }
    }

    @Override
    public FeedBlobb getByUuid(UUID uuid) throws BlobbDoesntExistException {
        Optional<FeedBlobb> blobb = blobbRepository.getBlobbWithUuid(uuid);
        if (blobb.isPresent()) {
            return blobb.get();
        }
        throw new BlobbDoesntExistException(uuid);
    }

    @Override
    public BlobbInfo getBlobbInfo(UUID uuid) throws BlobbDoesntExistException {
        Optional<BlobbInfo> info = blobbRepository.getInfoAboutBlobbWithUuid(uuid);
        if (info.isPresent()) {
            return info.get();
        }
        throw new BlobbDoesntExistException(uuid);
    }

    @Override
    public List<FeedBlobb> getAllResponsesTo(UUID uuid, Long skip, Long limit) throws BlobbDoesntExistException, IllegalArgumentException {
        throwIfBlobbDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return blobbRepository.getAllResponsesToBlobbWithUuid(uuid, skip, limit);
    }

    @Override
    public List<FeedBlobb> getAllReblobbsOf(UUID uuid, Long skip, Long limit) throws BlobbDoesntExistException, IllegalArgumentException {
        throwIfBlobbDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return blobbRepository.getAllReblobbsOfBlobbWithUuid(uuid, skip, limit);

    }

    @Override
    public boolean checkIfUserWithUuidLikes(User user, UUID blobbUuid) throws BlobbDoesntExistException {
        throwIfBlobbDoesntExist(blobbUuid);
        return blobbRepository.checkIfUserWithUuidLikes(user.getUuid(), blobbUuid).isPresent();
    }

    @Override
    public boolean likeBlobb(User user, UUID blobbUuid) throws BlobbDoesntExistException {
        throwIfBlobbDoesntExist(blobbUuid);
        Optional<Long> like = blobbRepository.checkIfUserWithUuidLikes(user.getUuid(), blobbUuid);

        // only like if there is no already existing 'likes' relationship
        // between the user and the post
        // otherwise this will create duplicate relationships
        if (like.isEmpty()) {
            like = blobbRepository.likeBlobbWithUuid(user.getUuid(), blobbUuid);
        }
        return like.isPresent();
    }

    @Override
    public boolean unlikeBlobb(User user, UUID blobbUuid) throws BlobbDoesntExistException {
        throwIfBlobbDoesntExist(blobbUuid);
        blobbRepository.unlikeBlobbWithUuid(user.getUuid(), blobbUuid);
        return blobbRepository.checkIfUserWithUuidLikes(user.getUuid(), blobbUuid).isEmpty();
    }

    @Override
    public List<FeedBlobb> getFeedForUser(User user, BlobbsSince since, Long skip, Long limit) throws IllegalArgumentException {

        if (limit < 0 || skip < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }

        int hoursToSubtract = since.getHours();
        Date now = Date.from(Instant.now(clock));
        Date before =  Date.from(now.toInstant().minus(hoursToSubtract, HOURS));
        return blobbRepository
                .getFeedForUserWithUuid_PostedBetween(user.getUuid(), before, now, skip, limit);
    }

    private List<Tag> findTagsInContent(Blobb blobb) {
        // look for the hashtag pattern in the blobb content
        Matcher m = hashtagPattern.matcher(blobb.getContent());

        Set<String> uniqueTags = new HashSet<>();

        // find all tags that were used and save only unique ones
        while (m.find()) {
            // every tag name should have all characters lower case
            uniqueTags.add(m.group().toLowerCase());
        }

        List<Tag> allFoundTags = new ArrayList<>();
        for (String tagName : uniqueTags) {
            // for every tag name check if that tag already exists
            // in the database
            try {
                Tag dbTag = tagService.findByName(tagName);
                allFoundTags.add(dbTag);
            } catch (TagDoesntExistException ex) {
                // tag doesn't exist in the database
                // create a new tag
                allFoundTags.add(new Tag(tagName));
            }
        }
        return allFoundTags;
    }

    @Override
    public Blobb processBlobbAndSave(Blobb blobb) {
        List<Tag> tags = findTagsInContent(blobb);
        tags.forEach(blobb::addTag);
        return blobbRepository.save(blobb);
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
