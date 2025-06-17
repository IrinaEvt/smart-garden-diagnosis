import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from '../api/axios'
import { useAuth } from './AuthContext'

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' })
  const navigate = useNavigate()
  const { login } = useAuth()

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleLogin = async (e) => {
    e.preventDefault()
    try {
      const res = await axios.post('/auth/login', form)
      login(res.data.token, res.data.userId)
      navigate('/')
    } catch (err) {
      alert('Грешно потребителско име или парола')
    }
  }

return (
  <div className="p-4 max-w-sm mx-auto">
    <h1 className="text-2xl mb-4">Вход</h1>
    <form onSubmit={handleLogin} className="space-y-4">
      <input name="username" placeholder="Потребителско име" onChange={handleChange} className="w-full border p-2" />
      <input name="password" type="password" placeholder="Парола" onChange={handleChange} className="w-full border p-2" />
      <button type="submit" className="w-full bg-green-600 text-white p-2 rounded">Влез</button>
    </form>

    {/* Линк към регистрация */}
    <p className="text-sm mt-4 text-center">
      Нямаш акаунт? <a href="/register" className="text-blue-600 underline">Регистрирай се</a>
    </p>
  </div>
)
}
