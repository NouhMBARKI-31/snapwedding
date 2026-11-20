package fr.snapwedding.media.internal;

import fr.snapwedding.guest.GuestApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Photos côté invité (jeton X-Guest-Token) : déposer, voir, supprimer les siennes (I3, I4, I5). */
@RestController
@RequiredArgsConstructor
@Tag(name = "Media (guest)", description = "Dépôt et galerie des invités")
@SecurityRequirement(name = "guestToken")
class GuestMediaController {

    private final MediaService service;
    private final GuestApi guests;

    @PostMapping(value = "/api/guest/albums/{albumId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Déposer une ou plusieurs photos (I3)")
    List<MediaResponse> upload(@PathVariable Long albumId, @RequestParam("files") List<MultipartFile> files) {
        Long guestId = guests.current().id();
        return service.upload(albumId, files).stream().map(m -> MediaResponse.forGuest(m, guestId)).toList();
    }

    @GetMapping("/api/guest/albums/{albumId}/media")
    @Operation(summary = "Photos visibles d'un album (I4)")
    Page<MediaResponse> list(@PathVariable Long albumId,
                             @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC)
                             Pageable pageable) {
        Long guestId = guests.current().id();
        return service.listForGuest(albumId, pageable).map(m -> MediaResponse.forGuest(m, guestId));
    }

    @DeleteMapping("/api/guest/media/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer MA photo (I5)")
    void delete(@PathVariable Long id) {
        service.deleteByGuest(id);
    }

    @GetMapping("/api/guest/media/{id}/content")
    @Operation(summary = "Le fichier d'une photo visible")
    ResponseEntity<InputStreamResource> content(@PathVariable Long id) {
        Media media = service.getForGuest(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getContentType()))
                .contentLength(media.getSize())
                .body(new InputStreamResource(service.content(media)));
    }

    @GetMapping("/api/guest/media/{id}/thumbnail")
    @Operation(summary = "La miniature de la photo (l'original tant qu'elle n'est pas prête)")
    ResponseEntity<InputStreamResource> thumbnail(@PathVariable Long id) {
        Media media = service.getForGuest(id);
        if (!media.hasThumbnail()) {
            return content(id);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(service.thumbnail(media)));
    }
}
