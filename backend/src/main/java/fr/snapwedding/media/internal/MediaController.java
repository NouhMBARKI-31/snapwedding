package fr.snapwedding.media.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Photos côté organisateur (JWT Google) : voir tout, modérer (O5, O6). */
@RestController
@RequiredArgsConstructor
@Tag(name = "Media (organizer)", description = "Photos et modération")
@SecurityRequirement(name = "googleJwt")
class MediaController {

    private final MediaService service;

    @GetMapping("/api/albums/{albumId}/media")
    @Operation(summary = "Toutes les photos d'un album, avec le pseudo de l'invité (O5)")
    Page<MediaResponse> list(@PathVariable Long albumId,
                             @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC)
                             Pageable pageable) {
        return service.listForOrganizer(albumId, pageable).map(MediaResponse::forOrganizer);
    }

    @PatchMapping("/api/media/{id}")
    @Operation(summary = "Masquer ou réafficher une photo (O6)")
    MediaResponse setHidden(@PathVariable Long id, @Valid @RequestBody HiddenRequest request) {
        return MediaResponse.forOrganizer(service.setHidden(id, request.hidden()));
    }

    @DeleteMapping("/api/media/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une photo (O6)")
    void delete(@PathVariable Long id) {
        service.deleteByOrganizer(id);
    }

    @GetMapping("/api/media/{id}/content")
    @Operation(summary = "Le fichier de la photo")
    ResponseEntity<InputStreamResource> content(@PathVariable Long id) {
        Media media = service.getForOrganizer(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getContentType()))
                .contentLength(media.getSize())
                .body(new InputStreamResource(service.content(media)));
    }
}
