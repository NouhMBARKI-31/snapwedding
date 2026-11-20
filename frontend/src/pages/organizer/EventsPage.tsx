import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { EventStatusBadge } from '@/components/EventStatusBadge';
import { EventDto, eventsApi } from '@/api/events';
import { EventFormDialog } from './EventFormDialog';

/** Tableau de bord de l'organisateur : ses événements uniquement (O2, O8). */
export default function EventsPage() {
  const [events, setEvents] = useState<EventDto[] | null>(null);

  const load = () => eventsApi.list().then((page) => setEvents(page.content)).catch((e) => toast.error(e.message));
  useEffect(() => { load(); }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Mes événements</h1>
          <p className="text-muted-foreground">Créez un événement, imprimez son QR code, récupérez les photos.</p>
        </div>
        <EventFormDialog
          title="Nouvel événement"
          trigger={<Button>Créer un événement</Button>}
          onSubmit={async (form) => {
            await eventsApi.create(form);
            toast.success('Événement créé');
            await load();
          }}
        />
      </div>

      {events === null && <p className="text-muted-foreground">Chargement…</p>}
      {events?.length === 0 && <p className="text-muted-foreground">Pas encore d'événement.</p>}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {events?.map((event) => (
          <Link key={event.id} to={`/events/${event.id}`}>
            <Card className="hover:border-primary transition-colors h-full">
              <CardHeader>
                <div className="flex items-start justify-between gap-2">
                  <CardTitle className="text-lg">{event.name}</CardTitle>
                  <EventStatusBadge status={event.status} />
                </div>
                <CardDescription>
                  {new Date(event.date).toLocaleDateString('fr-FR')}{event.location ? ` · ${event.location}` : ''}
                </CardDescription>
              </CardHeader>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
