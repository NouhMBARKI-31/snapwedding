import { FormEvent, useEffect, useState } from 'react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { AlbumDto, albumsApi } from '@/api/albums';
import { ModerationGallery } from '@/components/media/ModerationGallery';

/** Organisateur : créer les albums d'un événement (O3) et modérer leurs photos. */
export function AlbumsManager({ eventId }: { eventId: number }) {
  const [albums, setAlbums] = useState<AlbumDto[]>([]);
  const [selected, setSelected] = useState<number | null>(null);
  const [name, setName] = useState('');

  useEffect(() => {
    albumsApi.list(eventId).then((list) => {
      setAlbums(list);
      setSelected(list[0]?.id ?? null);
    }).catch((e) => toast.error(e.message));
  }, [eventId]);

  const create = async (e: FormEvent) => {
    e.preventDefault();
    try {
      const album = await albumsApi.create(eventId, name);
      setAlbums((list) => [...list, album]);
      setSelected(album.id);
      setName('');
    } catch (err) {
      toast.error((err as Error).message);
    }
  };

  const remove = async (album: AlbumDto) => {
    if (!window.confirm(`Supprimer l'album « ${album.name} » et toutes ses photos ?`)) return;
    await albumsApi.remove(album.id);
    const rest = albums.filter((a) => a.id !== album.id);
    setAlbums(rest);
    setSelected(rest[0]?.id ?? null);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Albums et photos</CardTitle>
        <CardDescription>Les invités choisissent un album avant de déposer leurs photos.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <form onSubmit={create} className="flex gap-2">
          <Input required maxLength={80} placeholder="Ex. Cérémonie, Vin d'honneur, Soirée" value={name}
                 onChange={(e) => setName(e.target.value)} />
          <Button type="submit">Ajouter</Button>
        </form>

        <div className="flex flex-wrap gap-2">
          {albums.map((album) => (
            <Button key={album.id} size="sm" variant={album.id === selected ? 'default' : 'outline'}
                    onClick={() => setSelected(album.id)}>
              {album.name}
            </Button>
          ))}
        </div>

        {selected !== null && (
          <div className="space-y-3">
            <div className="flex justify-end">
              <Button size="sm" variant="ghost" className="text-destructive"
                      onClick={() => remove(albums.find((a) => a.id === selected)!).catch((e) => toast.error(e.message))}>
                Supprimer cet album
              </Button>
            </div>
            <ModerationGallery albumId={selected} />
          </div>
        )}
      </CardContent>
    </Card>
  );
}
