package ml.echelon133.blobb.feed;

import ml.echelon133.blobb.blobb.FeedBlobb;
import ml.echelon133.blobb.blobb.IBlobbService;
import ml.echelon133.blobb.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static ml.echelon133.blobb.blobb.IBlobbService.BlobbsSince.*;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private IBlobbService blobbService;

    @Autowired
    public FeedController(IBlobbService blobbService) {
        this.blobbService = blobbService;
    }

    @GetMapping
    public ResponseEntity<List<FeedBlobb>> getUserFeed(@RequestParam(required = false) String since,
                                                       @RequestParam(required = false) String by,
                                                       @RequestParam(required = false) Long skip,
                                                       @RequestParam(required = false) Long limit) throws IllegalArgumentException {

        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (skip == null) {
            skip = 0L;
        }

        if (limit == null) {
            limit = 20L;
        }

        IBlobbService.BlobbsSince blobbsSince = ONE_HOUR;
        if (since != null) {
            switch(since.toUpperCase()) {
                case "SIX_HOURS":
                    blobbsSince = SIX_HOURS;
                    break;
                case "TWELVE_HOURS":
                    blobbsSince = TWELVE_HOURS;
                    break;
                case "HOUR":
                default:
                    // blobbsSince holds ONE_HOUR already
                    break;
            }
        }

        List<FeedBlobb> feed;

        if (by != null && by.equalsIgnoreCase("POPULARITY")) {
            // if 'by' is provided and contains 'POPULARITY'
            // get most popular posts
            feed = blobbService.getFeedForUser_Popular(loggedUser, blobbsSince, skip, limit);
        } else {
            // if 'by' is not provided or has some different value
            // get most recent posts
            feed = blobbService.getFeedForUser(loggedUser, blobbsSince, skip, limit);
        }

        return new ResponseEntity<>(feed, HttpStatus.OK);
    }
}
