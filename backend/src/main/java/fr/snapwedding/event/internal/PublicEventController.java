package fr.snapwedding.event.internal;

import fr.snapwedding.shared.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Route publique (sans compte) : la page d'arrivée après le scan du QR code (I1). */
@RestController
@RequiredArgsConstructor
@Tag(name = "Public", description = "Accès sans compte, par le code du QR code")
class PublicEventController {

    private final EventService service;

    @GetMapping("/api/public/events/{code}")
    @Operation(summary = "Infos publiques d'un événement à partir de son code (I1, I6)")
    PublicEventResponse byCode(@PathVariable String code) {
        return service.findByPublicCode(code)
                .map(PublicEventResponse::from)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }
}
