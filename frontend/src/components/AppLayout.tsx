import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { NavLink } from '@/components/NavLink';

/** Mise en page de l'espace organisateur et admin. (Les pages invité ont leur propre mise en page mobile.) */
export function AppLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b bg-background sticky top-0 z-10">
        <div className="container mx-auto px-4 py-3 flex items-center justify-between gap-4">
          <div className="flex items-center gap-6">
            <Link to="/events" className="text-xl font-bold">SnapWedding</Link>
            <nav className="flex gap-4">
              <NavLink to="/events" className="text-sm text-muted-foreground hover:text-foreground"
                       activeClassName="text-foreground font-medium">Mes événements</NavLink>
              <NavLink to="/admin" className="text-sm text-muted-foreground hover:text-foreground"
                       activeClassName="text-foreground font-medium">Admin</NavLink>
            </nav>
          </div>
          <div className="flex items-center gap-3">
            <Avatar className="h-8 w-8 hidden sm:flex">
              <AvatarImage src={user?.picture} alt={user?.name} />
              <AvatarFallback>{user?.name.substring(0, 2).toUpperCase()}</AvatarFallback>
            </Avatar>
            <Button variant="outline" size="sm" onClick={logout}>Déconnexion</Button>
          </div>
        </div>
      </header>
      <main className="flex-1 container mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
