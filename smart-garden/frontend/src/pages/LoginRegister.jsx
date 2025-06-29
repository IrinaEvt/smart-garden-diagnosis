import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from '../api/plainAxios'
import { useAuth } from '../auth/AuthContext'
import toast from 'react-hot-toast'

export default function LoginRegister() {
  const [isLogin, setIsLogin] = useState(true)
  const [form, setForm] = useState({ username: '', password: '', confirmPassword: '', })
  const navigate = useNavigate()
  const { login } = useAuth()


  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

     if (!form.username.trim() || !form.password.trim()) {
    toast.error('Моля, попълнете потребителско име и парола.')
    return
  }

  if (!isLogin && form.password !== form.confirmPassword) {
  toast.error('Паролите не съвпадат.')
  return
}
    try {
      if (isLogin) {
        const res = await axios.post('/auth/login', form)
        login(res.data.token, res.data.userId)
        toast.success('Успешен вход!')
        navigate('/dashboard')
      } else {
        await axios.post('/auth/register', form)
        toast.success('Успешна регистрация. Моля, влезте.')
        setIsLogin(true)
        setForm({ username: '', password: '' })
      }
    } catch (err) {
      toast.error(
        isLogin
          ? 'Грешно потребителско име или парола'
          : 'Потребителското име вече съществува'
      )
    }
  }

  return (
    <div className="relative w-full h-screen overflow-hidden bg-black text-white">
      <div className="absolute top-6 left-1/2 transform -translate-x-1/2 z-50">
        <button
          onClick={() => setIsLogin(!isLogin)}
          className="bg-green-600 hover:bg-green-700 px-6 py-2 rounded-full text-xs uppercase tracking-wider shadow-lg transition duration-300"
        >
          {isLogin ? 'Регистрация' : 'Вход'}
        </button>
      </div>

   
      <div
        className={`flex w-full h-full transition-all duration-700 ease-in-out ${
          isLogin ? 'flex-row' : 'flex-row-reverse'
        }`}
      >
      
        <div className="w-1/2 h-full flex items-center justify-center p-12 bg-black transition-all duration-700">
          <div className="w-full max-w-md">
            <h2 className="text-4xl font-bold mb-4">
              {isLogin ? 'Вход' : 'Регистрация'}
            </h2>
            <p className="text-sm text-gray-400 mb-8">
              {isLogin
                ? 'Влез в профила си.'
                : 'Създай акаунт, за да използваш платформата.'}
            </p>

            <form onSubmit={handleSubmit} className="space-y-5">
              <input
                name="username"
                value={form.username}
                onChange={handleChange}
                placeholder="Потребителско име"
                className="w-full px-4 py-3 border border-green-500 rounded bg-transparent text-white placeholder-green-400 focus:outline-none focus:ring-2 focus:ring-green-400"
              />
              <input
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                placeholder="Парола"
                className="w-full px-4 py-3 border border-green-500 rounded bg-transparent text-white placeholder-green-400 focus:outline-none focus:ring-2 focus:ring-green-400"
              />

              {!isLogin && (
                <input
                  name="confirmPassword"
                  type="password"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="Потвърди паролата"
                  className="w-full px-4 py-3 border border-green-500 rounded bg-transparent text-white placeholder-green-400 focus:outline-none focus:ring-2 focus:ring-green-400"
                />
              )}

              <button
                type="submit"
                className="w-full bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 py-3 rounded-full font-semibold text-white"
              >
                {isLogin ? 'Вход' : 'Регистрация'}
              </button>
            </form>

            <div className="flex justify-center mt-6 space-x-6 text-green-400 text-xl">
              <button>f</button>
              <button>G+</button>
              <button>t</button>
            </div>
          </div>
        </div>

        {/* Дясна страна – снимка */}
        <div
          className="w-1/2 h-full bg-cover bg-center transition-all duration-700"
          style={{ backgroundImage: "url(/fern.jpg)" }}
        />
      </div>
    </div>
  )
}
