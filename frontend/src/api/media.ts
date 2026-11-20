import { api, Auth, blobUrl, guest, json, ORGANIZER, PageResponse } from './client';

/** Membre C : photos. */

export interface MediaDto {
  id: number;
  albumId: number;
  guestPseudo: string;
  originalFilename: string;
  contentType: string;
  size: number;
  hidden: boolean;
  mine: boolean;
  createdAt: string;
}

export const MAX_FILE_SIZE = 10 * 1024 * 1024; // identique à spring.servlet.multipart.max-file-size

export const mediaApi = {
  // Organisateur
  list: (albumId: number, page = 0) =>
    api<PageResponse<MediaDto>>(`/api/albums/${albumId}/media?page=${page}&size=50`, ORGANIZER),
  setHidden: (id: number, hidden: boolean) =>
    api<MediaDto>(`/api/media/${id}`, ORGANIZER, { method: 'PATCH', ...json({ hidden }) }),
  remove: (id: number) => api<void>(`/api/media/${id}`, ORGANIZER, { method: 'DELETE' }),

  // Invité
  guestList: (code: string, albumId: number, page = 0) =>
    api<PageResponse<MediaDto>>(`/api/guest/albums/${albumId}/media?page=${page}&size=50`, guest(code)),
  upload: (code: string, albumId: number, files: File[]) => {
    const form = new FormData();
    files.forEach((file) => form.append('files', file));
    return api<MediaDto[]>(`/api/guest/albums/${albumId}/media`, guest(code), { method: 'POST', body: form });
  },
  guestRemove: (code: string, id: number) => api<void>(`/api/guest/media/${id}`, guest(code), { method: 'DELETE' }),

  /** URL locale de l'image : les photos sont protégées, un simple <img src> n'enverrait pas le jeton. */
  imageUrl: (id: number, auth: Auth) =>
    blobUrl(auth.kind === 'guest' ? `/api/guest/media/${id}/content` : `/api/media/${id}/content`, auth),
};
