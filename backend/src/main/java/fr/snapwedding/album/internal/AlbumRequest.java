package fr.snapwedding.album.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlbumRequest(@NotBlank @Size(max = 80) String name) {
}
