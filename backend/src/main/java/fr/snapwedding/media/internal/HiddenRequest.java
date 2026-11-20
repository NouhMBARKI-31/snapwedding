package fr.snapwedding.media.internal;

import jakarta.validation.constraints.NotNull;

public record HiddenRequest(@NotNull Boolean hidden) {
}
