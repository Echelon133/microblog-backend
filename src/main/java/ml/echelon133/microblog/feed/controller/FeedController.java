package ml.echelon133.microblog.feed.controller;

import ml.echelon133.microblog.post.service.IPostService;
import ml.echelon133.microblog.user.model.UserPost;
import ml.echelon133.microblog.user.model.UserPrincipal;
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

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private IPostService postService;

    @Autowired
    public FeedController(IPostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<UserPost>> getUserFeed(@RequestParam(required = false) Long skip,
                                                      @RequestParam(required = false) Long limit) throws Exception {

        if (skip == null) {
            skip = 0L;
        }

        if (limit == null) {
            limit = 20L;
        }

        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<UserPost> feed = postService.getFeedForUser(loggedUser, skip, limit);
        return new ResponseEntity<>(feed, HttpStatus.OK);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<UserPost>> getUserFeedPopular(@RequestParam(required = false) Long skip,
                                                             @RequestParam(required = false) Long limit) throws Exception {

        if (skip == null) {
            skip = 0L;
        }

        if (limit == null) {
            limit = 20L;
        }

        List<UserPost> feed;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            feed = postService.getFeedForAnonymousUser(skip, limit);
        } else {
            UserPrincipal loggedUser = (UserPrincipal) auth.getPrincipal();
            feed = postService.getFeedForUser_Popular(loggedUser, skip, limit);
        }

        return new ResponseEntity<>(feed, HttpStatus.OK);
    }
}
