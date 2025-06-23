import { useEffect, useState } from 'react'
import axios from '../api/axios'
import { useAuth } from '../auth/AuthContext'
import { Link } from 'react-router-dom'
import SidebarNavigation from '../components/SidebarNavigation'

export default function Dashboard() {
  const { token } = useAuth()
  console.log("Токен:", token)
  const [plants, setPlants] = useState([])
  const [plantTypes, setPlantTypes] = useState([]) 
  const [showTypeSelector, setShowTypeSelector] = useState(false)

  const [form, setForm] = useState({
    name: '',
    type: '',
    family: '',
    imageUrl: ''
  })

  const imageMap = {
    "Cactus": "/images/cactus.png",
    "Calathea": "/images/flower.jpg",
    "Cyclamen": "/images/tree.jpg",
    "Orchid": "/images/cactus.jpg",
    "SnakePlant": "/images/fern.jpg"
  }

  useEffect(() => {
    fetchPlants()
    fetchPlantTypes()
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

  const fetchPlantTypes = async () => {
    try {
      const res = await axios.get('/plants/types', {
        headers: { Authorization: `Bearer ${token}` }
      })
      console.log('Типове от сървъра:', res.data)
      setPlantTypes(res.data) // [{ type: "", family: "" }]
    } catch (err) {
      console.error('Грешка при зареждане на типовете растения:', err)
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
      setForm({ name: '', type: '', family: '', imageUrl: '' })
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

  // Групиране по семейство
  const groupedByFamily = plantTypes.reduce((acc, { type, family }) => {
    if (!acc[family]) acc[family] = []
    acc[family].push(type)
    return acc
  }, {})

  return (
    <div className="flex bg-[#0f1e13] text-white min-h-screen">
      <SidebarNavigation />

      <main className="flex-1 p-6 space-y-12">
        {/* Форма за създаване */}
        <form
          onSubmit={handleSubmit}
          className="max-w-4xl grid grid-cols-1 md:grid-cols-3 gap-4 bg-black p-6 rounded-lg border border-green-600 shadow-lg"
        >
          <input
            name="name"
            placeholder="Име на растение"
            value={form.name}
            onChange={handleChange}
            className="bg-transparent border border-green-500 px-4 py-3 rounded text-white placeholder-green-400 focus:outline-none focus:ring-2 focus:ring-green-400"
          />

          <button
            type="submit"
            className="md:col-span-1 bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 py-3 rounded-full font-semibold text-white"
          >
            Създай
          </button>

          {/* Визуален селектор на тип растение */}
          <div
            className="col-span-full relative"
            onMouseEnter={() => setShowTypeSelector(true)}
            onMouseLeave={() => setShowTypeSelector(false)}
          >
            <p className="mb-2 font-semibold">Избери тип растение:</p>

            <div className="bg-gray-900 border border-green-600 p-4 rounded cursor-pointer inline-block">
              {form.type || 'Посочи с мишката, за да избереш'}
            </div>

            {showTypeSelector && (
              <div className="absolute z-50 mt-2 bg-black border border-green-600 p-4 rounded shadow-lg max-h-[400px] overflow-y-auto space-y-6 w-full md:w-[600px]">
                {Object.entries(groupedByFamily).map(([family, types]) => (
                  <div key={family}>
                    <h3 className="text-green-400 text-sm mb-2 italic">{family}</h3>
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                      {types.map((type) => {
                        const selected = form.type === type
                        return (
                          <div
                            key={type}
                            onClick={() => {
                              setForm({
                                ...form,
                                type,
                                family,
                                imageUrl: imageMap[type] || '/images/default.jpg'
                              })
                              setShowTypeSelector(false)
                            }}
                            className={`cursor-pointer p-2 rounded-lg border-2 transition duration-300 
                              ${selected ? 'border-green-500 scale-[1.02]' : 'border-gray-600'} 
                              hover:border-green-400 hover:scale-[1.02]`}
                          >
                            <img
                              src={imageMap[type] || '/images/default.jpg'}
                              alt={type}
                              className="w-32 h-24 object-cover rounded"
                            />
                            <p className="mt-2 text-center text-white font-medium">{type}</p>
                          </div>
                        )
                      })}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </form>

        {/* Заглавие и списък */}
        <h1 className="text-3xl font-bold">Твоите растения</h1>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          {plants.map((p) => (
            <Link to={`/plants/${p.name}`} key={p.name}>
              <div className="bg-black border border-green-600 p-4 rounded-xl shadow hover:scale-105 hover:shadow-xl transition-transform cursor-pointer">
                <img
                  src={p.imageUrl || '/images/default.jpg'}
                  alt={p.name}
                  className="h-40 w-full object-cover rounded mb-4"
                />
                <h2 className="text-xl font-bold">{p.name}</h2>
                <p className="text-sm text-green-300">{p.type}</p>
                <p className="text-xs text-green-500 italic">{p.family}</p>

                <button
                  onClick={(e) => {
                    e.preventDefault()
                    handleDelete(p.name)
                  }}
                  className="mt-2 text-sm text-red-400 hover:text-red-600 underline"
                >
                  Изтрий
                </button>
              </div>
            </Link>
          ))}
        </div>
      </main>
    </div>
  )
}
