import { FormEvent, ReactNode, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { EventForm } from '@/api/events';

interface Props {
  title: string;
  trigger: ReactNode;
  initial?: EventForm;
  onSubmit: (form: EventForm) => Promise<void>;
}

/** Formulaire de création / modification d'un événement (O2). */
export function EventFormDialog({ title, trigger, initial, onSubmit }: Props) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<EventForm>(initial ?? { name: '', date: '', location: '' });
  const [saving, setSaving] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await onSubmit(form);
      setOpen(false);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <form onSubmit={submit} className="space-y-4">
          <DialogHeader><DialogTitle>{title}</DialogTitle></DialogHeader>
          <div className="space-y-2">
            <Label htmlFor="name">Nom</Label>
            <Input id="name" required maxLength={120} value={form.name}
                   onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="date">Date</Label>
            <Input id="date" type="date" required value={form.date}
                   onChange={(e) => setForm({ ...form, date: e.target.value })} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="location">Lieu</Label>
            <Input id="location" maxLength={200} value={form.location ?? ''}
                   onChange={(e) => setForm({ ...form, location: e.target.value })} />
          </div>
          <DialogFooter>
            <Button type="submit" disabled={saving}>{saving ? 'Enregistrement…' : 'Enregistrer'}</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
