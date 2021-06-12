package ml.echelon133.microblog.notification;

import ml.echelon133.microblog.notification.model.NotificationResult;
import ml.echelon133.microblog.notification.repository.NotificationRepository;
import ml.echelon133.microblog.notification.service.NotificationService;
import ml.echelon133.microblog.post.model.Post;
import ml.echelon133.microblog.post.model.QuotePost;
import ml.echelon133.microblog.post.model.ResponsePost;
import ml.echelon133.microblog.user.model.User;
import ml.echelon133.microblog.user.model.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTests {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private static UserPrincipal mockUser = new UserPrincipal() {
        private UUID uuid = UUID.randomUUID();

        @Override
        public UUID getUuid() {
            return uuid;
        }

        @Override
        public String getUsername() {
            return "test";
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(() -> "ROLE_USER");
        }
    };

    @Test
    public void findAllNotificationsOfUser_ThrowsWhenSkipOrLimitNegative() {

        // when
        String ex1 = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.findAllNotificationsOfUser(mockUser, -1L, 0L);
        }).getMessage();

        String ex2 = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.findAllNotificationsOfUser(mockUser, 0L, -1L);
        }).getMessage();

        // then
        assertEquals("Skip or limit cannot be negative", ex1);
        assertEquals("Skip or limit cannot be negative", ex2);
    }

    @Test
    public void findAllNotificationsOfUser_ReturnsNotifications() {
        // given
        given(notificationRepository.findAllNotificationsOfUser(mockUser.getUuid(), 0L, 10L))
                .willReturn(List.of(new NotificationResult()));

        // when
        List<NotificationResult> result = notificationService.findAllNotificationsOfUser(mockUser, 0L, 10L);

        // then
        assertEquals(1L, result.size());
    }

    @Test
    public void countUnreadNotificationsOfUser_Works() {
        // given
        given(notificationRepository.countUnreadNotificationsOfUser(mockUser.getUuid()))
                .willReturn(10L);

        // when
        Long unread = notificationService.countUnreadNotificationsOfUser(mockUser);

        // then
        assertEquals(10L, unread);
    }

    @Test
    public void readAllNotificationsOfUser_Works() {
        // given
        given(notificationRepository.readAllNotificationsOfUser(mockUser.getUuid()))
                .willReturn(10L);

        // when
        Long read = notificationService.readAllNotificationsOfUser(mockUser);

        // then
        assertEquals(10L, read);
    }

    @Test
    public void readSingleNotificationOfUser_Works() {
        UUID notificationUuid = UUID.randomUUID();

        // given
        given(notificationRepository.readSingleNotificationOfUser(mockUser.getUuid(), notificationUuid))
                .willReturn(true);

        // when
        boolean read = notificationService.readSingleNotificationOfUser(mockUser, notificationUuid);

        // then
        assertTrue(read);
    }

    @Test
    public void notifyAboutResponse_DoesNotNotifyAboutUserRespondingToTheirOwnPosts() {
        User author = new User();
        author.setUuid(UUID.randomUUID());

        ResponsePost responsePost = new ResponsePost(author, "", null);

        // when
        boolean notified = notificationService.notifyAboutResponse(responsePost, author);

        // then
        assertFalse(notified);
    }

    @Test
    public void notifyAboutResponse_NotifiesAboutUserRespondingToAnotherUserPost() {
        User author = new User();
        author.setUuid(UUID.randomUUID());
        User notifyUser = new User();
        notifyUser.setUuid(UUID.randomUUID());

        ResponsePost responsePost = new ResponsePost(author, "", null);

        // when
        boolean notified = notificationService.notifyAboutResponse(responsePost, notifyUser);

        // then
        assertTrue(notified);
    }

    @Test
    public void notifyAboutQuote_DoesNotNotifyAboutUserQuotingTheirOwnPosts() {
        User author = new User();
        author.setUuid(UUID.randomUUID());

        QuotePost quotePost = new QuotePost(author, "", null);

        // when
        boolean notified = notificationService.notifyAboutQuote(quotePost, author);

        // then
        assertFalse(notified);
    }

    @Test
    public void notifyAboutQuote_NotifiesAboutUserQuotingAnotherUserPost() {
        User author = new User();
        author.setUuid(UUID.randomUUID());
        User notifyUser = new User();
        notifyUser.setUuid(UUID.randomUUID());

        QuotePost quotePost = new QuotePost(author, "", null);

        // when
        boolean notified = notificationService.notifyAboutQuote(quotePost, notifyUser);

        // then
        assertTrue(notified);
    }

    @Test
    public void notifyAboutMention_NotifiesAllUsersWhoAreNotAuthorsOfPost() {
        User author = new User();
        author.setUuid(UUID.randomUUID());
        User notifyUser1 = new User();
        notifyUser1.setUuid(UUID.randomUUID());
        User notifyUser2 = new User();
        notifyUser2.setUuid(UUID.randomUUID());

        Post post = new Post(author, "testtest");

        List<User> mentionedUsers = List.of(author, notifyUser1, notifyUser2);

        // when
        // should return 2, because one mentioned user is the author of the post
        Long notifiedCounter = notificationService.notifyAboutMention(post, mentionedUsers);

        // then
        assertEquals(2, notifiedCounter);
    }
}
