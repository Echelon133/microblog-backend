package ml.echelon133.microblog.notification;

import ml.echelon133.microblog.post.Post;
import ml.echelon133.microblog.post.QuotePost;
import ml.echelon133.microblog.post.ResponsePost;
import ml.echelon133.microblog.user.User;
import ml.echelon133.microblog.user.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService implements INotificationService {

    private NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationResult> findAllNotificationsOfUser(UserPrincipal user, Long skip, Long limit) {
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Skip or limit cannot be negative");
        }
        return notificationRepository.findAllNotificationsOfUser(user.getUuid(), skip, limit);
    }

    @Override
    public Long countUnreadNotificationsOfUser(UserPrincipal user) {
        return notificationRepository.countUnreadNotificationsOfUser(user.getUuid());
    }

    @Override
    public Long readAllNotificationsOfUser(UserPrincipal user) {
        return notificationRepository.readAllNotificationsOfUser(user.getUuid());
    }

    @Override
    public boolean readSingleNotificationOfUser(UserPrincipal user, UUID notificationUuid) {
        return notificationRepository.readSingleNotificationOfUser(user.getUuid(), notificationUuid);
    }

    @Override
    public boolean notifyAboutResponse(ResponsePost notifyAbout, User notifiedUser) {
        UUID notifiedUserUuid = notifiedUser.getUuid();
        UUID responseAuthorUuid = notifyAbout.getAuthor().getUuid();
        // don't notify the user about them responding to their own posts
        if (!notifiedUserUuid.equals(responseAuthorUuid)) {
            Notification rNotif = new ResponseNotification(notifyAbout, notifiedUser);
            notificationRepository.save(rNotif);
            return true;
        }
        return false;
    }

    @Override
    public boolean notifyAboutQuote(QuotePost notifyAbout, User notifiedUser) {
        UUID notifiedUserUuid = notifiedUser.getUuid();
        UUID responseAuthorUuid = notifyAbout.getAuthor().getUuid();
        // don't notify the user about them quoting their own posts
        if (!notifiedUserUuid.equals(responseAuthorUuid)) {
            Notification qNotif = new QuoteNotification(notifyAbout, notifiedUser);
            notificationRepository.save(qNotif);
            return true;
        }
        return false;
    }

    @Override
    public Long notifyAboutMention(Post notifyAbout, List<User> notifiedUsers) {
        Long notifiedUsersCounter = 0L;
        for (User mentioned : notifiedUsers) {
            UUID notifiedUserUuid = mentioned.getUuid();
            UUID postAuthorUuid = notifyAbout.getAuthor().getUuid();
            // don't notify the user about a post in which they mention themselves
            if (!notifiedUserUuid.equals(postAuthorUuid)) {
                Notification mNotif = new MentionNotification(notifyAbout, mentioned);
                notificationRepository.save(mNotif);
                notifiedUsersCounter++;
            }
        }
        return notifiedUsersCounter;
    }
}
