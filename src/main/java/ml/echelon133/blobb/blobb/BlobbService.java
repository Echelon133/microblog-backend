package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BlobbService implements IBlobbService {

    private BlobbRepository blobbRepository;

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
        return false;
    }

    @Override
    public boolean likeBlobb(User user, UUID blobbUuid) throws BlobbDoesntExistException {
        return false;
    }

    @Override
    public boolean unlikeBlobb(User user, UUID blobbUuid) throws BlobbDoesntExistException {
        return false;
    }

    @Override
    public List<FeedBlobb> getFeedForUser(User user, BlobbsSince since, Long skip, Long limit) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Blobb processBlobbAndSave(Blobb blobb) {
        return null;
    }
}
