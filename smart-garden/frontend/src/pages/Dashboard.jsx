import { useEffect, useState } from 'react'
import axios from '../api/axios'
import { useAuth } from '../auth/AuthContext'
import PlantChart from '../components/PlantChart'
import { Link } from 'react-router-dom'

export default function Dashboard() {
  const { token } = useAuth()
  const [plants, setPlants] = useState([])
  const [form, setForm] = useState({
    name: '',
    type: '',
    imageUrl: ''
  })

  useEffect(() => {
    fetchPlants()
  }, [])

  const fetchPlants = async () => {
    try {
      const res = await axios.get('/plants', {
        headers: { Authorization: `Bearer ${token}` }
      })
      setPlants(res.data)
    } catch (err) {
      console.error('Грешка при зареждане на растенията:', err)
    }
  }

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await axios.post('/plants', form, {
        headers: { Authorization: `Bearer ${token}` }
      })
      setForm({ name: '', type: '', imageUrl: '' })
      fetchPlants()
    } catch (err) {
      alert('Неуспешно създаване на растение')
    }
  }

  const handleDelete = async (name) => {
    if (!confirm(`Сигурен ли си, че искаш да изтриеш "${name}"?`)) return
    try {
      await axios.delete(`/plants/${name}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      fetchPlants()
    } catch (err) {
      alert('Неуспешно изтриване')
    }
  }

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-8">
      <h1 className="text-3xl font-bold">Твоите растения 🌱</h1>

      {/* Форма */}
      <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <input name="name" placeholder="Име на растение" value={form.name} onChange={handleChange} className="border p-2" />
        <input name="type" placeholder="Тип растение" value={form.type} onChange={handleChange} className="border p-2" />
        <input name="imageUrl" placeholder="Снимка (URL)" value={form.imageUrl} onChange={handleChange} className="border p-2" />
        <button type="submit" className="bg-green-600 text-white px-4 py-2 rounded col-span-full md:col-span-1">Създай</button>
      </form>

      {/* Графика */}
      {plants.length > 0 && <PlantChart plants={plants} />}

      {/* Карти */}
<div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
  {plants.map(p => (
    <Link to={`/plants/${p.name}`} key={p.name}>
      <div className="border p-4 rounded shadow cursor-pointer hover:bg-gray-50 transition">
        <img src={p.imageUrl || 'https://example.com/default-plant.png'} alt={p.name} className="h-40 w-full object-cover mb-2 rounded" />
        <h2 className="text-xl font-bold">{p.name}</h2>
        <p className="text-sm text-gray-600">{p.type}</p>
      </div>
    </Link>
  ))}
</div>

    </div>
  )
}
