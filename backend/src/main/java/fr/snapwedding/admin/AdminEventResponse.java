package fr.snapwedding.admin;

import fr.snapwedding.event.EventStatus;

import java.time.LocalDate;

/** Une ligne de la liste du super-admin (S1). */
public record AdminEventResponse(
        Long id,
        String name,
        LocalDate date,
        String organizerId,
        EventStatus status,
        long mediaCount,
        long storageBytes
) {
}
