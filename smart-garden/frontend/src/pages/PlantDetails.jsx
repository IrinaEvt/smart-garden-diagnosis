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
  const [symptomOptions, setSymptomOptions] = useState({})
  const [tab, setTab] = useState('info')
  const [selectedGroup, setSelectedGroup] = useState('')
  const [selectedSymptom, setSelectedSymptom] = useState('')
  const [newSymptom, setNewSymptom] = useState('')

  useEffect(() => {
    fetchPlant()
    fetchSymptoms()
    fetchReasoning()
    fetchSymptomOptions()
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
      setReasoning(res.data)
    } catch (err) {
      console.error('Грешка при зареждане на reasoning:', err)
    }
  }

  const fetchSymptomOptions = async () => {
    try {
      const res = await axios.get('/reasoning/symptom-options', {
        headers: { Authorization: `Bearer ${token}` }
      })
      setSymptomOptions(res.data)
    } catch (err) {
      console.error('Грешка при зареждане на симптомите:', err)
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
    const symptomToAdd = selectedSymptom
    if (!symptomToAdd.trim()) return

    await axios.post(`/reasoning/${name}/symptoms`, { name: symptomToAdd }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    setSelectedGroup('')
    setSelectedSymptom('')
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

          {/* Селектор на категории и симптоми */}
          <form onSubmit={addSymptom} className="mt-4 space-y-2">
            <select
              value={selectedGroup}
              onChange={(e) => {
                setSelectedGroup(e.target.value)
                setSelectedSymptom('')
              }}
              className="border p-2 w-full"
            >
              <option value="">Избери категория</option>
              {Object.keys(symptomOptions).map(group => (
                <option key={group} value={group}>{group}</option>
              ))}
            </select>

            {selectedGroup && (
              <select
                value={selectedSymptom}
                onChange={(e) => setSelectedSymptom(e.target.value)}
                className="border p-2 w-full"
              >
                <option value="">Избери симптом</option>
                {symptomOptions[selectedGroup].map(symptom => (
                  <option key={symptom} value={symptom}>{symptom}</option>
                ))}
              </select>
            )}

            <button
              type="submit"
              className="bg-blue-600 text-white px-4 py-2 rounded"
              disabled={!selectedSymptom}
            >
              Добави
            </button>
          </form>
        </div>
      )}

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
    </div>
  )
}
