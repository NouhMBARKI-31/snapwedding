import { Badge } from '@/components/ui/badge';
import { EventStatus, statusLabel } from '@/api/events';

export function EventStatusBadge({ status }: { status: EventStatus }) {
  const variant = status === 'OPEN' ? 'default' : status === 'CLOSED' ? 'secondary' : 'destructive';
  return <Badge variant={variant}>{statusLabel[status]}</Badge>;
}
