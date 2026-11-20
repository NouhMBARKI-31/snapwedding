import { useEffect, useState } from 'react';
import { Auth } from '@/api/client';
import { ImageVariant, mediaApi } from '@/api/media';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Skeleton } from '@/components/ui/skeleton';

/** Affiche une photo protégée : on la télécharge avec le bon jeton, puis on l'affiche via une URL locale. */
export function ProtectedImage({ mediaId, auth, alt, className, variant = 'thumbnail' }: {
  mediaId: number; auth: Auth; alt: string; className?: string; variant?: ImageVariant;
}) {
  const [url, setUrl] = useState<string | null>(null);

  useEffect(() => {
    let local: string | null = null;
    mediaApi.imageUrl(mediaId, auth, variant).then((u) => { local = u; setUrl(u); }).catch(() => setUrl(null));
    return () => { if (local) URL.revokeObjectURL(local); }; // libère la mémoire
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mediaId, variant]);

  if (!url) return <Skeleton className={className} />;
  return <img src={url} alt={alt} loading="lazy" className={className} />;
}

/**
 * Dans les grilles : la MINIATURE (légère, option "miniatures asynchrones").
 * Un clic ouvre la photo originale en grand.
 */
export function ZoomablePhoto({ mediaId, auth, alt, className }: {
  mediaId: number; auth: Auth; alt: string; className?: string;
}) {
  const [open, setOpen] = useState(false);

  return (
    <>
      <button type="button" className="block w-full" onClick={() => setOpen(true)}>
        <ProtectedImage mediaId={mediaId} auth={auth} alt={alt} className={className} />
      </button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-3xl p-2">
          <DialogTitle className="sr-only">{alt}</DialogTitle>
          {open && (
            <ProtectedImage mediaId={mediaId} auth={auth} alt={alt} variant="content"
                            className="w-full min-h-48 max-h-[80vh] object-contain" />
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
