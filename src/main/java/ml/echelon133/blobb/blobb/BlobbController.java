package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/blobbs")
public class BlobbController {

    private IBlobbService blobbService;

    @Autowired
    public BlobbController(IBlobbService blobbService) {
        this.blobbService = blobbService;
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<FeedBlobb> getBlobbWithUuid(@PathVariable String uuid) throws Exception {
        return new ResponseEntity<>(
                blobbService.getByUuid(UUID.fromString(uuid)),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/info")
    public ResponseEntity<BlobbInfo> getInfoAboutBlobbWithUuid(@PathVariable String uuid) throws Exception {
        return new ResponseEntity<>(
                blobbService.getBlobbInfo(UUID.fromString(uuid)),
                HttpStatus.OK);
    }


    @GetMapping("/{uuid}/responses")
    public ResponseEntity<List<FeedBlobb>> getResponsesToBlobb(@PathVariable String uuid,
                                                               @RequestParam(required = false) Long skip,
                                                               @RequestParam(required = false) Long limit) throws Exception {
        if (skip == null) {
            skip = 0L;
        }
        if (limit == null) {
            limit = 5L;
        }

        return new ResponseEntity<>(
                blobbService.getAllResponsesTo(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/reblobbs")
    public ResponseEntity<List<FeedBlobb>> getReblobbsOfBlobb(@PathVariable String uuid,
                                                              @RequestParam(required = false) Long skip,
                                                              @RequestParam(required = false) Long limit) throws Exception {
        if (skip == null) {
            skip = 0L;
        }
        if (limit == null) {
            limit = 5L;
        }

        return new ResponseEntity<>(
                blobbService.getAllReblobbsOf(UUID.fromString(uuid), skip, limit),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/like")
    public ResponseEntity<Map<String, Boolean>> checkIfLikes(@PathVariable String uuid) throws Exception {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean result = blobbService.checkIfUserWithUuidLikes(loggedUser, UUID.fromString(uuid));

        Map<String, Boolean> response = Map.of("liked", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
