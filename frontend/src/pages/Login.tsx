import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { CredentialResponse, GoogleLogin } from '@react-oauth/google';
import { useAuth } from '@/contexts/AuthContext';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

/** Connexion des organisateurs (et du super-admin) avec Google (O1). */
export default function Login() {
  const { loginWithGoogle, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const targetPath = (location.state as { from?: string })?.from ?? '/events';

  useEffect(() => {
    if (isAuthenticated) navigate(targetPath, { replace: true });
  }, [isAuthenticated, navigate, targetPath]);

  const handleSuccess = (response: CredentialResponse) => {
    if (!response.credential) {
      setError('Réponse Google invalide.');
      return;
    }
    loginWithGoogle(response.credential);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/40 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-2">
          <CardTitle className="text-3xl font-bold">SnapWedding</CardTitle>
          <CardDescription>Espace organisateur : connectez-vous avec Google.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {error && <p className="text-sm text-destructive text-center">{error}</p>}
          <div className="flex justify-center">
            <GoogleLogin onSuccess={handleSuccess} onError={() => setError('Connexion Google impossible.')} />
          </div>
          <p className="text-xs text-muted-foreground text-center">
            Vous êtes invité ? Scannez le QR code posé sur votre table.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
