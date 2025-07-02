import { useEffect, useState } from 'react'
import SidebarNavigation from '../components/SidebarNavigation'
import axios from '../api/axios'
import { Sun, Droplet } from 'lucide-react' 

export default function PlantTypesPage() {
  const [families, setFamilies] = useState([])

  useEffect(() => {
    fetchFamilies()
  }, [])

  const fetchFamilies = async () => {
    try {
      const res = await axios.get('/families')
      setFamilies(res.data)
    } catch (err) {
      console.error('Грешка при зареждане на семействата:', err)
    }
  }

  const getImageForFamily = (name) => {
    const images = {
      'Primulaceae': '/images/families/primulaceae.png',
      'Cactaceae': '/images/opuntia.jpg',
      'Marantaceae': '/images/ctenanthe.png',
      'Orchidaceae': '/images/orchid.jpg',
      'Asparagaceae': '/images/snakeplant.jpg',
    }
    return images[name] || '/images/default.jpg'
  }

  const mapLight = (value) => {
  const v = value.toLowerCase()
  if (v.includes('пряко')) return 'direct'
  if (v.includes('дифузна') || v.includes('полусянка') || v.includes('светло')) return 'indirect'
  if (v.includes('слабо') || v.includes('без светлина')) return 'low'
  return ''
}

const mapHumidity = (value) => {
  const v = value.toLowerCase()
  if (v.includes('влага') && v.includes('постоянна')) return 'high'
  if (v.includes('умерено')) return 'moderate'
  if (v.includes('сухо') || v.includes('рядко')) return 'low'
  return ''
}


  const highlight = (value, match) => {
    return (
      <span className={value === match ? 'underline text-green-300 font-semibold' : 'text-gray-400'}>
        {match}
      </span>
    )
  }

  return (
    <div className="flex bg-[#0f1e13] text-white min-h-screen">
      <SidebarNavigation />
      <main className="flex-1 p-8 space-y-6">
        <h1 className="text-3xl font-bold mb-6">🌿 Семейства растения</h1>
        <div className="flex flex-col space-y-8">
         {families.map((family) => (
  <div key={family.id} className="w-full bg-black border border-green-600 p-6 rounded-2xl shadow space-y-4">
    <div className="flex items-center gap-6">
      <img
        src={getImageForFamily(family.scientificName)}
        alt={family.scientificName}
        className="w-28 h-28 object-cover rounded-xl border border-green-700"
      />
      <div>
        <h2 className="text-2xl font-semibold">{family.scientificName}</h2>
        <p className="text-green-400 italic">{family.environment}</p>
      </div>
    </div>

    <p className="text-green-200 text-sm">{family.generalDescription}</p>

<div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-2 text-green-400 text-sm">
  <div><strong>🌸 Цветове:</strong> {family.flowerDescription}</div>
  <div><strong>🌱 Листа:</strong> {family.leafType}</div>
  <div><strong>🧩 Корен:</strong> {family.rootType}</div>
  <div><strong>🧬 Родове:</strong> {family.commonGenera}</div>
  <div><strong>🔍 Известни видове:</strong> {family.notableSpecies}</div>
  <div><strong>☠️ Токсичност:</strong> {family.toxicity}</div>
  <div><strong>🪴 Почва:</strong> {family.careSoil}</div>
  <div><strong>🌡️ Температура:</strong> {family.careTemperature}</div>
  <div><strong>🚿 Поливане:</strong> {family.careWatering}</div>
  <div><strong>🐛 Вредители:</strong> {family.commonPests}</div>
  <div><strong>🦠 Болести:</strong> {family.commonDiseases}</div>
</div>


    <div className="flex items-center gap-16 mt-4">
      <div>
        <div className="flex items-center gap-2 text-sm font-medium mb-1"><Sun size={18} /> Светлина</div>
        <div className="flex gap-4">
          {highlight(mapLight(family.careLight), 'direct')}
          {highlight(mapLight(family.careLight), 'indirect')}
          {highlight(mapLight(family.careLight), 'low')}
        </div>
      </div>
      <div>
        <div className="flex items-center gap-2 text-sm font-medium mb-1"><Droplet size={18} /> Влажност</div>
        <div className="flex gap-4">
          {highlight(mapHumidity(family.careWatering), 'high')}
          {highlight(mapHumidity(family.careWatering), 'moderate')}
          {highlight(mapHumidity(family.careWatering), 'low')}
        </div>
      </div>
    </div>
  </div>
))}
        </div>
      </main>
    </div>
  )
}
