package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class BlobbService implements IBlobbService {

    private BlobbRepository blobbRepository;
    private Clock clock = Clock.systemDefaultZone();

    @Autowired
    public BlobbService(BlobbRepository blobbRepository) {
        this.blobbRepository = blobbRepository;
    }

    private void throwIfBlobbDoesntExist(UUID uuid) throws BlobbDoesntExistException {
        if (!blobbRepository.existsById(uuid)) {
            throw new BlobbDoesntExistException(uuid);
        }
    }

    @Override
    public FeedBlobb getByUuid(UUID uuid) throws BlobbDoesntExistException {
        Optional<FeedBlobb> blobb = blobbRepository.getBlobbWithUuid(uuid);
        if (blobb.isPresent()) {
            return blobb.get();
        }
        throw new BlobbDoesntExistException(uuid);
    }

    @Override
    public BlobbInfo getBlobbInfo(UUID uuid) throws BlobbDoesntExistException {
        Optional<BlobbInfo> info = blobbRepository.getInfoAboutBlobbWithUuid(uuid);
        if (info.isPresent()) {
            return info.get();
        }
        throw new BlobbDoesntExistException(uuid);
    }

    @Override
    public List<FeedBlobb> getAllResponsesTo(UUID uuid, Long skip, Long limit) throws BlobbDoesntExistException, IllegalArgumentException {
        throwIfBlobbDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return blobbRepository.getAllResponsesToBlobbWithUuid(uuid, skip, limit);
    }

    @Override
    public List<FeedBlobb> getAllReblobbsOf(UUID uuid, Long skip, Long limit) throws BlobbDoesntExistException, IllegalArgumentException {
        throwIfBlobbDoesntExist(uuid);
        if (skip < 0 || limit < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }
        return blobbRepository.getAllReblobbsOfBlobbWithUuid(uuid, skip, limit);

    }

    @Override
    public boolean checkIfUserWithUuidLikes(User user, UUID blobbUuid) throws BlobbDoesntExistException {
        throwIfBlobbDoesntExist(blobbUuid);
        return blobbRepository.checkIfUserWithUuidLikes(user.getUuid(), blobbUuid).isPresent();
    }

    @Override
    public boolean likeBlobb(User user, UUID blobbUuid) throws BlobbDoesntExistException {
        throwIfBlobbDoesntExist(blobbUuid);
        Optional<Long> like = blobbRepository.checkIfUserWithUuidLikes(user.getUuid(), blobbUuid);

        // only like if there is no already existing 'likes' relationship
        // between the user and the post
        // otherwise this will create duplicate relationships
        if (like.isEmpty()) {
            like = blobbRepository.likeBlobbWithUuid(user.getUuid(), blobbUuid);
        }
        return like.isPresent();
    }

    @Override
    public boolean unlikeBlobb(User user, UUID blobbUuid) throws BlobbDoesntExistException {
        throwIfBlobbDoesntExist(blobbUuid);
        blobbRepository.unlikeBlobbWithUuid(user.getUuid(), blobbUuid);
        return blobbRepository.checkIfUserWithUuidLikes(user.getUuid(), blobbUuid).isEmpty();
    }

    @Override
    public List<FeedBlobb> getFeedForUser(User user, BlobbsSince since, Long skip, Long limit) throws IllegalArgumentException {

        if (limit < 0 || skip < 0) {
            throw new IllegalArgumentException("Invalid skip and/or limit values.");
        }

        int hoursToSubtract = since.getHours();
        Date now = Date.from(Instant.now(clock));
        Date before =  Date.from(now.toInstant().minus(hoursToSubtract, HOURS));
        return blobbRepository
                .getFeedForUserWithUuid_PostedBetween(user.getUuid(), before, now, skip, limit);
    }

    @Override
    public Blobb processBlobbAndSave(Blobb blobb) {
        return null;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
