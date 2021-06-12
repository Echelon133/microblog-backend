package ml.echelon133.microblog.notification.service;

import ml.echelon133.microblog.notification.model.NotificationResult;
import ml.echelon133.microblog.post.model.Post;
import ml.echelon133.microblog.post.model.QuotePost;
import ml.echelon133.microblog.post.model.ResponsePost;
import ml.echelon133.microblog.user.model.User;
import ml.echelon133.microblog.user.model.UserPrincipal;

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
