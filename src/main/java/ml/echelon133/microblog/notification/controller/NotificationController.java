package ml.echelon133.microblog.notification.controller;

import ml.echelon133.microblog.notification.service.INotificationService;
import ml.echelon133.microblog.notification.model.NotificationResult;
import ml.echelon133.microblog.user.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private INotificationService notificationService;

    @Autowired
    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResult>> getAllNotifications(@RequestParam(required = false) Long skip,
                                                                        @RequestParam(required = false) Long limit) {

        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (skip == null) {
            skip = 0L;
        }

        if (limit == null) {
            limit = 5L;
        }

        List<NotificationResult> response = notificationService.findAllNotificationsOfUser(loggedUser, skip, limit);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/unreadCounter")
    public ResponseEntity<Map<String, Long>> getUnreadCounter() {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Map<String, Long> result = Map.of(
                "unreadCounter",
                notificationService.countUnreadNotificationsOfUser(loggedUser)
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/readAll")
    public ResponseEntity<Map<String, Long>> readAllNotifications() {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Map<String, Long> result = Map.of(
                "markedAsRead",
                notificationService.readAllNotificationsOfUser(loggedUser)
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/read")
    public ResponseEntity<Map<String, Boolean>> readSingleNotification(@PathVariable String uuid) {
        UserPrincipal loggedUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID notificationUuid = UUID.fromString(uuid);
        Map<String, Boolean> result = Map.of(
                "read",
                notificationService.readSingleNotificationOfUser(loggedUser, notificationUuid)
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
