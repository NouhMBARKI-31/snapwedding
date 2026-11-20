package fr.snapwedding.event.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Corps de POST et PUT /api/events. */
public record EventRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull LocalDate date,
        @Size(max = 200) String location
) {
}
