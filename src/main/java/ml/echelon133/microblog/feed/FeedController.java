package ml.echelon133.microblog.feed;

import ml.echelon133.microblog.post.FeedPost;
import ml.echelon133.microblog.post.IPostService;
import ml.echelon133.microblog.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static ml.echelon133.microblog.post.IPostService.PostsSince.*;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private IPostService postService;

    @Autowired
    public FeedController(IPostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<FeedPost>> getUserFeed(@RequestParam(required = false) String since,
                                                      @RequestParam(required = false) String by,
                                                      @RequestParam(required = false) Long skip,
                                                      @RequestParam(required = false) Long limit) throws IllegalArgumentException {

        if (skip == null) {
            skip = 0L;
        }

        if (limit == null) {
            limit = 20L;
        }

        IPostService.PostsSince postsSince = ONE_HOUR;
        if (since != null) {
            switch(since.toUpperCase()) {
                case "SIX_HOURS":
                    postsSince = SIX_HOURS;
                    break;
                case "TWELVE_HOURS":
                    postsSince = TWELVE_HOURS;
                    break;
                case "DAY":
                    postsSince = DAY;
                    break;
                case "HOUR":
                default:
                    // postsSince holds ONE_HOUR already
                    break;
            }
        }

        List<FeedPost> feed;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            feed = postService.getFeedForAnonymousUser(postsSince, skip, limit);
        } else {
            User loggedUser = (User) auth.getPrincipal();
            if (by != null && by.equalsIgnoreCase("POPULARITY")) {
                // if 'by' is provided and contains 'POPULARITY'
                // get most popular posts
                feed = postService.getFeedForUser_Popular(loggedUser, postsSince, skip, limit);
            } else {
                // if 'by' is not provided or has some different value
                // get most recent posts
                feed = postService.getFeedForUser(loggedUser, postsSince, skip, limit);
            }
        }

        return new ResponseEntity<>(feed, HttpStatus.OK);
    }
}
