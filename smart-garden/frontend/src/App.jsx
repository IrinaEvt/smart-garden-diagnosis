import { Routes, Route, Navigate } from 'react-router-dom'
import LoginRegister from './pages/LoginRegister'
import Dashboard from './pages/Dashboard'
import PlantDetails from './pages/PlantDetails'
import PlantTypesPage from './pages/PlantTypesPage'
import SymptomsPage from './pages/SymptomsPage'
import PrivateRoute from './auth/PrivateRoute'

function App() {
  return (
    <Routes>
      {/* Публична страница (login/register) */}
      <Route path="/" element={<LoginRegister />} />

      {/* Приватни маршрути */}
      <Route path="/dashboard" element={
        <PrivateRoute>
          <Dashboard />
        </PrivateRoute>
      } />
      
      <Route path="/plants/:id" element={
        <PrivateRoute>
          <PlantDetails />
        </PrivateRoute>
      } />

      <Route path="/info/plants" element={
        <PrivateRoute>
          <PlantTypesPage />
        </PrivateRoute>
      } />

      <Route path="/info/symptoms" element={
        <PrivateRoute>
          <SymptomsPage />
        </PrivateRoute>
      } />

      {/* Пренасочване при невалиден път */}
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  )
}

export default App
