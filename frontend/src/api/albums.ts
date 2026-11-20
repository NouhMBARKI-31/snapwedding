import { api, guest, json, ORGANIZER } from './client';

/** Membre C : albums. */

export interface AlbumDto {
  id: number;
  eventId: number;
  name: string;
}

export const albumsApi = {
  list: (eventId: number) => api<AlbumDto[]>(`/api/events/${eventId}/albums`, ORGANIZER),
  create: (eventId: number, name: string) =>
    api<AlbumDto>(`/api/events/${eventId}/albums`, ORGANIZER, { method: 'POST', ...json({ name }) }),
  rename: (id: number, name: string) => api<AlbumDto>(`/api/albums/${id}`, ORGANIZER, { method: 'PUT', ...json({ name }) }),
  remove: (id: number) => api<void>(`/api/albums/${id}`, ORGANIZER, { method: 'DELETE' }),
  /** Invité : les albums de SON événement (déduit du jeton). */
  guestList: (code: string) => api<AlbumDto[]>('/api/guest/albums', guest(code)),
};
