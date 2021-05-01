package ml.echelon133.microblog.notification;

import ml.echelon133.microblog.post.Post;
import ml.echelon133.microblog.post.PostRepository;
import ml.echelon133.microblog.post.QuotePost;
import ml.echelon133.microblog.post.ResponsePost;
import ml.echelon133.microblog.user.User;
import ml.echelon133.microblog.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
public class NotificationRepositoryTests {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private NotificationRepository notificationRepository;

    @Autowired
    public NotificationRepositoryTests(UserRepository userRepository,
                                       PostRepository postRepository,
                                       NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.notificationRepository = notificationRepository;
    }

    @BeforeEach
    public void beforeEach() {
        // setup users
        User u1 = new User("user1", "user1@mail.com", "user1", "");
        User u2 = new User("user2", "user2@mail.com", "user2", "");
        userRepository.save(u1);
        userRepository.save(u2);
    }

    @Test
    public void findAllNotificationsOfUser_ReturnsEmptyListWhenNoNotifications() {
        Optional<User> u = userRepository.findByUsername("user1");

        // when
        UUID userUuid = u.get().getUuid();
        List<NotificationResult> notifications = notificationRepository.findAllNotificationsOfUser(userUuid, 0L, 10L);

        // then
        assertEquals(0, notifications.size());
    }

    @Test
    public void findAllNotificationsOfUser_ReturnsFoundNotificationsInCorrectOrder() {
        Optional<User> u1 = userRepository.findByUsername("user1");
        Optional<User> u2 = userRepository.findByUsername("user2");

        // given
        Post savedPost = postRepository.save(new Post(u1.get(), "test content of a post"));
        // make user2 quote and respond to post of user1, and also mention him in one separate post
        QuotePost savedQuote = postRepository.save(new QuotePost(u2.get(), "quote", savedPost));
        ResponsePost savedResponse = postRepository.save(new ResponsePost(u2.get(), "response", savedPost));
        Post savedMention = postRepository.save(new Post(u2.get(), "@user1"));

        // notify user1 about user2's quote, response and mention
        notificationRepository.save(new QuoteNotification(savedQuote, u1.get()));
        notificationRepository.save(new ResponseNotification(savedResponse, u1.get()));
        notificationRepository.save(new MentionNotification(savedMention, u1.get()));

        // when
        UUID userUuid = u1.get().getUuid();
        // get all notifications of user1
        List<NotificationResult> notifications = notificationRepository.findAllNotificationsOfUser(userUuid, 0L, 10L);

        // then
        assertEquals(3, notifications.size());

        // check if the notifications have correct order and values
        NotificationResult notif1 = notifications.get(0);
        assertEquals("mention", notif1.getType());
        assertEquals("user2", notif1.getNotifiedBy());
        assertFalse(notif1.isRead());
        assertEquals(savedMention.getUuid(), notif1.getNotificationPost());

        NotificationResult notif2 = notifications.get(1);
        assertEquals("response", notif2.getType());
        assertEquals("user2", notif2.getNotifiedBy());
        assertFalse(notif2.isRead());
        assertEquals(savedResponse.getUuid(), notif2.getNotificationPost());

        NotificationResult notif3 = notifications.get(2);
        assertEquals("quote", notif3.getType());
        assertEquals("user2", notif2.getNotifiedBy());
        assertFalse(notif3.isRead());
        assertEquals(savedQuote.getUuid(), notif3.getNotificationPost());
    }

    @Test
    public void findAllNotificationsOfUser_SkipAndLimitWork() {
        Optional<User> u1 = userRepository.findByUsername("user1");
        Optional<User> u2 = userRepository.findByUsername("user2");

        // given
        Post savedPost = postRepository.save(new Post(u1.get(), "test content of a post"));
        ResponsePost savedResponse = postRepository.save(new ResponsePost(u2.get(), "response", savedPost));

        // make 5 notifications
        for (int i = 0; i < 5; i++) {
            notificationRepository.save(new ResponseNotification(savedResponse, u1.get()));
        }

        // when
        UUID userUuid = u1.get().getUuid();
        // limit to 1
        List<NotificationResult> notifications1 = notificationRepository.findAllNotificationsOfUser(userUuid, 0L, 1L);
        // skip 3
        List<NotificationResult> notifications2 = notificationRepository.findAllNotificationsOfUser(userUuid, 3L, 5L);

        // then
        assertEquals(1, notifications1.size());
        assertEquals(2, notifications2.size());
    }

    @Test
    public void countUnreadNotificationsOfUser_ReturnsZeroWhenNoNotifications() {
        Optional<User> u1 = userRepository.findByUsername("user1");

        // when
        Long unreadCounter = notificationRepository.countUnreadNotificationsOfUser(u1.get().getUuid());

        // then
        assertEquals(0, unreadCounter);
    }

    @Test
    public void countUnreadNotificationsOfUser_ReturnsCorrectNumberOfUnread() {
        Optional<User> u1 = userRepository.findByUsername("user1");
        Optional<User> u2 = userRepository.findByUsername("user2");

        // given
        // create two responses and two notifications
        Post savedPost = postRepository.save(new Post(u1.get(), "test content of a post"));
        ResponsePost savedResponse1 = postRepository.save(new ResponsePost(u2.get(), "response", savedPost));
        ResponsePost savedResponse2 = postRepository.save(new ResponsePost(u2.get(), "response", savedPost));
        notificationRepository.save(new ResponseNotification(savedResponse1, u1.get()));
        notificationRepository.save(new ResponseNotification(savedResponse2, u1.get()));

        // when
        Long unreadCounter = notificationRepository.countUnreadNotificationsOfUser(u1.get().getUuid());

        // then
        assertEquals(2, unreadCounter);
    }

    @Test
    public void readAllNotificationsOfUser_SetsNotificationStateCorrectly() {
        Optional<User> u1 = userRepository.findByUsername("user1");
        Optional<User> u2 = userRepository.findByUsername("user2");

        // given
        // create two responses and two notifications
        Post savedPost = postRepository.save(new Post(u1.get(), "test content of a post"));
        ResponsePost savedResponse1 = postRepository.save(new ResponsePost(u2.get(), "response", savedPost));
        ResponsePost savedResponse2 = postRepository.save(new ResponsePost(u2.get(), "response", savedPost));
        notificationRepository.save(new ResponseNotification(savedResponse1, u1.get()));
        notificationRepository.save(new ResponseNotification(savedResponse2, u1.get()));

        // when
        // should read 2 notifications
        Long readNotificationsCounter1 = notificationRepository
                .readAllNotificationsOfUser(u1.get().getUuid());
        // since all notifications are already read, this returns 0 read
        Long readNotificationsCounter2 = notificationRepository
                .readAllNotificationsOfUser(u1.get().getUuid());
        Long unreadCounter = notificationRepository.countUnreadNotificationsOfUser(u1.get().getUuid());

        // then
        assertEquals(2, readNotificationsCounter1);
        assertEquals(0, readNotificationsCounter2);
        assertEquals(0, unreadCounter);
    }

    @Test
    public void readSingleNotificationOfUser_SetsNotificationStateCorrectly() {
        Optional<User> u1 = userRepository.findByUsername("user1");
        Optional<User> u2 = userRepository.findByUsername("user2");

        // given
        // create two responses and two notifications
        Post savedPost = postRepository.save(new Post(u1.get(), "test content of a post"));
        ResponsePost savedResponse1 = postRepository.save(new ResponsePost(u2.get(), "response", savedPost));
        Notification notif = notificationRepository.save(new ResponseNotification(savedResponse1, u1.get()));

        // when
        UUID notificationUuid = notif.getUuid();
        // notification has not been read yet, should return true
        boolean readNotification1 = notificationRepository.readSingleNotificationOfUser(u1.get().getUuid(), notificationUuid);
        // notification has already been read, should return false
        boolean readNotification2 = notificationRepository.readSingleNotificationOfUser(u1.get().getUuid(), notificationUuid);

        // then
        assertTrue(readNotification1);
        assertFalse(readNotification2);
    }
}
