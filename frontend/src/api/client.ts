/**
 * Client HTTP commun : ajoute la bonne authentification à chaque appel.
 *  - "organizer" : le token Google (header Authorization: Bearer ...)
 *  - "guest"     : le jeton d'invité de l'événement (header X-Guest-Token)
 *  - "none"      : routes publiques (/api/public/**)
 */
export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8080';

const ORGANIZER_TOKEN_KEY = 'snapwedding_token';
const ORGANIZER_USER_KEY = 'snapwedding_user';

/** Format des pages Spring Data (VIA_DTO). */
export interface PageResponse<T> {
  content: T[];
  page: { size: number; number: number; totalElements: number; totalPages: number };
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

// ---------- Session organisateur ----------

export const organizerSession = {
  token: () => localStorage.getItem(ORGANIZER_TOKEN_KEY),
  user: () => {
    const raw = localStorage.getItem(ORGANIZER_USER_KEY);
    return raw ? JSON.parse(raw) : null;
  },
  save: (token: string, user: unknown) => {
    localStorage.setItem(ORGANIZER_TOKEN_KEY, token);
    localStorage.setItem(ORGANIZER_USER_KEY, JSON.stringify(user));
  },
  clear: () => {
    localStorage.removeItem(ORGANIZER_TOKEN_KEY);
    localStorage.removeItem(ORGANIZER_USER_KEY);
  },
};

// ---------- Session invité (une par événement, rangée sous le code public) ----------

export interface GuestSession {
  token: string;
  guestId: number;
  pseudo: string;
  eventId: number;
  eventName: string;
}

const guestKey = (code: string) => `snapwedding_guest_${code}`;

export const guestSessions = {
  get: (code: string): GuestSession | null => {
    const raw = localStorage.getItem(guestKey(code));
    return raw ? (JSON.parse(raw) as GuestSession) : null;
  },
  save: (code: string, session: GuestSession) => localStorage.setItem(guestKey(code), JSON.stringify(session)),
  clear: (code: string) => localStorage.removeItem(guestKey(code)),
};

// ---------- Requêtes ----------

export type Auth = { kind: 'organizer' } | { kind: 'guest'; code: string } | { kind: 'none' };

export const ORGANIZER: Auth = { kind: 'organizer' };
export const PUBLIC: Auth = { kind: 'none' };
export const guest = (code: string): Auth => ({ kind: 'guest', code });

function authHeaders(auth: Auth): Record<string, string> {
  if (auth.kind === 'organizer') {
    const token = organizerSession.token();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }
  if (auth.kind === 'guest') {
    const session = guestSessions.get(auth.code);
    return session ? { 'X-Guest-Token': session.token } : {};
  }
  return {};
}

async function send(path: string, auth: Auth, init: RequestInit = {}): Promise<Response> {
  const isForm = init.body instanceof FormData;
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      ...(isForm || !init.body ? {} : { 'Content-Type': 'application/json' }),
      ...authHeaders(auth),
      ...(init.headers as Record<string, string> | undefined),
    },
  });

  if (response.status === 401) {
    if (auth.kind === 'organizer') {
      organizerSession.clear();
      window.dispatchEvent(new Event('snapwedding:logout'));
    } else if (auth.kind === 'guest') {
      guestSessions.clear(auth.code); // jeton invalide ou événement fermé : il faudra rejoindre à nouveau
    }
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new ApiError(response.status, body?.error ?? `Erreur ${response.status}`);
  }
  return response;
}

/** Appel JSON. Renvoie undefined pour un 204. */
export async function api<T>(path: string, auth: Auth, init: RequestInit = {}): Promise<T> {
  const response = await send(path, auth, init);
  return (response.status === 204 ? undefined : await response.json()) as T;
}

/** Télécharge une image protégée (photo, QR code) et renvoie une URL locale utilisable dans <img>. */
export async function blobUrl(path: string, auth: Auth): Promise<string> {
  const response = await send(path, auth);
  return URL.createObjectURL(await response.blob());
}

export const json = (body: unknown): RequestInit => ({ body: JSON.stringify(body) });
