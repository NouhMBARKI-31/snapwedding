package fr.snapwedding.media;

import java.util.Collection;
import java.util.Map;

/** Ce que le module media offre aux autres modules (utilisé par admin). */
public interface MediaApi {

    /** Statistiques de plusieurs événements en UNE requête SQL groupée (pas de N+1). */
    Map<Long, MediaStats> statsByEvent(Collection<Long> eventIds);
}
