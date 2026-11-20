package fr.snapwedding.album.internal;

import fr.snapwedding.album.AlbumView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Albums côté organisateur (JWT Google). */
@RestController
@RequiredArgsConstructor
@Tag(name = "Albums", description = "Albums d'un événement")
@SecurityRequirement(name = "googleJwt")
class AlbumController {

    private final AlbumService service;

    @GetMapping("/api/events/{eventId}/albums")
    @Operation(summary = "Albums d'un de mes événements")
    List<AlbumView> list(@PathVariable Long eventId) {
        return service.listForOrganizer(eventId);
    }

    @PostMapping("/api/events/{eventId}/albums")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un album (O3)")
    AlbumView create(@PathVariable Long eventId, @Valid @RequestBody AlbumRequest request) {
        return service.create(eventId, request.name());
    }

    @PutMapping("/api/albums/{id}")
    @Operation(summary = "Renommer un album")
    AlbumView rename(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
        return service.rename(id, request.name());
    }

    @DeleteMapping("/api/albums/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un album et ses photos")
    void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
