package ml.echelon133.microblog.post.controller;

import ml.echelon133.microblog.post.exception.InvalidPostContentException;
import ml.echelon133.microblog.post.model.*;
import ml.echelon133.microblog.post.service.IPostService;
import ml.echelon133.microblog.user.model.UserPost;
import ml.echelon133.microblog.user.service.IUserService;
import ml.echelon133.microblog.user.model.User;
import ml.echelon133.microblog.user.model.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/posts")
public class PostController {

    private IPostService postService;
    private IUserService userService;

    @Autowired
    public PostController(IPostService postService, IUserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UserPost> getPostWithUuid(@PathVariable String uuid) throws Exception {
        return new ResponseEntity<>(
                postService.getByUuid(UUID.fromString(uuid)),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/info")
    public ResponseEntity<PostInfo> getInfoAboutPostWithUuid(@PathVariable String uuid) throws Exception {
        return new ResponseEntity<>(
                postService.getPostInfo(UUID.fromString(uuid)),
                HttpStatus.OK);
    }


    @GetMapping("/{uuid}/responses")
    public ResponseEntity<List<UserPost>> getResponsesToPost(@PathVariable String uuid,
                                                             @RequestParam(defaultValue = "0") Long skip,
                                                             @RequestParam(defaultValue = "5") Long limit) throws Exception {
        return new ResponseEntity<>(
                postService.getAllResponsesTo(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/quotes")
    public ResponseEntity<List<UserPost>> getQuotesOfPost(@PathVariable String uuid,
                                                          @RequestParam(defaultValue = "0") Long skip,
                                                          @RequestParam(defaultValue = "5") Long limit) throws Exception {
        return new ResponseEntity<>(
                postService.getAllQuotesOf(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/like")
    public ResponseEntity<Map<String, Boolean>> checkIfLikes(@PathVariable String uuid) throws Exception {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = postService.checkIfUserWithUuidLikes(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("likes", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/like")
    public ResponseEntity<Map<String, Boolean>> likePost(@PathVariable String uuid) throws Exception {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = postService.likePost(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("likes", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}/like")
    public ResponseEntity<Map<String, Boolean>> unlikePost(@PathVariable String uuid) throws Exception {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean result = postService.unlikePost(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("likes", !result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Map<String, String>> postPost(@Valid @RequestBody PostDto postDto, BindingResult result) throws Exception {

        if (result.hasErrors()) {
            if (result.getFieldError() != null)
                throw new InvalidPostContentException(result.getFieldError().getDefaultMessage());
            else
                throw new InvalidPostContentException();
        }

        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByUsername(loggedUser.getUsername());
        Post savedPost = postService.postPost(user, postDto.getContent());

        return new ResponseEntity<>(
                Map.of("uuid", savedPost.getUuid().toString()),
                HttpStatus.OK
        );
    }

    @PostMapping("/{uuid}/responses")
    public ResponseEntity<Map<String, String>> respondToPost(@PathVariable String uuid,
                                                             @Valid @RequestBody ResponseDto responseDto,
                                                             BindingResult result) throws Exception {

        if (result.hasErrors()) {
            if (result.getFieldError() != null)
                throw new InvalidPostContentException(result.getFieldError().getDefaultMessage());
            else
                throw new InvalidPostContentException();
        }

        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByUsername(loggedUser.getUsername());
        Post savedResponse = postService.postResponse(user, responseDto.getContent(), UUID.fromString(uuid));

        return new ResponseEntity<>(
                Map.of("uuid", savedResponse.getUuid().toString()),
                HttpStatus.OK
        );
    }

    @PostMapping("/{uuid}/quotes")
    public ResponseEntity<Map<String, String>> quoteOfPost(@PathVariable String uuid,
                                                           @Valid @RequestBody QuotePostDto quotePostDto,
                                                           BindingResult result) throws Exception {

        if (result.hasErrors()) {
            if (result.getFieldError() != null)
                throw new InvalidPostContentException(result.getFieldError().getDefaultMessage());
            else
                throw new InvalidPostContentException();
        }

        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByUsername(loggedUser.getUsername());
        Post savedQuote = postService.postQuote(user, quotePostDto.getContent(), UUID.fromString(uuid));

        return new ResponseEntity<>(
                Map.of("uuid", savedQuote.getUuid().toString()),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Map<String, Boolean>> deletePost(@PathVariable String uuid) throws Exception {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByUsername(loggedUser.getUsername());
        boolean deleted = postService.markPostAsDeleted(user, UUID.fromString(uuid));

        return new ResponseEntity<>(
                Map.of("deleted", deleted),
                HttpStatus.OK
        );
    }
}
