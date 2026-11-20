package fr.snapwedding.album.internal;

import fr.snapwedding.album.AlbumView;
import fr.snapwedding.guest.GuestApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Albums côté invité : ceux de SON événement, déduit du jeton (I3, I4, I6). */
@RestController
@RequiredArgsConstructor
@Tag(name = "Albums", description = "Albums d'un événement")
@SecurityRequirement(name = "guestToken")
class GuestAlbumController {

    private final AlbumService service;
    private final GuestApi guests;

    @GetMapping("/api/guest/albums")
    @Operation(summary = "Albums de mon événement (invité)")
    List<AlbumView> list() {
        return service.listOfEvent(guests.current().eventId());
    }
}
