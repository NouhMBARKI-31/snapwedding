/**
 * Module "media" : les photos déposées par les invités, stockées dans S3 (RustFS).
 *
 * API publique : {@link fr.snapwedding.media.MediaApi} (statistiques pour le super-admin),
 * {@link fr.snapwedding.media.MediaStats} et l'événement {@link fr.snapwedding.media.MediaUploaded}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Media")
package fr.snapwedding.media;
