package ml.echelon133.microblog.post;

import ml.echelon133.microblog.tag.ITagService;
import ml.echelon133.microblog.tag.Tag;
import ml.echelon133.microblog.tag.TagDoesntExistException;
import ml.echelon133.microblog.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class PostService implements IPostService {

    private PostRepository postRepository;
    private ITagService tagService;
    private Clock clock = Clock.systemDefaultZone();
    private Pattern hashtagPattern = Pattern.compile("#([a-zA-Z0-9]{2,20})");

    @Autowired
    public PostService(PostRepository postRepository,
                       ITagService tagService) {
        this.postRepository = postRepository;
        this.tagService = tagService;
    }

    private void throwIfPostDoesntExist(UUID uuid) throws PostDoesntExistException {
        if (!postRepository.existsById(uuid)) {
            throw new PostDoesntExistException(uuid);
        }
    }

    @Override
    public FeedPost getByUuid(UUID uuid) throws PostDoesntExistException {
        Optional<FeedPost> post = postRepository.getPostWithUuid(uuid);
        if (post.isPresent()) {
            return post.get();
        }
        throw new PostDoesntExistException(uuid);
    }

    @Override
    public PostInfo getPostInfo(UUID uuid) throws PostDoesntExistException {
        Optional<PostInfo> info = postRepository.getInfoAboutPostWithUuid(uuid);
        if (info.isPresent()) {
            return info.get();
        }
        throw new PostDoesntExistException(uuid);
    }

    @Override
    public List<FeedPost> getAllResponsesTo(UUID uuid, Long skip, Long limit) throws PostDoesntExistException, IllegalArgumentException {
        throwIfPostDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return postRepository.getAllResponsesToPostWithUuid(uuid, skip, limit);
    }

    @Override
    public List<FeedPost> getAllQuotesOf(UUID uuid, Long skip, Long limit) throws PostDoesntExistException, IllegalArgumentException {
        throwIfPostDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return postRepository.getAllQuotesOfPostWithUuid(uuid, skip, limit);

    }

    @Override
    public boolean checkIfUserWithUuidLikes(User user, UUID postUuid) throws PostDoesntExistException {
        throwIfPostDoesntExist(postUuid);
        return postRepository.checkIfUserWithUuidLikes(user.getUuid(), postUuid).isPresent();
    }

    @Override
    public boolean likePost(User user, UUID postUuid) throws PostDoesntExistException {
        throwIfPostDoesntExist(postUuid);
        Optional<Long> like = postRepository.checkIfUserWithUuidLikes(user.getUuid(), postUuid);

        // only like if there is no already existing 'likes' relationship
        // between the user and the post
        // otherwise this will create duplicate relationships
        if (like.isEmpty()) {
            like = postRepository.likePostWithUuid(user.getUuid(), postUuid);
        }
        return like.isPresent();
    }

    @Override
    public boolean unlikePost(User user, UUID postUuid) throws PostDoesntExistException {
        throwIfPostDoesntExist(postUuid);
        postRepository.unlikePostWithUuid(user.getUuid(), postUuid);
        return postRepository.checkIfUserWithUuidLikes(user.getUuid(), postUuid).isEmpty();
    }

    @Override
    public List<FeedPost> getFeedForUser(User user, PostsSince since, Long skip, Long limit) throws IllegalArgumentException {

        if (limit < 0 || skip < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }

        int hoursToSubtract = since.getHours();
        Date now = Date.from(Instant.now(clock));
        Date before =  Date.from(now.toInstant().minus(hoursToSubtract, HOURS));
        return postRepository
                .getFeedForUserWithUuid_PostedBetween(user.getUuid(), before, now, skip, limit);
    }

    @Override
    public List<FeedPost> getFeedForUser_Popular(User user, PostsSince since, Long skip, Long limit) throws IllegalArgumentException {

        if (limit < 0 || skip < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }

        int hoursToSubtract = since.getHours();
        Date now = Date.from(Instant.now(clock));
        Date before =  Date.from(now.toInstant().minus(hoursToSubtract, HOURS));
        return postRepository
                .getFeedForUserWithUuid_Popular_PostedBetween(user.getUuid(), before, now, skip, limit);

    }

    private List<Tag> findTagsInContent(Post post) {
        // look for the hashtag pattern in the post content
        Matcher m = hashtagPattern.matcher(post.getContent());

        Set<String> uniqueTags = new HashSet<>();

        // find all tags that were used and save only unique ones
        while (m.find()) {
            // every tag name should have all characters lower case
            uniqueTags.add(m.group(1).toLowerCase());
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
    public Post processPostAndSave(Post post) {
        List<Tag> tags = findTagsInContent(post);
        tags.forEach(post::addTag);
        return postRepository.save(post);
    }

    @Override
    public Post postPost(User author, String content) {
        Post b = new Post(author, content);
        return processPostAndSave(b);
    }

    @Override
    public Post postQuote(User author, String content, UUID quotedPostUuid) throws PostDoesntExistException {
        Optional<Post> quotedPost = postRepository.findById(quotedPostUuid);

        if (quotedPost.isPresent() && !quotedPost.get().isDeleted()) {
            Post quote = new QuotePost(author, content, quotedPost.get());
            return processPostAndSave(quote);
        }
        throw new PostDoesntExistException(quotedPostUuid);
    }

    @Override
    public Post postResponse(User author, String content, UUID parentPostUuid) throws PostDoesntExistException {
        Optional<Post> parentPost = postRepository.findById(parentPostUuid);

        if (parentPost.isPresent() && !parentPost.get().isDeleted()) {
            Post response = new ResponsePost(author, content, parentPost.get());
            return processPostAndSave(response);
        }
        throw new PostDoesntExistException(parentPostUuid);
    }

    @Override
    public boolean markPostAsDeleted(User loggedUser, UUID postUuid) throws PostDoesntExistException, UserCannotDeletePostException {
        Optional<Post> postToDelete = postRepository.findById(postUuid);

        if (postToDelete.isPresent()) {
            Post b = postToDelete.get();
            // make sure that the user has the right to delete that post
            if (!b.getAuthor().getUuid().equals(loggedUser.getUuid())) {
                throw new UserCannotDeletePostException(loggedUser, postUuid);
            }
            b.markAsDeleted();
            return postRepository.save(b).isDeleted();
        }
        throw new PostDoesntExistException(postUuid);
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
