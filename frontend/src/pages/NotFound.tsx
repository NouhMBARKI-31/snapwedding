import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export default function NotFound() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-4 p-4 text-center">
      <h1 className="text-4xl font-bold">404</h1>
      <p className="text-muted-foreground">Cette page n'existe pas.</p>
      <Button asChild variant="outline"><Link to="/">Retour à l'accueil</Link></Button>
    </div>
  );
}
