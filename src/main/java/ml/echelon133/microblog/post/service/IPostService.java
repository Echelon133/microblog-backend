package ml.echelon133.microblog.post.service;

import ml.echelon133.microblog.post.exception.PostDoesntExistException;
import ml.echelon133.microblog.post.exception.UserCannotDeletePostException;
import ml.echelon133.microblog.post.model.Post;
import ml.echelon133.microblog.post.model.PostInfo;
import ml.echelon133.microblog.user.model.User;
import ml.echelon133.microblog.user.model.UserPost;
import ml.echelon133.microblog.user.model.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface IPostService {

    UserPost getByUuid(UUID uuid) throws PostDoesntExistException;
    PostInfo getPostInfo(UUID uuid) throws PostDoesntExistException;
    List<UserPost> getAllResponsesTo(UUID uuid, Long skip, Long limit) throws PostDoesntExistException, IllegalArgumentException;
    List<UserPost> getAllQuotesOf(UUID uuid, Long skip, Long limit) throws PostDoesntExistException, IllegalArgumentException;
    boolean checkIfUserWithUuidLikes(UserPrincipal user, UUID postUuid) throws PostDoesntExistException;
    boolean likePost(UserPrincipal user, UUID postUuid) throws PostDoesntExistException;
    boolean unlikePost(UserPrincipal user, UUID postUuid) throws PostDoesntExistException;
    List<UserPost> getFeedForUser(UserPrincipal user, Long skip, Long limit) throws IllegalArgumentException;
    List<UserPost> getFeedForUser_Popular(UserPrincipal user, Long skip, Long limit) throws IllegalArgumentException;
    List<UserPost> getFeedForAnonymousUser(Long skip, Long limit) throws IllegalArgumentException;
    Post processPostAndSave(Post post);
    Post postPost(User author, String content);
    Post postQuote(User author, String content, UUID quoteUuid) throws PostDoesntExistException;
    Post postResponse(User author, String content, UUID parentPostUuid) throws PostDoesntExistException;
    boolean markPostAsDeleted(User loggedUser, UUID postUuid) throws PostDoesntExistException, UserCannotDeletePostException;
}
