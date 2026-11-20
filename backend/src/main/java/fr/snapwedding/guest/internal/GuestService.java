package fr.snapwedding.guest.internal;

import fr.snapwedding.event.EventApi;
import fr.snapwedding.event.EventView;
import fr.snapwedding.guest.GuestApi;
import fr.snapwedding.guest.GuestView;
import fr.snapwedding.shared.ForbiddenException;
import fr.snapwedding.shared.InvalidRequestException;
import fr.snapwedding.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class GuestService implements GuestApi {

    private final GuestRepository guests;
    private final GuestTokens tokens;
    private final EventApi events;

    /** Résultat de "rejoindre" : le jeton en clair n'existe qu'ici, une seule fois. */
    record Joined(String token, GuestView guest, EventView event) {
    }

    /**
     * I2 : un visiteur qui a le code de l'événement (QR code) choisit un pseudo et reçoit un jeton.
     * Sans le bon code : 404 (I6).
     */
    @Transactional
    Joined join(String publicCode, String pseudo) {
        EventView event = events.findByPublicCode(publicCode)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!event.isOpen()) {
            throw new InvalidRequestException("Event is " + event.status().name().toLowerCase());
        }

        String token = tokens.generate();
        Guest guest = guests.save(new Guest(event.id(), pseudo.trim(), tokens.hash(token)));
        return new Joined(token, guest.toView(), event);
    }

    EventView eventOf(GuestView guest) {
        return events.get(guest.eventId());
    }

    @Override
    public GuestView current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof GuestAuthentication guestAuth) {
            return guestAuth.getPrincipal();
        }
        throw new ForbiddenException("Guest authentication required");
    }

    @Transactional
    void deleteAllOfEvent(Long eventId) {
        guests.deleteAllByEventId(eventId);
    }
}
