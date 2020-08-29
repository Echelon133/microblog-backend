package ml.echelon133.blobb.blobb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
}
