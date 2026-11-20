package fr.snapwedding.shared;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Propriétés "snapwedding.*" de application.yaml.
 *
 * @param frontUrl    URL du front, encodée dans le QR code
 * @param superAdmins claims "sub" des comptes Google super-admin
 */
@ConfigurationProperties(prefix = "snapwedding")
public record SnapWeddingProperties(String frontUrl, List<String> superAdmins) {

    public SnapWeddingProperties {
        superAdmins = superAdmins == null ? List.of() : superAdmins.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
