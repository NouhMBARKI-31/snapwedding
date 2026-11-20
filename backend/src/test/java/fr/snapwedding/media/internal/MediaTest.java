package fr.snapwedding.media.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MediaTest {

    private static Media photoOfGuest(Long guestId) {
        return new Media(7L, 3L, guestId, "Yassine", "key", "photo.jpg", "image/jpeg", 1000);
    }

    @Test
    void onlyTheAuthorOwnsAPhoto() {
        Media media = photoOfGuest(1L);
        assertThat(media.isOwnedByGuest(1L)).isTrue();
        assertThat(media.isOwnedByGuest(2L)).isFalse();
    }

    @Test
    void aHiddenPhotoIsInvisibleToGuests() {
        Media media = photoOfGuest(1L);
        assertThat(media.isVisibleToGuestOf(7L)).isTrue();

        media.setHidden(true);
        assertThat(media.isVisibleToGuestOf(7L)).isFalse();
    }

    @Test
    void aPhotoIsInvisibleToGuestsOfAnotherEvent() {
        assertThat(photoOfGuest(1L).isVisibleToGuestOf(8L)).isFalse();
    }
}
