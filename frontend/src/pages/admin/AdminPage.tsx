import { useEffect, useState } from 'react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { EventStatusBadge } from '@/components/EventStatusBadge';
import { AdminEvent, adminApi } from '@/api/events';
import { ApiError } from '@/api/client';

const formatBytes = (bytes: number) =>
  bytes < 1024 * 1024 ? `${(bytes / 1024).toFixed(0)} Ko` : `${(bytes / 1024 / 1024).toFixed(1)} Mo`;

/** Super-admin : tous les événements de la plateforme avec leurs statistiques (S1), suspension (S2). */
export default function AdminPage() {
  const [events, setEvents] = useState<AdminEvent[] | null>(null);
  const [forbidden, setForbidden] = useState(false);

  const load = () =>
    adminApi.list()
      .then((page) => setEvents(page.content))
      .catch((e) => (e instanceof ApiError && e.status === 403 ? setForbidden(true) : toast.error(e.message)));

  useEffect(() => { load(); }, []);

  if (forbidden) return <p className="text-muted-foreground">Cette page est réservée au super-admin.</p>;
  if (!events) return <p className="text-muted-foreground">Chargement…</p>;

  const toggle = async (event: AdminEvent) => {
    if (event.status === 'SUSPENDED') await adminApi.reactivate(event.id);
    else await adminApi.suspend(event.id);
    await load();
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Super-admin</h1>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Événement</TableHead>
            <TableHead>Date</TableHead>
            <TableHead>Statut</TableHead>
            <TableHead className="text-right">Photos</TableHead>
            <TableHead className="text-right">Stockage</TableHead>
            <TableHead />
          </TableRow>
        </TableHeader>
        <TableBody>
          {events.map((event) => (
            <TableRow key={event.id}>
              <TableCell className="font-medium">{event.name}</TableCell>
              <TableCell>{new Date(event.date).toLocaleDateString('fr-FR')}</TableCell>
              <TableCell><EventStatusBadge status={event.status} /></TableCell>
              <TableCell className="text-right">{event.mediaCount}</TableCell>
              <TableCell className="text-right">{formatBytes(event.storageBytes)}</TableCell>
              <TableCell className="text-right">
                <Button size="sm" variant={event.status === 'SUSPENDED' ? 'outline' : 'destructive'}
                        onClick={() => toggle(event).catch((e) => toast.error(e.message))}>
                  {event.status === 'SUSPENDED' ? 'Réactiver' : 'Suspendre'}
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
