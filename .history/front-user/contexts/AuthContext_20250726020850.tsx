"use client"

import type React from "react"
import { createContext, useContext, useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { apiService } from "@/services/api"

interface User {
  id: number
  username: string
  role: string
}

interface AuthContextType {
  user: User | null
  token: string | null
  login: (phoneNumber: string, password: string) => Promise<void>
  register: (data: RegisterData) => Promise<void>
  logout: () => void
  loading: boolean
}

interface RegisterData {
  fullName: string
  username: string
  phoneNumber: string
  password: string
  confirmPassword: string
  referralCode: string
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const router = useRouter()

  useEffect(() => {
    const savedToken = localStorage.getItem("token")
    const savedUser = localStorage.getItem("user")

    if (savedToken && savedUser) {
      setToken(savedToken)
      setUser(JSON.parse(savedUser))
      apiService.setAuthToken(savedToken)
    } else {
      // Clear any existing token in API service if no token in localStorage
      apiService.setAuthToken(null)
    }
    setLoading(false)
  }, [])

  const login = async (phoneNumber: string, password: string) => {
    try {
      const response = await apiService.login({ phoneNumber, password })

      setToken(response.token)
      setUser({
        id: response.userId,
        username: response.username,
        role: response.role,
      })

      localStorage.setItem("token", response.token)
      localStorage.setItem(
        "user",
        JSON.stringify({
          id: response.userId,
          username: response.username,
          role: response.role,
        }),
      )

      apiService.setAuthToken(response.token)
      router.push("/dashboard")
    } catch (error: any) {
      throw new Error(error.message || "Login failed")
    }
  }

  const register = async (data: RegisterData) => {
    try {
      const response = await apiService.register(data)

      setToken(response.token)
      setUser({
        id: response.userId,
        username: response.username,
        role: response.role,
      })

      localStorage.setItem("token", response.token)
      localStorage.setItem(
        "user",
        JSON.stringify({
          id: response.userId,
          username: response.username,
          role: response.role,
        }),
      )

      apiService.setAuthToken(response.token)
      router.push("/dashboard")
    } catch (error: any) {
      throw new Error(error.message || "Registration failed")
    }
  }

  const logout = () => {
    setUser(null)
    setToken(null)
    localStorage.removeItem("token")
    localStorage.removeItem("user")
    apiService.setAuthToken(null)
    router.push("/login")
  }

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, loading }}>{children}</AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
