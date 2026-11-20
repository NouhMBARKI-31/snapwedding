import { useEffect, useState } from 'react';
import { Auth } from '@/api/client';
import { mediaApi } from '@/api/media';
import { Skeleton } from '@/components/ui/skeleton';

/** Affiche une photo protégée : on la télécharge avec le bon jeton, puis on l'affiche via une URL locale. */
export function ProtectedImage({ mediaId, auth, alt, className }: {
  mediaId: number; auth: Auth; alt: string; className?: string;
}) {
  const [url, setUrl] = useState<string | null>(null);

  useEffect(() => {
    let local: string | null = null;
    mediaApi.imageUrl(mediaId, auth).then((u) => { local = u; setUrl(u); }).catch(() => setUrl(null));
    return () => { if (local) URL.revokeObjectURL(local); }; // libère la mémoire
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mediaId]);

  if (!url) return <Skeleton className={className} />;
  return <img src={url} alt={alt} loading="lazy" className={className} />;
}
