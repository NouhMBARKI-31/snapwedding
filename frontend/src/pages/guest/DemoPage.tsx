import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import { EventDto, eventsApi } from '@/api/events';

/**
 * Page de démo pour la soutenance (autorisée par l'énoncé) : le QR code et, à côté,
 * la zone invité vers laquelle il redirige, dans un cadre de la taille d'un téléphone.
 *
 * Elle ne contourne pas la sécurité : le cadre charge la vraie page /e/<code>,
 * qui passe par le même parcours qu'un invité (code, pseudo, jeton).
 */
export default function DemoPage() {
  const eventId = Number(useParams().eventId);
  const [event, setEvent] = useState<EventDto | null>(null);
  const [qrUrl, setQrUrl] = useState<string | null>(null);

  useEffect(() => {
    eventsApi.get(eventId).then(setEvent).catch((e) => toast.error(e.message));
    eventsApi.qrCodeUrl(eventId).then(setQrUrl).catch(() => setQrUrl(null));
  }, [eventId]);

  if (!event) return <p className="p-6 text-muted-foreground">Chargement…</p>;

  const guestPath = `/e/${event.publicCode}`;

  return (
    <div className="min-h-screen bg-muted/40 p-6">
      <Link to={`/events/${event.id}`} className="text-sm text-muted-foreground hover:underline">← Retour</Link>
      <div className="mt-4 flex flex-col lg:flex-row items-center justify-center gap-10">
        <div className="text-center space-y-3">
          <h1 className="text-2xl font-bold">{event.name}</h1>
          {qrUrl && <img src={qrUrl} alt="QR code" className="w-72 h-72 bg-white p-3 rounded-lg border" />}
          <p className="text-sm text-muted-foreground break-all max-w-xs">{event.guestUrl}</p>
        </div>
        <div className="text-center space-y-2">
          <p className="text-sm text-muted-foreground">Ce que voit l'invité après le scan</p>
          <iframe title="Zone invité" src={guestPath}
                  className="w-[390px] h-[780px] rounded-[2rem] border-8 border-foreground bg-background" />
        </div>
      </div>
    </div>
  );
}
