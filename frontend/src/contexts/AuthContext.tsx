import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { organizerSession } from '@/api/client';

/** Organisateur connecté avec Google. (Les invités n'ont pas de compte : voir api/client.ts.) */
export interface User {
  id: string;
  email: string;
  name: string;
  picture?: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  loginWithGoogle: (credential: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function decodeGoogleCredential(credential: string) {
  const base64 = credential.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
  return JSON.parse(atob(padded)) as { sub: string; email: string; name?: string; picture?: string };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  // Lecture immédiate du localStorage : pas de redirection vers /login au rechargement de la page
  const [user, setUser] = useState<User | null>(() => (organizerSession.token() ? organizerSession.user() : null));

  useEffect(() => {
    const onLogout = () => setUser(null);
    window.addEventListener('snapwedding:logout', onLogout);
    return () => window.removeEventListener('snapwedding:logout', onLogout);
  }, []);

  const loginWithGoogle = (credential: string) => {
    const payload = decodeGoogleCredential(credential);
    const googleUser: User = {
      id: payload.sub,
      email: payload.email,
      name: payload.name ?? payload.email,
      picture: payload.picture,
    };
    organizerSession.save(credential, googleUser);
    setUser(googleUser);
  };

  const logout = () => {
    organizerSession.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, loginWithGoogle, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
