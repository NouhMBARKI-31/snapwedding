package fr.snapwedding.event;

import java.time.LocalDate;

/**
 * Vue en lecture seule d'un événement, donnée aux autres modules.
 * Les autres modules ne voient jamais l'entité JPA : ils ne peuvent pas la modifier en douce.
 */
public record EventView(
        Long id,
        String name,
        LocalDate date,
        String location,
        EventStatus status,
        String organizerId,
        String publicCode
) {
    /** Seul un événement ouvert accepte des invités et des dépôts. */
    public boolean isOpen() {
        return status == EventStatus.OPEN;
    }
}
