package ml.echelon133.microblog.post;

import ml.echelon133.microblog.user.User;
import ml.echelon133.microblog.user.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface IPostService {

    enum PostsSince {
        ONE_HOUR(1),
        SIX_HOURS(6),
        TWELVE_HOURS(12),
        DAY(24);

        private int hours;

        PostsSince(int i) {
            this.hours = i;
        }

        int getHours() {
            return hours;
        }
    }

    FeedPost getByUuid(UUID uuid) throws PostDoesntExistException;
    PostInfo getPostInfo(UUID uuid) throws PostDoesntExistException;
    List<FeedPost> getAllResponsesTo(UUID uuid, Long skip, Long limit) throws PostDoesntExistException, IllegalArgumentException;
    List<FeedPost> getAllQuotesOf(UUID uuid, Long skip, Long limit) throws PostDoesntExistException, IllegalArgumentException;
    boolean checkIfUserWithUuidLikes(UserPrincipal user, UUID postUuid) throws PostDoesntExistException;
    boolean likePost(UserPrincipal user, UUID postUuid) throws PostDoesntExistException;
    boolean unlikePost(UserPrincipal user, UUID postUuid) throws PostDoesntExistException;
    List<FeedPost> getFeedForUser(UserPrincipal user, PostsSince since, Long skip, Long limit) throws IllegalArgumentException;
    List<FeedPost> getFeedForUser_Popular(UserPrincipal user, PostsSince since, Long skip, Long limit) throws IllegalArgumentException;
    List<FeedPost> getFeedForAnonymousUser(PostsSince since, Long skip, Long limit) throws IllegalArgumentException;
    Post processPostAndSave(Post post);
    Post postPost(User author, String content);
    Post postQuote(User author, String content, UUID quoteUuid) throws PostDoesntExistException;
    Post postResponse(User author, String content, UUID parentPostUuid) throws PostDoesntExistException;
    boolean markPostAsDeleted(User loggedUser, UUID postUuid) throws PostDoesntExistException, UserCannotDeletePostException;
}
