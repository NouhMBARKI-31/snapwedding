package fr.snapwedding;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Vérifie l'architecture : pas de cycle entre modules, et aucun module n'utilise
 * les classes internes (sous-package internal) d'un autre.
 */
class ModularityTest {

    static final ApplicationModules modules = ApplicationModules.of(SnapWeddingApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    /**
     * Génère la documentation des modules (diagrammes C4 / PlantUML + fiches) dans
     * target/spring-modulith-docs. À recopier dans docs/ et commiter (livrable).
     */
    @Test
    void writesDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
