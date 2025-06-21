import { NavLink } from 'react-router-dom'

export default function SidebarNavigation() {
  const linkClass = ({ isActive }) =>
    `block px-4 py-2 rounded-md font-medium transition-colors duration-200 ${
      isActive ? 'bg-green-700 text-white' : 'text-gray-300 hover:bg-green-800 hover:text-white'
    }`

  return (
    <aside className="w-52 bg-[#0b1410] border-r border-green-700 min-h-screen p-4 hidden md:block">
      <h2 className="text-xl font-bold text-green-400 mb-6">Меню</h2>
      <nav className="space-y-2">
        <NavLink to="/dashboard" className={linkClass}>
          Табло
        </NavLink>
        <NavLink to="/info/plants" className={linkClass}>
          Типове растения
        </NavLink>
        <NavLink to="/info/symptoms" className={linkClass}>
          Симптоми
        </NavLink>
      </nav>
    </aside>
  )
}
