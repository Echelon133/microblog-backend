package ml.echelon133.blobb.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
public class UserRepositoryTests {

    private UserRepository userRepository;

    @Autowired
    public UserRepositoryTests(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void beforeEach() {
        User u1 = new User("user1", "user1@mail.com", "user1", "");
        User u2 = new User("user2", "user2@mail.com", "user2", "");
        User u3 = new User("user3", "user3@mail.com", "user3", "");
        userRepository.save(u1);
        userRepository.save(u2);
        userRepository.save(u3);
    }

    @Test
    public void savedUserGetsUuid() {
        User user = new User("test1", "test@mail.com", "test1", "");
        assertNull(user.getUuid());

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getUuid());
    }

    @Test
    public void usersCanFollowEachOther() {
        User u1 = userRepository.findByUsername("user1").orElse(null);
        User u2 = userRepository.findByUsername("user2").orElse(null);
        User u3 = userRepository.findByUsername("user3").orElse(null);

        // All these lists should be empty, because we don't have any relationships between users
        List<User> usersFollowedByU1 = userRepository.findAllFollowedByUserWithUuid(u1.getUuid());
        List<User> usersFollowedByU2 = userRepository.findAllFollowedByUserWithUuid(u2.getUuid());
        List<User> usersFollowedByU3 = userRepository.findAllFollowedByUserWithUuid(u3.getUuid());

        assertEquals(0, usersFollowedByU1.size());
        assertEquals(0, usersFollowedByU2.size());
        assertEquals(0, usersFollowedByU3.size());

        // u1 follows u2, u3
        userRepository.followUserWithUuid(u1.getUuid(), u2.getUuid());
        userRepository.followUserWithUuid(u1.getUuid(), u3.getUuid());

        // u2 follows u3
        userRepository.followUserWithUuid(u2.getUuid(), u3.getUuid());

        // u3 follows u1
        userRepository.followUserWithUuid(u3.getUuid(), u1.getUuid());

        // query the database again
        usersFollowedByU1 = userRepository.findAllFollowedByUserWithUuid(u1.getUuid());
        usersFollowedByU2 = userRepository.findAllFollowedByUserWithUuid(u2.getUuid());
        usersFollowedByU3 = userRepository.findAllFollowedByUserWithUuid(u3.getUuid());

        // check if u1 follows u2, u3
        assertEquals(2, usersFollowedByU1.size());
        assertEquals(1, usersFollowedByU1.stream().filter(u -> u.getUuid() == u2.getUuid()).count());
        assertEquals(1, usersFollowedByU1.stream().filter(u -> u.getUuid() == u3.getUuid()).count());

        // check if u2 follows u3
        assertEquals(1, usersFollowedByU2.size());
        assertEquals(1, usersFollowedByU2.stream().filter(u -> u.getUuid() == u3.getUuid()).count());

        // check if u3 follows u1
        assertEquals(1, usersFollowedByU3.size());
        assertEquals(1, usersFollowedByU3.stream().filter(u -> u.getUuid() == u1.getUuid()).count());
    }

    @Test
    public void usersCanUnfollowEachOther() {
        User u1 = userRepository.findByUsername("user1").orElse(null);
        User u2 = userRepository.findByUsername("user2").orElse(null);

        // u1 follows u2, u2 follows u1
        userRepository.followUserWithUuid(u1.getUuid(), u2.getUuid());
        userRepository.followUserWithUuid(u2.getUuid(), u1.getUuid());

        List<User> usersFollowedByU1 = userRepository.findAllFollowedByUserWithUuid(u1.getUuid());
        List<User> usersFollowedByU2 = userRepository.findAllFollowedByUserWithUuid(u2.getUuid());

        // check if u1 follows u2, and u2 follows u1
        assertEquals(1, usersFollowedByU1.size());
        assertEquals(1, usersFollowedByU1.stream().filter(u -> u.getUuid() == u2.getUuid()).count());
        assertEquals(1, usersFollowedByU2.size());
        assertEquals(1, usersFollowedByU2.stream().filter(u -> u.getUuid() == u1.getUuid()).count());

        // make u1 and u2 unfollow each other
        userRepository.unfollowUserWithUuid(u1.getUuid(), u2.getUuid());
        userRepository.unfollowUserWithUuid(u2.getUuid(), u1.getUuid());

        // check the database again
        usersFollowedByU1 = userRepository.findAllFollowedByUserWithUuid(u1.getUuid());
        usersFollowedByU2 = userRepository.findAllFollowedByUserWithUuid(u2.getUuid());

        // check if both lists are empty
        assertEquals(0, usersFollowedByU1.size());
        assertEquals(0, usersFollowedByU2.size());
    }

    @Test
    public void usersCanCheckWhoFollowsThem() {
        User u1 = userRepository.findByUsername("user1").orElse(null);
        User u2 = userRepository.findByUsername("user2").orElse(null);

        // list of all who follow u1
        List<User> usersFollowingU1 = userRepository.findAllFollowingUserWithUuid(u1.getUuid());
        assertEquals(0, usersFollowingU1.size());

        // follow u1 as u2
        userRepository.followUserWithUuid(u2.getUuid(), u1.getUuid());

        // refresh the list
        usersFollowingU1 = userRepository.findAllFollowingUserWithUuid(u1.getUuid());

        // check if the list of u1 followers contains u2
        assertEquals(1, usersFollowingU1.size());
        assertEquals(1, usersFollowingU1.stream().filter(u -> u.getUuid() == u2.getUuid()).count());
    }
}
