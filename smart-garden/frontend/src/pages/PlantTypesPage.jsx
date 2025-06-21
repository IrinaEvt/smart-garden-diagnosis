
import SidebarNavigation from '../components/SidebarNavigation'

const plantTypes = [
  { name: 'Сукуленти', description: 'Обичат слънце, не изискват често поливане.' },
  { name: 'Тропически', description: 'Изискват висока влажност и топлина.' },
  { name: 'Цъфтящи', description: 'Нуждаят се от светлина и хранителни вещества.' },
  { name: 'Листни', description: 'Красиви листа, умерено поливане и светлина.' },
]

export default function PlantTypesPage() {
  return (
    <div className="flex bg-[#0f1e13] text-white min-h-screen">
      <SidebarNavigation />
      <main className="flex-1 p-8 space-y-6">
        <h1 className="text-3xl font-bold mb-4">🌿 Типове растения</h1>
        <ul className="space-y-4">
          {plantTypes.map((type, index) => (
            <li key={index} className="border-b border-green-700 pb-2">
              <h3 className="text-xl font-semibold">{type.name}</h3>
              <p className="text-green-300">{type.description}</p>
            </li>
          ))}
        </ul>
      </main>
    </div>
  )
}
