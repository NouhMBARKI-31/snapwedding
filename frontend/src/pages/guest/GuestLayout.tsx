import { ReactNode } from 'react';

/** Mise en page invité : pensée d'abord pour un téléphone (mobile first), centrée et étroite. */
export function GuestLayout({ title, subtitle, children }: { title: string; subtitle?: string; children: ReactNode }) {
  return (
    <div className="min-h-screen bg-muted/40">
      <header className="bg-background border-b px-4 py-4 text-center">
        <p className="text-xs uppercase tracking-widest text-muted-foreground">SnapWedding</p>
        <h1 className="text-xl font-bold">{title}</h1>
        {subtitle && <p className="text-sm text-muted-foreground">{subtitle}</p>}
      </header>
      <main className="mx-auto w-full max-w-md px-4 py-6 space-y-4">{children}</main>
    </div>
  );
}
