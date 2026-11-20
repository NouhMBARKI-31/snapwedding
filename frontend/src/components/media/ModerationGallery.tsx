import { useEffect, useState } from 'react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ORGANIZER } from '@/api/client';
import { MediaDto, mediaApi } from '@/api/media';
import { ProtectedImage } from './ProtectedImage';

/** Organisateur : toutes les photos d'un album avec le pseudo de l'invité (O5), masquer / supprimer (O6). */
export function ModerationGallery({ albumId }: { albumId: number }) {
  const [media, setMedia] = useState<MediaDto[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const load = (p: number) =>
    mediaApi.list(albumId, p).then((res) => {
      setMedia((prev) => (p === 0 ? res.content : [...prev, ...res.content]));
      setPage(res.page.number);
      setTotalPages(res.page.totalPages);
    }).catch((e) => toast.error(e.message));

  useEffect(() => { load(0); }, [albumId]); // eslint-disable-line react-hooks/exhaustive-deps

  const toggleHidden = async (item: MediaDto) => {
    const updated = await mediaApi.setHidden(item.id, !item.hidden);
    setMedia((list) => list.map((m) => (m.id === updated.id ? updated : m)));
  };

  const remove = async (item: MediaDto) => {
    await mediaApi.remove(item.id);
    setMedia((list) => list.filter((m) => m.id !== item.id));
  };

  if (media.length === 0) return <p className="text-sm text-muted-foreground">Aucune photo dans cet album.</p>;

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
        {media.map((item) => (
          <div key={item.id} className="rounded-md border overflow-hidden bg-background">
            <ProtectedImage mediaId={item.id} auth={ORGANIZER} alt={item.originalFilename}
                            className={`w-full aspect-square object-cover ${item.hidden ? 'opacity-30' : ''}`} />
            <div className="p-2 space-y-2">
              <div className="flex items-center justify-between gap-2 text-xs">
                <span className="truncate">par {item.guestPseudo}</span>
                {item.hidden && <Badge variant="secondary">Masquée</Badge>}
              </div>
              <div className="flex gap-2">
                <Button size="sm" variant="outline" className="flex-1"
                        onClick={() => toggleHidden(item).catch((e) => toast.error(e.message))}>
                  {item.hidden ? 'Afficher' : 'Masquer'}
                </Button>
                <Button size="sm" variant="destructive"
                        onClick={() => remove(item).catch((e) => toast.error(e.message))}>Suppr.</Button>
              </div>
            </div>
          </div>
        ))}
      </div>
      {page + 1 < totalPages && (
        <Button variant="outline" className="w-full" onClick={() => load(page + 1)}>Voir plus</Button>
      )}
    </div>
  );
}
