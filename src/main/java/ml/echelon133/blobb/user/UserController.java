package ml.echelon133.blobb.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
