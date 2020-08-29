package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.user.User;

import java.util.List;
import java.util.UUID;

public interface IBlobbService {

    enum BlobbsSince {
        ONE_HOUR(1),
        SIX_HOURS(6),
        TWELVE_HOURS(12);

        private int hours;

        BlobbsSince(int i) {
            this.hours = i;
        }

        int getHours() {
            return hours;
        }
    }

    FeedBlobb getByUuid(UUID uuid) throws BlobbDoesntExistException;
    BlobbInfo getBlobbInfo(UUID uuid) throws BlobbDoesntExistException;
    List<FeedBlobb> getAllResponsesTo(UUID uuid, Long skip, Long limit) throws BlobbDoesntExistException, IllegalArgumentException;
    List<FeedBlobb> getAllReblobbsOf(UUID uuid, Long skip, Long limit) throws BlobbDoesntExistException, IllegalArgumentException;
    boolean checkIfUserWithUuidLikes(User user, UUID blobbUuid) throws BlobbDoesntExistException;
    boolean likeBlobb(User user, UUID blobbUuid) throws BlobbDoesntExistException;
    boolean unlikeBlobb(User user, UUID blobbUuid) throws BlobbDoesntExistException;
    List<FeedBlobb> getFeedForUser(User user, BlobbsSince since, Long skip, Long limit) throws IllegalArgumentException;
    Blobb processBlobbAndSave(Blobb blobb);
}
