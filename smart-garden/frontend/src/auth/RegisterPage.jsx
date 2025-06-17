// src/auth/RegisterPage.jsx
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from '../api/axios'

export default function RegisterPage() {
  const [form, setForm] = useState({ username: '', password: '' })
  const navigate = useNavigate()

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleRegister = async (e) => {
    e.preventDefault()
    try {
      await axios.post('/auth/register', form)
      alert('Успешна регистрация. Моля, влезте.')
      navigate('/login')
    } catch (err) {
      alert('Потребителското име вече съществува')
    }
  }

  return (
    <div className="p-4 max-w-sm mx-auto">
      <h1 className="text-2xl mb-4">Регистрация</h1>
      <form onSubmit={handleRegister} className="space-y-4">
        <input name="username" placeholder="Потребителско име" onChange={handleChange} className="w-full border p-2" />
        <input name="password" type="password" placeholder="Парола" onChange={handleChange} className="w-full border p-2" />
        <button type="submit" className="w-full bg-blue-600 text-white p-2 rounded">Регистрация</button>
      </form>
    </div>
  )
}
