package ml.echelon133.blobb.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
                                                     @RequestParam(required = false) Long limit) throws IllegalArgumentException {

        if (limit == null) {
            limit = 5L;
        }

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
}
