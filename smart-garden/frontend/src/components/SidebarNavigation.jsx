import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import { LayoutDashboard, Leaf, Biohazard } from 'lucide-react'

export default function SidebarNavigation() {
  const [isExpandedByClick, setIsExpandedByClick] = useState(false)

  const toggleSidebar = () => {
    setIsExpandedByClick(prev => !prev)
  }

  const linkClass = ({ isActive }) =>
    `group flex items-center gap-3 px-4 py-2 rounded-md font-medium transition-all duration-200 
     ${isActive ? 'bg-green-700 text-white' : 'text-gray-300 hover:bg-green-800 hover:text-white'}`

  const iconClass = (isExpandedByClick) =>
    `rounded p-1 bg-white text-black 
     ${isExpandedByClick ? 'w-10 h-10' : 'w-6 h-6'} 
     group-hover:w-10 group-hover:h-10 
     transition-all duration-300`

  const sidebarClasses = `
    ${isExpandedByClick ? 'w-52' : 'w-20 hover:w-52'} 
    group transition-all duration-300 
    bg-[#0b1410] border-r border-green-700 
    min-h-screen p-4 hidden md:block 
    overflow-x-hidden overflow-y-auto relative
  `

  return (
    <aside className={sidebarClasses}>
      <button
        onClick={toggleSidebar}
        className="absolute top-4 right-4 text-green-400 hover:text-white transition-colors z-10"
      >
        {isExpandedByClick ? '<' : '>'}
      </button>

      <h2 className="text-xl font-bold text-green-400 mb-6 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-200">
        Меню
      </h2>

      <nav className="space-y-2">
<NavLink to="/dashboard" className={linkClass}>
  <LayoutDashboard className="bg-green-600 text-black p-1 w-6 h-6 group-hover:w-10 group-hover:h-10 transition-all duration-300 rounded" />
  <span className="whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-300">
    Табло
  </span>
</NavLink>

<NavLink to="/info/plants" className={linkClass}>
  <Leaf className="bg-green-600 text-black p-1 w-6 h-6 group-hover:w-10 group-hover:h-10 transition-all duration-300 rounded" />
  <span className="whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-300">
    Типове растения
  </span>
</NavLink>

<NavLink to="/info/symptoms" className={linkClass}>
  <Biohazard className="bg-green-600 text-black p-1 w-6 h-6 group-hover:w-10 group-hover:h-10 transition-all duration-300 rounded" />
  <span className="whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-300">
    Симптоми
  </span>
</NavLink>
      </nav>
    </aside>
  )
}
