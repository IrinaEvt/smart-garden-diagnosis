import { Routes, Route, Navigate } from 'react-router-dom'
import LoginRegister from './pages/LoginRegister'
import Dashboard from './pages/Dashboard'
import PlantDetails from './pages/PlantDetails' // 👈 добави това
import PrivateRoute from './auth/PrivateRoute'

function App() {
  return (
    <Routes>
      {/* ✅ Set AuthForm като начален път */}
      <Route path="/" element={<LoginRegister />} />

      {/* 👇 Приватни маршрути */}
      <Route path="/dashboard" element={
        <PrivateRoute>
          <Dashboard />
        </PrivateRoute>
      } />
      <Route path="/plants/:name" element={
        <PrivateRoute>
          <PlantDetails />
        </PrivateRoute>
      } />

      {/* 👉 Ако е невалиден път */}
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  )
}

export default App
