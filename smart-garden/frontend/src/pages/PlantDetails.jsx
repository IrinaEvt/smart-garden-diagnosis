import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import axios from '../api/axios'
import { useAuth } from '../auth/AuthContext'

export default function PlantDetails() {
  const { name } = useParams()
  const navigate = useNavigate()
  const { token } = useAuth()

  const [plant, setPlant] = useState(null)
  const [symptoms, setSymptoms] = useState([])
  const [reasoning, setReasoning] = useState([])
  const [tab, setTab] = useState('info')
  const [newSymptom, setNewSymptom] = useState('')

  useEffect(() => {
    fetchPlant()
    fetchSymptoms()
    fetchReasoning()
  }, [])

  const fetchPlant = async () => {
    const res = await axios.get(`/plants/${name}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    setPlant(res.data)
  }

  const fetchSymptoms = async () => {
    const res = await axios.get(`/reasoning/${name}/symptoms`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    setSymptoms(res.data)
  }

  const fetchReasoning = async () => {
   try {
    const res = await axios.get(`/reasoning/${name}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    console.log('REASONING:', res.data)
    setReasoning(res.data) 
  } catch (err) {
    console.error('Грешка при зареждане на reasoning:', err)
  }
  }

  const deleteSymptom = async (id) => {
    await axios.delete(`/reasoning/${name}/symptoms/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    fetchSymptoms()
  }

  const addSymptom = async (e) => {
    e.preventDefault()
    if (!newSymptom.trim()) return
    await axios.post(`/reasoning/${name}/symptoms`, { name: newSymptom }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    setNewSymptom('')
    await fetchSymptoms()
    await fetchReasoning()
  }

  const handleDeletePlant = async () => {
    if (!confirm(`Сигурен ли си, че искаш да изтриеш "${name}"?`)) return
    await axios.delete(`/plants/${name}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    navigate('/dashboard')
  }

  if (!plant) return <p>Зареждане...</p>

  return (
    <div className="p-6 max-w-3xl mx-auto space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">{plant.name}</h1>
        <button onClick={handleDeletePlant} className="text-red-600 underline text-sm">Изтрий растение</button>
      </div>

      <div className="flex gap-4">
        <button onClick={() => setTab('info')} className={tab === 'info' ? 'font-bold underline' : ''}>Информация</button>
        <button onClick={() => setTab('reasoning')} className={tab === 'reasoning' ? 'font-bold underline' : ''}>Съвети</button>
      </div>

      {tab === 'info' && (
        <div className="space-y-2">
          <img src={plant.imageUrl || 'https://example.com/default-plant.png'} className="h-48 w-full object-cover rounded" />
          <p><strong>Тип:</strong> {plant.type}</p>
          <p><strong>Температура:</strong> {plant.temperature}</p>
          <p><strong>Светлина:</strong> {plant.light}</p>
          <p><strong>Влажност:</strong> {plant.humidity}</p>
          <p><strong>Почвена влажност:</strong> {plant.soilMoisture}</p>

          {symptoms.length > 0 && (
            <div>
              <h3 className="font-semibold mt-4">История на симптомите</h3>
              <ul className="list-disc list-inside">
                {symptoms.map(s => (
                  <li key={s.id} className="flex justify-between">
                    <span>{s.name}</span>
                    <button onClick={() => deleteSymptom(s.id)} className="text-red-500 text-sm ml-2">Изтрий</button>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <form onSubmit={addSymptom} className="mt-4 flex gap-2">
            <input
              value={newSymptom}
              onChange={e => setNewSymptom(e.target.value)}
              placeholder="Нов симптом"
              className="border p-2 flex-1"
            />
            <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded">
              Добави
            </button>
          </form>
        </div>
      )}

      {tab === 'reasoning' && (
        <div className="space-y-2">
          {reasoning.length === 0 ? (
            <p>Няма съвети към момента.</p>
          ) : (
            <ul className="list-disc list-inside">
             {tab === 'reasoning' && (
  <div className="space-y-4">
    <div>
      <h3 className="font-semibold">Симптоми</h3>
      <ul className="list-disc list-inside">
        {reasoning.symptoms?.map((s, i) => <li key={i}>{s}</li>)}
      </ul>
    </div>

    <div>
      <h3 className="font-semibold">Причини</h3>
      <ul className="list-disc list-inside">
        {reasoning.causes?.map((c, i) => <li key={i}>{c}</li>)}
      </ul>
    </div>

    <div>
      <h3 className="font-semibold">Препоръки</h3>
      <ul className="list-disc list-inside">
        {reasoning.careActions?.map((a, i) => <li key={i}>{a}</li>)}
      </ul>
    </div>
  </div>
)}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}
