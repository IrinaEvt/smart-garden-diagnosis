import { useEffect, useState } from 'react'
import SidebarNavigation from '../components/SidebarNavigation'
import axios from '../api/axios'
import { AlertCircle, Leaf, Bug, Droplet, Info } from 'lucide-react'
import { getReadableSymptomName } from '../utils/symptomNameMap'

export default function SymptomPage() {
  const [symptoms, setSymptoms] = useState([])
  const [filter, setFilter] = useState('All')

  useEffect(() => {
    fetchSymptoms()
  }, [])

  const fetchSymptoms = async () => {
    try {
      const res = await axios.get('/symptoms')
      setSymptoms(res.data)
    } catch (err) {
      console.error('Грешка при зареждане на симптомите:', err)
    }
  }

  const filterOptions = [
  { key: 'All', label: ' Всички' },
  { key: 'Leaf', label: ' Листа' },
  { key: 'Stem', label: ' Стъбло' },
  { key: 'Root', label: ' Корени' },
]

  const getIcon = (part) => {
    switch (part) {
      case 'Leaf': return <Leaf size={20} className="text-green-400" />
      case 'Root': return <Droplet size={20} className="text-yellow-400" />
      case 'Stem': return <Bug size={20} className="text-red-400" />
      default: return <AlertCircle size={20} />
    }
  }

  const getImageForSymptom = (name) => {
  const images = {
    'InterveinalChlorosis': '/images/symptoms/interveinal-chlorosis.png',
    'BaseRotFromSoil': '/images/symptoms/root-rot.png',
    'LeafEdgeBurns': '/images/symptoms/tip-burn.png',
    'StemSofteningAndCollapse': '/images/symptoms/stem-soft.png',
    'LeafYellowing': '/images/symptoms/yellow-leaves.jpg',
    'PowderyWhiteSubstanceOnLeaves': '/images/symptoms/powdery.png',
    'WiltedLeaves': '/images/symptoms/wilted.jpg',
  }

  return images[name] || '/images/symptoms/default.jpg'
}

  const filteredSymptoms = filter === 'All'
    ? symptoms
    : symptoms.filter(sym => sym.partAffected === filter)



  return (
    <div className="flex bg-[#0f1e13] text-white min-h-screen">
      <SidebarNavigation />
      <main className="flex-1 p-8 space-y-6">
        <h1 className="text-3xl font-bold mb-6">🦠 Симптоми при растенията</h1>

        <div className="flex gap-4 mb-6">
      {filterOptions.map(({ key, label }) => (
  <button
    key={key}
    onClick={() => setFilter(key)}
    className={`px-4 py-1 rounded-full border ${
      filter === key ? 'border-green-400 text-green-300 font-semibold' : 'border-gray-600 text-gray-400'
    }`}
  >
    {label}
  </button>
))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredSymptoms.map((symptom) => (
            <div
              key={symptom.id || `${symptom.symptomName}-${symptom.partAffected}`}
              className="bg-black border border-green-700 rounded-2xl p-6 shadow hover:shadow-green-500/20 transition"
            >
              <div className="flex items-center gap-3 mb-2">
                {getIcon(symptom.partAffected)}
                <h2 className="text-xl font-semibold">{getReadableSymptomName(symptom.symptomName)}</h2>
              </div>
              <img
                src={getImageForSymptom(symptom.symptomName)}
                alt={symptom.symptomName}
                className="w-full h-40 object-cover rounded-xl mb-3 border border-green-600"
              />
              <p className="text-green-300 text-sm mb-2">{symptom.visualDescription}</p>
              <div className="text-xs text-gray-400 flex justify-between items-center">
                <span>📍 {symptom.partAffected}</span>
                <span>
                  {symptom.severity === 'High' ? '🔴 Сериозна' : symptom.severity === 'Medium' ? '🟡 Средна' : '🟢 Лека'}
                </span>
              </div>
              <div className="mt-4 text-right">
                <button className="inline-flex items-center gap-1 text-sm text-green-400 hover:underline">
                  <Info size={16} /> Детайли
                </button>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  )
}
