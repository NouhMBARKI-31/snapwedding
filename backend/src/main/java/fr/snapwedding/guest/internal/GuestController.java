package fr.snapwedding.guest.internal;

import fr.snapwedding.event.EventView;
import fr.snapwedding.guest.GuestView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Guests", description = "Invités sans compte")
class GuestController {

    private final GuestService service;

    @PostMapping("/api/public/events/{code}/join")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Rejoindre un événement avec un pseudo et recevoir un jeton d'invité (I2)")
    GuestSessionResponse join(@PathVariable String code, @Valid @RequestBody JoinRequest request) {
        GuestService.Joined joined = service.join(code, request.pseudo());
        return new GuestSessionResponse(joined.token(), joined.guest().id(), joined.guest().pseudo(),
                joined.event().id(), joined.event().name());
    }

    @GetMapping("/api/guest/me")
    @SecurityRequirement(name = "guestToken")
    @Operation(summary = "L'invité connecté et son événement")
    GuestSessionResponse me() {
        GuestView guest = service.current();
        EventView event = service.eventOf(guest);
        return new GuestSessionResponse(null, guest.id(), guest.pseudo(), event.id(), event.name());
    }
}
