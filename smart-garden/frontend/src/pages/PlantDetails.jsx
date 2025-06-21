import { useEffect, useState, Fragment } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import axios from '../api/axios'
import { useAuth } from '../auth/AuthContext'
import { Listbox, Transition } from '@headlessui/react'
import { ChevronUpDownIcon, CheckIcon } from '@heroicons/react/20/solid'
import SensorChart from '../components/SensorChart'
import SidebarNavigation from '../components/SidebarNavigation'

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
  const [selectedSymptom, setSelectedSymptom] = useState(null)
  const [sensorHistory, setSensorHistory] = useState([])
  const [sensorAlerts, setSensorAlerts] = useState([])

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

  const fetchSensorHistory = async () => {
    const res = await axios.get(`/plants/${name}/sensors/history`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const parsed = res.data.readings.map(r => ({
      ...r,
      readingValue: parseFloat(r.readingValue)
    }))
    setSensorHistory(parsed)
    setSensorAlerts(res.data.alerts)
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
    if (!selectedSymptom?.trim()) return

    await axios.post(`/reasoning/${name}/symptoms`, { name: selectedSymptom }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    setSelectedGroup('')
    setSelectedSymptom(null)
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

  if (!plant) return <p className="text-white p-4">Зареждане...</p>

  return (
    <div className="flex bg-[#0f1e13] text-white min-h-screen">
      <SidebarNavigation />
      <main className="flex-1 py-8 px-4 space-y-8">
        <div className="flex justify-between items-center border-b border-green-600 pb-4">
          <h1 className="text-4xl font-bold">🌿 {plant.name}</h1>
          <button onClick={handleDeletePlant} className="text-red-500 hover:text-red-700 text-sm">Изтрий растение</button>
        </div>

        <div className="flex gap-4">
          <button onClick={() => setTab('info')} className={tab === 'info' ? 'underline font-semibold' : 'text-gray-400'}>Информация</button>
          <button onClick={() => setTab('reasoning')} className={tab === 'reasoning' ? 'underline font-semibold' : 'text-gray-400'}>Съвети</button>
          <button onClick={() => { setTab('sensors'); fetchSensorHistory() }} className={tab === 'sensors' ? 'underline font-semibold' : 'text-gray-400'}>Сензори</button>
        </div>

        {tab === 'info' && (
          <div className="flex flex-col md:flex-row gap-6 items-start">
            <div className="w-full md:w-64 shrink-0">
              <img
                src={plant.imageUrl || '/images/default.jpg'}
                alt={plant.name}
                className="object-cover rounded-xl w-full h-48 md:h-64"
              />
            </div>

            <div className="flex-1 space-y-6">
              <div className="border-b border-green-800 pb-4">
                <h3 className="text-lg font-semibold mb-2">Основна информация</h3>
                <p><strong>Тип:</strong> {plant.type}</p>
              </div>

              <div className="border-b border-green-800 pb-4">
                <h3 className="text-lg font-semibold mb-2">Условия за отглеждане</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <p><strong>Температура:</strong> {plant.temperature}</p>
                  <p><strong>Светлина:</strong> {plant.light}</p>
                  <p><strong>Влажност:</strong> {plant.humidity}</p>
                  <p><strong>Почвена влажност:</strong> {plant.soilMoisture}</p>
                </div>
              </div>

              {symptoms.length > 0 && (
                <div className="border-b border-green-800 pb-4">
                  <h3 className="text-lg font-semibold mb-2">📋 История на симптомите</h3>
                  <ul className="list-disc list-inside mt-2">
                    {symptoms.map(s => (
                      <li key={s.id} className="flex justify-between items-center">
                        <span>{s.name}</span>
                        <button onClick={() => deleteSymptom(s.id)} className="text-red-400 hover:text-red-600 text-sm">Изтрий</button>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              <form onSubmit={addSymptom} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <Listbox value={selectedGroup} onChange={(val) => {
                    setSelectedGroup(val)
                    setSelectedSymptom(null)
                  }}>
                    <div className="relative">
                      <Listbox.Button className="relative w-full bg-[#1a2a1f] border border-green-600 text-left py-2 pl-3 pr-10 rounded-md shadow-md text-white">
                        {selectedGroup || 'Избери категория'}
                        <ChevronUpDownIcon className="absolute inset-y-0 right-0 w-5 h-5 text-gray-400 mr-2" />
                      </Listbox.Button>
                      <Transition as={Fragment} leave="transition ease-in duration-100" leaveFrom="opacity-100" leaveTo="opacity-0">
                        <Listbox.Options className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-[#1a2a1f] text-white ring-1 ring-green-500">
                          {Object.keys(symptomOptions).map((group, i) => (
                            <Listbox.Option key={i} value={group} className={({ active }) =>
                              `cursor-pointer select-none px-4 py-2 ${active ? 'bg-green-600' : ''}`}>
                              {group}
                            </Listbox.Option>
                          ))}
                        </Listbox.Options>
                      </Transition>
                    </div>
                  </Listbox>

                  {selectedGroup && (
                    <Listbox value={selectedSymptom} onChange={setSelectedSymptom}>
                      <div className="relative">
                        <Listbox.Button className="relative w-full bg-[#1a2a1f] border border-green-600 text-left py-2 pl-3 pr-10 rounded-md shadow-md text-white">
                          {selectedSymptom || 'Избери симптом'}
                          <ChevronUpDownIcon className="absolute inset-y-0 right-0 w-5 h-5 text-gray-400 mr-2" />
                        </Listbox.Button>
                        <Transition as={Fragment} leave="transition ease-in duration-100" leaveFrom="opacity-100" leaveTo="opacity-0">
                          <Listbox.Options className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-[#1a2a1f] text-white ring-1 ring-green-500">
                            {symptomOptions[selectedGroup]?.map((symptom, i) => (
                              <Listbox.Option key={i} value={symptom} className={({ active }) =>
                                `cursor-pointer select-none px-4 py-2 ${active ? 'bg-green-600' : ''}`}>
                                {({ selected }) => (
                                  <span className={`block ${selected ? 'font-semibold' : ''}`}>
                                    {symptom}
                                    {selected && <CheckIcon className="w-4 h-4 inline ml-2" />}
                                  </span>
                                )}
                              </Listbox.Option>
                            ))}
                          </Listbox.Options>
                        </Transition>
                      </div>
                    </Listbox>
                  )}
                </div>

                <button
                  type="submit"
                  className="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 px-4 py-2 rounded-full font-semibold"
                  disabled={!selectedSymptom}
                >
                  Добави симптом
                </button>
              </form>
            </div>
          </div>
        )}

        {tab === 'reasoning' && (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold">Симптоми</h3>
              <ul className="list-disc list-inside">
                {reasoning.symptoms?.map((s, i) => <li key={i}>{s}</li>)}
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-semibold">Причини</h3>
              <ul className="list-disc list-inside">
                {reasoning.causes?.map((c, i) => <li key={i}>{c}</li>)}
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-semibold">Препоръки</h3>
              <ul className="list-disc list-inside">
                {reasoning.careActions?.map((a, i) => <li key={i}>{a}</li>)}
              </ul>
            </div>
          </div>
        )}

        {tab === 'sensors' && (
          <div className="space-y-6">
            <div className="border-b border-green-800 pb-4">
              <h3 className="text-lg font-semibold mb-2">📡 Сензорни стойности</h3>
              <button
                onClick={fetchSensorHistory}
                className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-full text-sm"
              >
                Обнови сензори
              </button>

              {sensorAlerts.length > 0 ? (
                <div className="mt-4 text-red-400">
                  <h4 className="font-medium">⚠️ Проблеми:</h4>
                  <ul className="list-disc list-inside">
                    {sensorAlerts.map((alert, i) => (
                      <li key={i}>{alert}</li>
                    ))}
                  </ul>
                </div>
              ) : (
                <p className="text-green-400 mt-4">✅ Всичко е в норма!</p>
              )}
            </div>

            {["temperature", "light", "humidity", "soilMoisture"].map(param => (
              <SensorChart key={param} parameter={param} data={sensorHistory} />
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
