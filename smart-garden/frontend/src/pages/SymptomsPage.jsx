// pages/SymptomsPage.jsx
import SidebarNavigation from '../components/SidebarNavigation'

const symptoms = [
  {
    name: 'Пожълтели листа',
    causes: ['Прекалено поливане', 'Липса на светлина'],
    actions: ['Намали поливането', 'Премести растението на светло място']
  },
  {
    name: 'Кафяви краища',
    causes: ['Суха почва', 'Ниска влажност на въздуха'],
    actions: ['Полей растението', 'Увеличи влажността с пулверизиране']
  }
]

export default function SymptomsPage() {
  return (
    <div className="flex bg-[#0f1e13] text-white min-h-screen">
      <SidebarNavigation />
      <main className="flex-1 p-8 space-y-6">
        <h1 className="text-3xl font-bold mb-4">🩺 Симптоми и причини</h1>
        {symptoms.map((s, i) => (
          <div key={i} className="border-b border-green-800 pb-4">
            <h3 className="text-xl font-semibold">{s.name}</h3>
            <p className="mt-2"><strong>Причини:</strong></p>
            <ul className="list-disc list-inside text-green-300 ml-4">
              {s.causes.map((c, j) => <li key={j}>{c}</li>)}
            </ul>
            <p className="mt-2"><strong>Препоръки:</strong></p>
            <ul className="list-disc list-inside text-green-400 ml-4">
              {s.actions.map((a, j) => <li key={j}>{a}</li>)}
            </ul>
          </div>
        ))}
      </main>
    </div>
  )
}
