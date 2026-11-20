package fr.snapwedding.media.internal;

import fr.snapwedding.album.AlbumApi;
import fr.snapwedding.album.AlbumView;
import fr.snapwedding.event.EventApi;
import fr.snapwedding.event.EventStatus;
import fr.snapwedding.event.EventView;
import fr.snapwedding.guest.GuestApi;
import fr.snapwedding.guest.GuestView;
import fr.snapwedding.shared.ForbiddenException;
import fr.snapwedding.shared.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests unitaires des règles de dépôt et de suppression, tout le reste est mocké. */
@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    private static final GuestView YASSINE = new GuestView(1L, 7L, "Yassine");

    @Mock MediaRepository repository;
    @Mock MediaStorage storage;
    @Mock FileTypeChecker typeChecker;
    @Mock ThumbnailGenerator thumbnails;
    @Mock AlbumApi albums;
    @Mock EventApi events;
    @Mock GuestApi guests;
    @Mock ApplicationEventPublisher publisher;
    @Mock TransactionTemplate transaction;

    @InjectMocks
    MediaService service;

    private final MockMultipartFile photo =
            new MockMultipartFile("files", "photo.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

    @BeforeEach
    void aGuestOfEvent7() {
        lenient().when(guests.current()).thenReturn(YASSINE);
        lenient().when(albums.getInEvent(3L, 7L)).thenReturn(new AlbumView(3L, 7L, "Soirée"));
    }

    private void eventStatus(EventStatus status) {
        when(events.get(7L)).thenReturn(new EventView(7L, "Mariage", LocalDate.of(2027, 6, 12), "Toulouse",
                status, "alice", "code"));
    }

    @Test
    void noUploadOnAClosedEvent() {
        eventStatus(EventStatus.CLOSED);

        assertThatThrownBy(() -> service.upload(3L, List.of(photo))).isInstanceOf(InvalidRequestException.class);
        verify(storage, never()).put(anyString(), any(), anyLong(), anyString());
    }

    @Test
    void ifTheDatabaseFailsTheUploadedFileIsDeleted() {
        eventStatus(EventStatus.OPEN);
        when(typeChecker.detectAllowedType(photo)).thenReturn("image/jpeg");
        when(transaction.execute(any())).thenAnswer(inv -> ((TransactionCallback<?>) inv.getArgument(0)).doInTransaction(null));
        when(repository.save(any())).thenThrow(new IllegalStateException("database down"));

        assertThatThrownBy(() -> service.upload(3L, List.of(photo))).isInstanceOf(IllegalStateException.class);
        verify(storage).deleteQuietly(any()); // compensation : pas de fichier orphelin dans S3
    }

    @Test
    void aGuestCannotDeleteSomeoneElsesPhoto() {
        Media ofSomeoneElse = new Media(7L, 3L, 2L, "Sarah", "key", "p.jpg", "image/jpeg", 10);
        when(repository.findById(9L)).thenReturn(Optional.of(ofSomeoneElse));

        assertThatThrownBy(() -> service.deleteByGuest(9L)).isInstanceOf(ForbiddenException.class);
        verify(repository, never()).delete(eq(ofSomeoneElse));
    }

    @Test
    void theThumbnailIsStoredNextToThePhotoAndAttachedToIt() {
        Media media = new Media(7L, 3L, 1L, "Yassine", "events/7/albums/3/abc", "p.jpg", "image/jpeg", 10);
        when(repository.findById(9L)).thenReturn(Optional.of(media));
        when(storage.get("events/7/albums/3/abc")).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(thumbnails.toJpeg(any())).thenReturn(Optional.of(new byte[]{4, 5}));

        service.createThumbnail(9L);

        verify(storage).put(eq("events/7/albums/3/abc-thumb.jpg"), any(), eq(2L), eq("image/jpeg"));
        assertThat(media.hasThumbnail()).isTrue();
        assertThat(media.storageKeys()).containsExactly("events/7/albums/3/abc", "events/7/albums/3/abc-thumb.jpg");
    }
}
