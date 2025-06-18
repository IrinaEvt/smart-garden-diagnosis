import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './auth/LoginPage'
import RegisterPage from './auth/RegisterPage'
import Dashboard from './pages/Dashboard'
import PlantDetails from './pages/PlantDetails' // 👈 добави това
import PrivateRoute from './auth/PrivateRoute'

function App() {
  return (
    <Routes>
      <Route path="/" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/dashboard" element={
        <PrivateRoute>
          <Dashboard />
        </PrivateRoute>
      } />

      {/* 👉 Добави това преди "*" */}
      <Route path="/plants/:name" element={
        <PrivateRoute>
          <PlantDetails />
        </PrivateRoute>
      } />

      {/* Redirect към dashboard ако пътят е невалиден */}
      <Route path="*" element={<Navigate to="/dashboard" />} />
    </Routes>
  )
}

export default App
