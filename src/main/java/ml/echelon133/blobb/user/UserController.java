package ml.echelon133.blobb.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/")
public class UserController {

    private IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("{uuid}")
    public ResponseEntity<User> getUser(@PathVariable String uuid) throws Exception {
        return new ResponseEntity<>(userService.findByUuid(UUID.fromString(uuid)), HttpStatus.OK);
    }

    @GetMapping("{uuid}/profile")
    public ResponseEntity<UserProfileInfo> getUserProfile(@PathVariable String uuid) throws Exception {
        return new ResponseEntity<>(userService.getUserProfileInfo(UUID.fromString(uuid)), HttpStatus.OK);
    }

    @GetMapping("{uuid}/follow")
    public ResponseEntity<Map<String, Boolean>> checkIfFollowed(@PathVariable String uuid) throws Exception {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = userService.checkIfUserFollows(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("followed", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("{uuid}/follow")
    public ResponseEntity<Map<String, Boolean>> followUser(@PathVariable String uuid) throws Exception {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = userService.followUserWithUuid(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("followed", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
