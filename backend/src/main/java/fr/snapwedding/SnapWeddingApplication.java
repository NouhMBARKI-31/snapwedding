package fr.snapwedding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Point d'entrée. Chaque package de premier niveau (event, album, media, guest, admin, shared)
 * est un module Spring Modulith : voir ModularityTest.
 */
@Modulithic(sharedModules = "shared")
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync // les @ApplicationModuleListener s'exécutent en asynchrone
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class SnapWeddingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapWeddingApplication.class, args);
    }
}
