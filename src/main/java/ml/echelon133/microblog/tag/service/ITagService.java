package ml.echelon133.microblog.tag.service;

import ml.echelon133.microblog.tag.model.RecentPost;
import ml.echelon133.microblog.tag.model.Tag;
import ml.echelon133.microblog.tag.exception.TagDoesntExistException;

import java.util.List;
import java.util.UUID;

public interface ITagService {

    enum PopularSince {
        ONE_HOUR, DAY, WEEK
    }

    Tag findByUuid(UUID uuid) throws TagDoesntExistException;
    Tag findByName(String name) throws TagDoesntExistException;
    List<Tag> findMostPopular(Long limit, PopularSince since) throws IllegalArgumentException;
    List<RecentPost> findRecentPostsTagged(UUID tagUuid, Long skip, Long limit) throws TagDoesntExistException,
            IllegalArgumentException;
}
