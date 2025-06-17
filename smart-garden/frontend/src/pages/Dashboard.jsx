// src/pages/Dashboard.jsx
import { useAuth } from '../auth/AuthContext'

export default function Dashboard() {
  const { userId, logout } = useAuth()

  return (
    <div className="p-4">
      <h1 className="text-2xl mb-4">Здравей, потребител #{userId}</h1>
      <button
        onClick={logout}
        className="bg-red-600 text-white px-4 py-2 rounded"
      >
        Изход
      </button>
    </div>
  )
}
