import { BrowserRouter, Navigate, Route, Routes, useParams } from 'react-router-dom'
import {
  CategoriesPage,
  ClipDetailPage,
  InboxPage,
  PastePage,
  PrivacyPage,
  SendersPage,
} from './pages'
import './styles.css'

function ClipRoute() {
  const { clipId } = useParams()
  if (!clipId) return <Navigate to="/" replace />
  return <ClipDetailPage clipId={clipId} />
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<InboxPage />} />
        <Route path="/paste" element={<PastePage />} />
        <Route path="/clip/:clipId" element={<ClipRoute />} />
        <Route path="/senders" element={<SendersPage />} />
        <Route path="/categories" element={<CategoriesPage />} />
        <Route path="/privacy" element={<PrivacyPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
