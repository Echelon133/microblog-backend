package ml.echelon133.microblog.notification;

import ml.echelon133.microblog.post.Post;
import ml.echelon133.microblog.post.QuotePost;
import ml.echelon133.microblog.post.ResponsePost;
import ml.echelon133.microblog.user.User;
import ml.echelon133.microblog.user.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    List<NotificationResult> findAllNotificationsOfUser(UserPrincipal user, Long skip, Long limit) throws IllegalArgumentException;
    Long countUnreadNotificationsOfUser(UserPrincipal user);
    Long readAllNotificationsOfUser(UserPrincipal user);
    boolean readSingleNotificationOfUser(UserPrincipal user, UUID notificationUuid);
    boolean notifyAboutResponse(ResponsePost notifyAbout, User notifiedUser);
    boolean notifyAboutQuote(QuotePost notifyAbout, User notifiedUser);
    Long notifyAboutMention(Post notifyAbout, List<User> notifiedUsers);
}
