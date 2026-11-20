import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription,
  AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { EventStatusBadge } from '@/components/EventStatusBadge';
import { AlbumsManager } from '@/components/albums/AlbumsManager';
import { EventDto, eventsApi } from '@/api/events';
import { EventFormDialog } from './EventFormDialog';

/** Page d'un événement : infos, QR code (O4), fermeture (O7), suppression, albums et modération (C). */
export default function EventDetailPage() {
  const eventId = Number(useParams().eventId);
  const navigate = useNavigate();
  const [event, setEvent] = useState<EventDto | null>(null);
  const [qrUrl, setQrUrl] = useState<string | null>(null);

  useEffect(() => {
    eventsApi.get(eventId).then(setEvent).catch((e) => toast.error(e.message));
    let url: string | null = null;
    eventsApi.qrCodeUrl(eventId).then((u) => { url = u; setQrUrl(u); }).catch(() => setQrUrl(null));
    return () => { if (url) URL.revokeObjectURL(url); };
  }, [eventId]);

  if (!event) return <p className="text-muted-foreground">Chargement…</p>;

  const close = async () => {
    setEvent(await eventsApi.close(event.id));
    toast.success('Événement fermé : plus aucun dépôt possible');
  };

  const remove = async () => {
    await eventsApi.remove(event.id);
    toast.success('Événement supprimé');
    navigate('/events');
  };

  return (
    <div className="space-y-6">
      <Link to="/events" className="text-sm text-muted-foreground hover:underline">← Mes événements</Link>

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-bold">{event.name}</h1>
            <EventStatusBadge status={event.status} />
          </div>
          <p className="text-muted-foreground">
            {new Date(event.date).toLocaleDateString('fr-FR')}{event.location ? ` · ${event.location}` : ''}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <EventFormDialog
            title="Modifier l'événement"
            initial={{ name: event.name, date: event.date, location: event.location ?? '' }}
            trigger={<Button variant="outline">Modifier</Button>}
            onSubmit={async (form) => setEvent(await eventsApi.update(event.id, form))}
          />
          <Button variant="outline" asChild><Link to={`/demo/${event.id}`}>Page de démo</Link></Button>
          {event.status === 'OPEN' && (
            <Confirm title="Fermer l'événement ?" description="Les invités ne pourront plus déposer de photos. L'album reste consultable."
                     label="Fermer" onConfirm={close} />
          )}
          <Confirm title="Supprimer l'événement ?" description="Les albums, les photos et les invités seront supprimés définitivement."
                   label="Supprimer" destructive onConfirm={remove} />
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>QR code à imprimer</CardTitle>
          <CardDescription>Posez-le sur les tables : les invités le scannent pour déposer leurs photos.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col sm:flex-row items-center gap-6">
          {qrUrl && <img src={qrUrl} alt="QR code de l'événement" className="w-48 h-48 border rounded-md" />}
          <div className="space-y-3 text-sm break-all">
            <p><span className="text-muted-foreground">Lien invité : </span>{event.guestUrl}</p>
            {qrUrl && (
              <Button asChild variant="outline">
                <a href={qrUrl} download={`qrcode-${event.name}.png`}>Télécharger le PNG</a>
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      <AlbumsManager eventId={event.id} />
    </div>
  );
}

function Confirm({ title, description, label, destructive, onConfirm }: {
  title: string; description: string; label: string; destructive?: boolean; onConfirm: () => Promise<void>;
}) {
  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button variant={destructive ? 'destructive' : 'secondary'}>{label}</Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription>{description}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Annuler</AlertDialogCancel>
          <AlertDialogAction onClick={() => onConfirm().catch((e) => toast.error(e.message))}>{label}</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
