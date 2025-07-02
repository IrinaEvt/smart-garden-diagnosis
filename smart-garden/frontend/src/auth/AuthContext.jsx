
import { createContext, useContext, useEffect, useState } from 'react'

const AuthContext = createContext()

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(null)
  const [userId, setUserId] = useState(null)

  useEffect(() => {
    const savedToken = localStorage.getItem('token')
    const savedUserId = localStorage.getItem('userId')
    if (savedToken) {
      setToken(savedToken)
      setUserId(savedUserId)
    }
  }, [])

  const login = (token, userId) => {
    localStorage.setItem('token', token)
    localStorage.setItem('userId', userId)
    setToken(token)
    setUserId(userId)
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    setToken(null)
    setUserId(null)
  }

  return (
    <AuthContext.Provider value={{ token, userId, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
