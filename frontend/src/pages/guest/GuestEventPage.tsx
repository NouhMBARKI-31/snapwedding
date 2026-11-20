import { FormEvent, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { ApiError, GuestSession, guestSessions } from '@/api/client';
import { eventsApi, PublicEvent, statusLabel } from '@/api/events';
import { guestApi } from '@/api/guest';
import { AlbumDto, albumsApi } from '@/api/albums';
import { GuestLayout } from './GuestLayout';

/**
 * Page ouverte par le QR code : /e/<code public> (I1).
 * Sans session : formulaire "pseudo" pour rejoindre (I2). Avec session : la liste des albums.
 */
export default function GuestEventPage() {
  const code = useParams().code!;
  const [event, setEvent] = useState<PublicEvent | null>(null);
  const [notFound, setNotFound] = useState(false);
  const [session, setSession] = useState<GuestSession | null>(() => guestSessions.get(code));
  const [albums, setAlbums] = useState<AlbumDto[]>([]);

  useEffect(() => {
    eventsApi.publicInfo(code).then(setEvent).catch(() => setNotFound(true));
  }, [code]);

  useEffect(() => {
    if (!session) return;
    albumsApi.guestList(code)
      .then(setAlbums)
      .catch((e) => {
        if (e instanceof ApiError && e.status === 401) setSession(null); // jeton refusé : il faut rejoindre à nouveau
        else toast.error(e.message);
      });
  }, [code, session]);

  if (notFound) {
    return <GuestLayout title="Événement introuvable"><p className="text-center">Vérifiez le QR code.</p></GuestLayout>;
  }
  if (!event) return <GuestLayout title="Chargement…"><span /></GuestLayout>;

  const subtitle = new Date(event.date).toLocaleDateString('fr-FR') + (event.location ? ` · ${event.location}` : '');

  if (event.status !== 'OPEN') {
    return (
      <GuestLayout title={event.name} subtitle={subtitle}>
        <p className="text-center text-muted-foreground">Cet événement est {statusLabel[event.status].toLowerCase()} : les dépôts sont terminés.</p>
      </GuestLayout>
    );
  }

  if (!session) {
    return (
      <GuestLayout title={event.name} subtitle={subtitle}>
        <JoinForm code={code} onJoined={setSession} />
      </GuestLayout>
    );
  }

  return (
    <GuestLayout title={event.name} subtitle={`Bonjour ${session.pseudo} !`}>
      <p className="text-center text-sm text-muted-foreground">Choisissez un album pour y déposer vos photos.</p>
      {albums.length === 0 && <p className="text-center text-muted-foreground">Aucun album pour le moment.</p>}
      <div className="grid gap-3">
        {albums.map((album) => (
          <Button key={album.id} asChild size="lg" variant="outline" className="h-16 text-lg justify-start">
            <Link to={`/e/${code}/albums/${album.id}`}>📷 {album.name}</Link>
          </Button>
        ))}
      </div>
      <Button variant="ghost" className="w-full" onClick={() => { guestApi.leave(code); setSession(null); }}>
        Changer de pseudo
      </Button>
    </GuestLayout>
  );
}

function JoinForm({ code, onJoined }: { code: string; onJoined: (s: GuestSession) => void }) {
  const [pseudo, setPseudo] = useState('');
  const [sending, setSending] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setSending(true);
    try {
      onJoined(await guestApi.join(code, pseudo));
    } catch (err) {
      toast.error((err as Error).message);
    } finally {
      setSending(false);
    }
  };

  return (
    <Card>
      <CardHeader><CardTitle className="text-lg">Partagez vos photos</CardTitle></CardHeader>
      <CardContent>
        <form onSubmit={submit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="pseudo">Votre prénom ou pseudo</Label>
            <Input id="pseudo" required maxLength={40} autoComplete="nickname" value={pseudo}
                   onChange={(e) => setPseudo(e.target.value)} placeholder="Ex. Exemple Nom" />
          </div>
          <Button type="submit" size="lg" className="w-full" disabled={sending}>
            {sending ? 'Connexion…' : 'Rejoindre'}
          </Button>
          <p className="text-xs text-center text-muted-foreground">Pas de compte à créer.</p>
        </form>
      </CardContent>
    </Card>
  );
}
