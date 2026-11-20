import { api, guest, GuestSession, guestSessions, json, PUBLIC } from './client';

/** Invités : rejoindre un événement et session invité. */

export const guestApi = {
  /** I2 : pseudo -> jeton d'invité, gardé dans le navigateur pour cet événement. */
  join: async (code: string, pseudo: string): Promise<GuestSession> => {
    const session = await api<GuestSession>(`/api/public/events/${encodeURIComponent(code)}/join`, PUBLIC, {
      method: 'POST',
      ...json({ pseudo }),
    });
    guestSessions.save(code, session);
    return session;
  },

  /** Vérifie que le jeton gardé est toujours valable (événement encore ouvert). */
  me: (code: string) => api<Omit<GuestSession, 'token'>>('/api/guest/me', guest(code)),

  leave: (code: string) => guestSessions.clear(code),
};
