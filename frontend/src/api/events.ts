import { api, blobUrl, json, ORGANIZER, PageResponse, PUBLIC } from './client';

/** Événements (organisateur) et espace super-admin. */

export type EventStatus = 'OPEN' | 'CLOSED' | 'SUSPENDED';

export interface EventDto {
  id: number;
  name: string;
  date: string; // "2027-06-12"
  location?: string;
  status: EventStatus;
  publicCode: string;
  guestUrl: string;
}

export interface EventForm {
  name: string;
  date: string;
  location?: string;
}

export interface PublicEvent {
  name: string;
  date: string;
  location?: string;
  status: EventStatus;
}

export interface AdminEvent {
  id: number;
  name: string;
  date: string;
  organizerId: string;
  status: EventStatus;
  mediaCount: number;
  storageBytes: number;
}

export const eventsApi = {
  list: (page = 0) => api<PageResponse<EventDto>>(`/api/events?page=${page}&size=20`, ORGANIZER),
  get: (id: number) => api<EventDto>(`/api/events/${id}`, ORGANIZER),
  create: (form: EventForm) => api<EventDto>('/api/events', ORGANIZER, { method: 'POST', ...json(form) }),
  update: (id: number, form: EventForm) => api<EventDto>(`/api/events/${id}`, ORGANIZER, { method: 'PUT', ...json(form) }),
  remove: (id: number) => api<void>(`/api/events/${id}`, ORGANIZER, { method: 'DELETE' }),
  close: (id: number) => api<EventDto>(`/api/events/${id}/close`, ORGANIZER, { method: 'POST' }),
  qrCodeUrl: (id: number) => blobUrl(`/api/events/${id}/qrcode`, ORGANIZER),
  publicInfo: (code: string) => api<PublicEvent>(`/api/public/events/${encodeURIComponent(code)}`, PUBLIC),
};

export const adminApi = {
  list: (page = 0) => api<PageResponse<AdminEvent>>(`/api/admin/events?page=${page}&size=20`, ORGANIZER),
  suspend: (id: number) => api<void>(`/api/admin/events/${id}/suspend`, ORGANIZER, { method: 'POST' }),
  reactivate: (id: number) => api<void>(`/api/admin/events/${id}/reactivate`, ORGANIZER, { method: 'POST' }),
};

export const statusLabel: Record<EventStatus, string> = {
  OPEN: 'Ouvert',
  CLOSED: 'Fermé',
  SUSPENDED: 'Suspendu',
};
