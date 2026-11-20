package fr.snapwedding.guest.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest(@NotBlank @Size(max = 40) String pseudo) {
}
