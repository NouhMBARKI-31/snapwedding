import { ChangeEvent, useEffect, useRef, useState } from 'react';
import { Link, Navigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { ApiError, guest, guestSessions } from '@/api/client';
import { AlbumDto, albumsApi } from '@/api/albums';
import { MAX_FILE_SIZE, MediaDto, mediaApi } from '@/api/media';
import { ZoomablePhoto } from '@/components/media/ProtectedImage';
import { GuestLayout } from './GuestLayout';

/** Invité : déposer des photos dans un album (I3), voir la galerie partagée (I4), supprimer les siennes (I5). */
export default function GuestAlbumPage() {
  const { code = '', albumId: albumParam } = useParams();
  const albumId = Number(albumParam);
  const session = guestSessions.get(code);
  const [album, setAlbum] = useState<AlbumDto | null>(null);
  const [media, setMedia] = useState<MediaDto[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [uploading, setUploading] = useState(false);
  const [expired, setExpired] = useState(false);
  const fileInput = useRef<HTMLInputElement>(null);

  const handleError = (e: unknown) => {
    if (e instanceof ApiError && e.status === 401) setExpired(true);
    else toast.error((e as Error).message);
  };

  const load = (p: number) =>
    mediaApi.guestList(code, albumId, p).then((res) => {
      setMedia((prev) => (p === 0 ? res.content : [...prev, ...res.content]));
      setPage(res.page.number);
      setTotalPages(res.page.totalPages);
    }).catch(handleError);

  useEffect(() => {
    if (!session) return;
    albumsApi.guestList(code).then((list) => setAlbum(list.find((a) => a.id === albumId) ?? null)).catch(handleError);
    load(0);
  }, [code, albumId]); // eslint-disable-line react-hooks/exhaustive-deps

  // Pas encore rejoint, ou jeton refusé (événement fermé...) : retour à la page de l'événement
  if (!session || expired) return <Navigate to={`/e/${code}`} replace />;

  const upload = async (e: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files ?? []);
    e.target.value = '';
    const tooBig = files.filter((f) => f.size > MAX_FILE_SIZE);
    if (tooBig.length > 0) {
      toast.error(`${tooBig.length} photo(s) dépassent 10 Mo`);
      return;
    }
    if (files.length === 0) return;

    setUploading(true);
    try {
      const added = await mediaApi.upload(code, albumId, files);
      setMedia((list) => [...added, ...list]);
      toast.success(`${added.length} photo(s) envoyée(s), merci !`);
    } catch (err) {
      handleError(err);
    } finally {
      setUploading(false);
    }
  };

  const remove = async (item: MediaDto) => {
    try {
      await mediaApi.guestRemove(code, item.id);
      setMedia((list) => list.filter((m) => m.id !== item.id));
    } catch (err) {
      handleError(err);
    }
  };

  return (
    <GuestLayout title={album?.name ?? 'Album'} subtitle={session.eventName}>
      <Link to={`/e/${code}`} className="text-sm text-muted-foreground">← Tous les albums</Link>

      {/* accept="image/*" : sur téléphone, propose la galerie et l'appareil photo */}
      <input ref={fileInput} type="file" accept="image/*" multiple hidden onChange={upload} />
      <Button size="lg" className="w-full h-14 text-lg" disabled={uploading} onClick={() => fileInput.current?.click()}>
        {uploading ? 'Envoi en cours…' : '📷 Ajouter mes photos'}
      </Button>

      {media.length === 0 && <p className="text-center text-muted-foreground">Soyez le premier à partager une photo !</p>}

      <div className="grid grid-cols-2 gap-2">
        {media.map((item) => (
          <div key={item.id} className="relative rounded-md overflow-hidden bg-background">
            <ZoomablePhoto mediaId={item.id} auth={guest(code)} alt={`Photo de ${item.guestPseudo}`}
                          className="w-full aspect-square object-cover" />
            <div className="absolute bottom-0 inset-x-0 flex items-center justify-between bg-black/50 px-2 py-1 text-xs text-white">
              <span className="truncate">{item.guestPseudo}</span>
              {item.mine && (
                <button className="underline" onClick={() => remove(item)}>Supprimer</button>
              )}
            </div>
          </div>
        ))}
      </div>

      {page + 1 < totalPages && (
        <Button variant="outline" className="w-full" onClick={() => load(page + 1)}>Voir plus</Button>
      )}
    </GuestLayout>
  );
}
