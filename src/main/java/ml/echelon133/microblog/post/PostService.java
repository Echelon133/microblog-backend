package ml.echelon133.microblog.post;

import ml.echelon133.microblog.notification.*;
import ml.echelon133.microblog.tag.ITagService;
import ml.echelon133.microblog.tag.Tag;
import ml.echelon133.microblog.tag.TagDoesntExistException;
import ml.echelon133.microblog.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class PostService implements IPostService {

    private Clock clock = Clock.systemDefaultZone();
    private PostRepository postRepository;
    private IUserService userService;
    private INotificationService notificationService;
    private ITagService tagService;
    private Pattern hashtagPattern = Pattern.compile("#([a-zA-Z0-9]{2,20})");
    private Pattern usernamePattern = Pattern.compile("@([A-Za-z0-9]{1,30})");

    @Autowired
    public PostService(PostRepository postRepository,
                       INotificationService notificationService,
                       ITagService tagService,
                       IUserService userService) {
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.notificationService = notificationService;
        this.userService = userService;
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
    public boolean checkIfUserWithUuidLikes(UserPrincipal user, UUID postUuid) throws PostDoesntExistException {
        throwIfPostDoesntExist(postUuid);
        return postRepository.checkIfUserWithUuidLikes(user.getUuid(), postUuid).isPresent();
    }

    @Override
    public boolean likePost(UserPrincipal user, UUID postUuid) throws PostDoesntExistException {
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
    public boolean unlikePost(UserPrincipal user, UUID postUuid) throws PostDoesntExistException {
        throwIfPostDoesntExist(postUuid);
        postRepository.unlikePostWithUuid(user.getUuid(), postUuid);
        return postRepository.checkIfUserWithUuidLikes(user.getUuid(), postUuid).isEmpty();
    }

    @Override
    public List<FeedPost> getFeedForUser(UserPrincipal user, Long skip, Long limit) throws IllegalArgumentException {
        if (limit < 0 || skip < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return postRepository
                .getFeedForUserWithUuid(user.getUuid(), skip, limit);
    }

    @Override
    public List<FeedPost> getFeedForUser_Popular(UserPrincipal user, Long skip, Long limit) throws IllegalArgumentException {
        if (limit < 0 || skip < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }

        // only get posts that had been posted after this date
        Date dayAgo = Date.from(Instant.now(clock).minus(1, DAYS));
        return postRepository
                .getFeedForUserWithUuid_Popular(user.getUuid(), dayAgo, skip, limit);
    }

    @Override
    public List<FeedPost> getFeedForAnonymousUser(Long skip, Long limit) throws IllegalArgumentException {
        if (limit < 0 || skip < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }

        // only get posts that had been posted after this date
        Date dayAgo = Date.from(Instant.now(clock).minus(1, DAYS));
        return postRepository
                .getFeedForAnonymousUser_Popular(dayAgo, skip, limit);
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

    private List<User> findMentionedUsersInContent(Post post) {
        // look for the username pattern in the post content
        Matcher m = usernamePattern.matcher(post.getContent());

        Set<String> uniqueUsernames = new HashSet<>();

        // find all mentioned usernames
        while (m.find()) {
            // every tag name should have all characters lower case
            uniqueUsernames.add(m.group(1));
        }

        List<User> allFoundUsers = new ArrayList<>();
        for (String username : uniqueUsernames) {
            // for every username check if that user exists in the database
            try {
                User u = userService.findByUsername(username);
                allFoundUsers.add(u);
            } catch (UserDoesntExistException ignore) {
            }
        }
        return allFoundUsers;
    }

    @Override
    public Post processPostAndSave(Post post) {
        List<Tag> tags = findTagsInContent(post);
        tags.forEach(post::addTag);
        Post savedPost = postRepository.save(post);
        List<User> mentionedUsers = findMentionedUsersInContent(post);
        notificationService.notifyAboutMention(savedPost, mentionedUsers);
        return savedPost;
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
            Post savedPost = processPostAndSave(quote);
            notificationService.notifyAboutQuote((QuotePost)savedPost, quotedPost.get().getAuthor());
            return savedPost;
        }
        throw new PostDoesntExistException(quotedPostUuid);
    }

    @Override
    public Post postResponse(User author, String content, UUID parentPostUuid) throws PostDoesntExistException {
        Optional<Post> parentPost = postRepository.findById(parentPostUuid);

        if (parentPost.isPresent() && !parentPost.get().isDeleted()) {
            Post response = new ResponsePost(author, content, parentPost.get());
            Post savedPost = processPostAndSave(response);
            notificationService.notifyAboutResponse((ResponsePost)savedPost, parentPost.get().getAuthor());
            return savedPost;
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
