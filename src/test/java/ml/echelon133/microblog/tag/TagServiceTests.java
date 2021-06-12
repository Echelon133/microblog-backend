package ml.echelon133.microblog.tag;

import ml.echelon133.microblog.tag.exception.TagDoesntExistException;
import ml.echelon133.microblog.tag.model.RecentPost;
import ml.echelon133.microblog.tag.model.Tag;
import ml.echelon133.microblog.tag.repository.TagRepository;
import ml.echelon133.microblog.tag.service.ITagService;
import ml.echelon133.microblog.tag.service.TagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TagServiceTests {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    public void findByUuid_ThrowsWhenTagDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(tagRepository.findById(uuid)).willReturn(Optional.empty());

        // when
        String message = assertThrows(TagDoesntExistException.class, () -> {
            tagService.findByUuid(uuid);
        }).getMessage();

        // then
        assertEquals(String.format("Tag with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void findByUuid_ReturnsObject() throws Exception {
        UUID uuid = UUID.randomUUID();
        Tag tag = new Tag("test");

        // given
        given(tagRepository.findById(uuid)).willReturn(Optional.of(tag));

        // when
        Tag foundTag = tagService.findByUuid(uuid);

        // then
        assertEquals(tag, foundTag);
    }

    @Test
    public void findByName_ThrowsWhenTagDoesntExist() {
        String name = "test";

        // given
        given(tagRepository.findByName(name)).willReturn(Optional.empty());

        // when
        String message = assertThrows(TagDoesntExistException.class, () -> {
            tagService.findByName(name);
        }).getMessage();

        // then
        assertEquals(String.format("Tag #%s doesn't exist", name), message);
    }

    @Test
    public void findByName_ReturnsObject() throws Exception {
        String name = "test";
        Tag tag = new Tag(name);

        // given
        given(tagRepository.findByName(name)).willReturn(Optional.of(tag));

        // when
        Tag foundTag = tagService.findByName(name);

        // then
        assertEquals(tag, foundTag);
    }

    @Test
    public void findMostPopular_ThrowsWhenLimitNegative() {

        // when
        String message = assertThrows(IllegalArgumentException.class, () -> {
            tagService.findMostPopular(-1L, ITagService.PopularSince.ONE_HOUR);
        }).getMessage();

        // then
        assertEquals("Limit cannot be negative", message);
    }

    @Test
    public void findMostPopular_CorrectlyCalculatesPastDates() {
        Date now = new Date();
        Clock fixedClock = Clock.fixed(now.toInstant(), ZoneId.systemDefault());
        Date hourAgo = Date.from(fixedClock.instant().minus(1, HOURS));
        Date dayAgo = Date.from(fixedClock.instant().minus(1, DAYS));
        Date weekAgo = Date.from(fixedClock.instant().minus(7, DAYS));

        tagService.setClock(fixedClock);

        // given
        given(tagRepository.findMostPopularTags_Between(hourAgo, now, 10L))
                .willReturn(List.of(new Tag()));
        given(tagRepository.findMostPopularTags_Between(dayAgo, now, 10L))
                .willReturn(List.of(new Tag(), new Tag()));
        given(tagRepository.findMostPopularTags_Between(weekAgo, now, 10L))
                .willReturn(List.of(new Tag(), new Tag(), new Tag()));

        // when
        List<Tag> popularHourAgo = tagService
                .findMostPopular(10L, ITagService.PopularSince.ONE_HOUR);
        List<Tag> popularDayAgo = tagService
                .findMostPopular(10L, ITagService.PopularSince.DAY);
        List<Tag> popularWeekAgo = tagService
                .findMostPopular(10L, ITagService.PopularSince.WEEK);

        // then
        assertEquals(1L, popularHourAgo.size());
        assertEquals(2L, popularDayAgo.size());
        assertEquals(3L, popularWeekAgo.size());
    }

    @Test
    public void findRecentPostsTagged_ThrowsIfTagDoesntExist() {
        UUID uuid = UUID.randomUUID();

        // given
        given(tagRepository.existsById(uuid)).willReturn(false);

        // then
        String message = assertThrows(TagDoesntExistException.class, () -> {
            tagService.findRecentPostsTagged(uuid, 0L, 10L);
        }).getMessage();

        assertEquals(String.format("Tag with UUID %s doesn't exist", uuid), message);
    }

    @Test
    public void findRecentPostsTagged_ThrowsIfSkipIsNegative() {
        UUID uuid = UUID.randomUUID();

        // given
        given(tagRepository.existsById(uuid)).willReturn(true);

        // then
        String message = assertThrows(IllegalArgumentException.class, () -> {
            tagService.findRecentPostsTagged(uuid, -1L, 10L);
        }).getMessage();

        assertEquals("Invalid skip and/or limit values.", message);
    }

    @Test
    public void findRecentPostsTagged_ThrowsIfLimitIsNegative() {
        UUID uuid = UUID.randomUUID();

        // given
        given(tagRepository.existsById(uuid)).willReturn(true);

        // then
        String message = assertThrows(IllegalArgumentException.class, () -> {
            tagService.findRecentPostsTagged(uuid, 0L, -10L);
        }).getMessage();

        assertEquals("Invalid skip and/or limit values.", message);
    }

    @Test
    public void findRecentPostsTagged_ReturnsObjects() throws Exception {
        UUID uuid = UUID.randomUUID();

        // given
        given(tagRepository.existsById(uuid)).willReturn(true);
        given(tagRepository.findRecentPostsTagged(uuid, 0L, 10L))
                .willReturn(List.of(new RecentPost(), new RecentPost()));

        // when
        List<RecentPost> recent = tagService.findRecentPostsTagged(uuid, 0L, 10L);

        // then
        assertEquals(2, recent.size());
    }
}
