package ml.echelon133.microblog.tag.controller;

import ml.echelon133.microblog.tag.service.ITagService;
import ml.echelon133.microblog.tag.model.RecentPost;
import ml.echelon133.microblog.tag.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private ITagService tagService;

    @Autowired
    public TagController(ITagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<Tag> getTagByName(@RequestParam String name) throws Exception {
        Tag tag = tagService.findByName(name);
        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Tag>> listPopularTags(@RequestParam(required = false) String since,
                                                     @RequestParam(defaultValue = "5") Long limit) throws IllegalArgumentException {
        ITagService.PopularSince popularSince = ITagService.PopularSince.ONE_HOUR;
        if (since != null) {
            switch(since.toUpperCase()) {
                case "DAY":
                    popularSince = ITagService.PopularSince.DAY;
                    break;
                case "WEEK":
                    popularSince = ITagService.PopularSince.WEEK;
                    break;
                case "HOUR":
                default:
                    popularSince = ITagService.PopularSince.ONE_HOUR;
            }
        }

        List<Tag> result = tagService.findMostPopular(limit, popularSince);
        return new ResponseEntity<>(
                result, HttpStatus.OK
        );
    }

    @GetMapping("/{uuid}/recentPosts")
    public ResponseEntity<List<RecentPost>> findRecentPosts(@PathVariable String uuid,
                                                            @RequestParam(defaultValue = "0") Long skip,
                                                            @RequestParam(defaultValue = "5") Long limit) throws Exception {
        List<RecentPost> recent = tagService.findRecentPostsTagged(UUID.fromString(uuid), skip, limit);
        return new ResponseEntity<>(recent, HttpStatus.OK);
    }
}
