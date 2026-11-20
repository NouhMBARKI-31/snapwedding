package fr.snapwedding.event.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Routes de l'organisateur (JWT Google). Toutes filtrées par son tenant dans EventService. */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Événements de l'organisateur")
@SecurityRequirement(name = "googleJwt")
class EventController {

    private final EventService service;
    private final QrCodeService qrCodes;

    @GetMapping
    @Operation(summary = "Mes événements (O2, O8)")
    Page<EventResponse> findMine(@PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return service.findMine(pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Un de mes événements")
    EventResponse findOne(@PathVariable Long id) {
        return toResponse(service.getOwnedByCurrentOrganizer(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un événement (O2)")
    EventResponse create(@Valid @RequestBody EventRequest request) {
        return toResponse(service.create(request.name(), request.date(), request.location()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un événement (O2)")
    EventResponse update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return toResponse(service.update(id, request.name(), request.date(), request.location()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un événement, ses albums, ses photos et ses invités (O2)")
    void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Fermer l'événement : plus aucun dépôt (O7)")
    EventResponse close(@PathVariable Long id) {
        return toResponse(service.close(id));
    }

    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "QR code à imprimer, en PNG (O4)")
    ResponseEntity<byte[]> qrCode(@PathVariable Long id) {
        String code = service.getOwnedByCurrentOrganizer(id).publicCode();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCodes.png(code));
    }

    private EventResponse toResponse(fr.snapwedding.event.EventView view) {
        return EventResponse.from(view, qrCodes.guestUrl(view.publicCode()));
    }
}
