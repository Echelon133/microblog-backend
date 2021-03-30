package ml.echelon133.microblog.post;

import ml.echelon133.microblog.user.User;
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

    @Autowired
    public PostController(IPostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<FeedPost> getPostWithUuid(@PathVariable String uuid) throws Exception {
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
    public ResponseEntity<List<FeedPost>> getResponsesToPost(@PathVariable String uuid,
                                                              @RequestParam(required = false) Long skip,
                                                              @RequestParam(required = false) Long limit) throws Exception {
        if (skip == null) {
            skip = 0L;
        }
        if (limit == null) {
            limit = 5L;
        }

        return new ResponseEntity<>(
                postService.getAllResponsesTo(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/quotes")
    public ResponseEntity<List<FeedPost>> getQuotesOfPost(@PathVariable String uuid,
                                                             @RequestParam(required = false) Long skip,
                                                             @RequestParam(required = false) Long limit) throws Exception {
        if (skip == null) {
            skip = 0L;
        }
        if (limit == null) {
            limit = 5L;
        }

        return new ResponseEntity<>(
                postService.getAllQuotesOf(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/like")
    public ResponseEntity<Map<String, Boolean>> checkIfLikes(@PathVariable String uuid) throws Exception {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = postService.checkIfUserWithUuidLikes(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("liked", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/like")
    public ResponseEntity<Map<String, Boolean>> likePost(@PathVariable String uuid) throws Exception {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = postService.likePost(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("liked", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/unlike")
    public ResponseEntity<Map<String, Boolean>> unlikePost(@PathVariable String uuid) throws Exception {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = postService.unlikePost(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("unliked", result);
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

        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Post savedPost = postService.postPost(loggedUser, postDto.getContent());

        return new ResponseEntity<>(
                Map.of("uuid", savedPost.getUuid().toString()),
                HttpStatus.OK
        );
    }

    @PostMapping("/{uuid}/respond")
    public ResponseEntity<Map<String, String>> respondToPost(@PathVariable String uuid,
                                                              @Valid @RequestBody ResponseDto responseDto,
                                                              BindingResult result) throws Exception {

        if (result.hasErrors()) {
            if (result.getFieldError() != null)
                throw new InvalidPostContentException(result.getFieldError().getDefaultMessage());
            else
                throw new InvalidPostContentException();
        }

        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Post savedResponse = postService.postResponse(loggedUser, responseDto.getContent(), UUID.fromString(uuid));

        return new ResponseEntity<>(
                Map.of("uuid", savedResponse.getUuid().toString()),
                HttpStatus.OK
        );
    }

    @PostMapping("/{uuid}/quote")
    public ResponseEntity<Map<String, String>> quoteOfPost(@PathVariable String uuid,
                                                              @Valid @RequestBody QuotePostDto quotePostDto,
                                                              BindingResult result) throws Exception {

        if (result.hasErrors()) {
            if (result.getFieldError() != null)
                throw new InvalidPostContentException(result.getFieldError().getDefaultMessage());
            else
                throw new InvalidPostContentException();
        }

        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Post savedQuote = postService.postQuote(loggedUser, quotePostDto.getContent(), UUID.fromString(uuid));

        return new ResponseEntity<>(
                Map.of("uuid", savedQuote.getUuid().toString()),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Map<String, Boolean>> deletePost(@PathVariable String uuid) throws Exception {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean deleted = postService.markPostAsDeleted(loggedUser, UUID.fromString(uuid));

        return new ResponseEntity<>(
                Map.of("deleted", deleted),
                HttpStatus.OK
        );
    }
}
