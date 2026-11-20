import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { Toaster } from '@/components/ui/sonner';
import { TooltipProvider } from '@/components/ui/tooltip';
import { AuthProvider } from '@/contexts/AuthContext';
import { AppLayout } from '@/components/AppLayout';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import Login from '@/pages/Login';
import NotFound from '@/pages/NotFound';
// Organisateur et super-admin
import EventsPage from '@/pages/organizer/EventsPage';
import EventDetailPage from '@/pages/organizer/EventDetailPage';
import AdminPage from '@/pages/admin/AdminPage';
// Invité
import GuestEventPage from '@/pages/guest/GuestEventPage';
import DemoPage from '@/pages/guest/DemoPage';
import GuestAlbumPage from '@/pages/guest/GuestAlbumPage';

const App = () => (
  <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID ?? ''}>
    <AuthProvider>
      <TooltipProvider>
        <Toaster richColors position="top-center" />
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />

            {/* Organisateur et super-admin (JWT Google) */}
            <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
              <Route path="/" element={<Navigate to="/events" replace />} />
              <Route path="/events" element={<EventsPage />} />
              <Route path="/events/:eventId" element={<EventDetailPage />} />
              <Route path="/admin" element={<AdminPage />} />
            </Route>
            <Route path="/demo/:eventId" element={<ProtectedRoute><DemoPage /></ProtectedRoute>} />

            {/* Invités (sans compte, jeton d'invité) : l'URL du QR code est /e/<code public> */}
            <Route path="/e/:code" element={<GuestEventPage />} />
            <Route path="/e/:code/albums/:albumId" element={<GuestAlbumPage />} />

            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </AuthProvider>
  </GoogleOAuthProvider>
);

export default App;
