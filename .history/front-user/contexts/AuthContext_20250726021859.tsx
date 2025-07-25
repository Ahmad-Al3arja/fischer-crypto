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
  checkAuth: () => Promise<boolean>
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
    try {
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
    } catch (error) {
      console.error("AuthContext initialization error:", error)
      // Handle any localStorage errors
      apiService.setAuthToken(null)
    } finally {
      setLoading(false)
    }
  }, [])

  // Ensure API service token is always synchronized
  useEffect(() => {
    if (token) {
      apiService.setAuthToken(token)
    }
  }, [token])

  const login = async (phoneNumber: string, password: string) => {
    try {
      const response = await apiService.login({ phoneNumber, password })

      // Check if response has the expected structure
      if (!response.token || !response.userId || !response.username || !response.role) {
        throw new Error("Invalid response format from server")
      }

      const userData = {
        id: response.userId,
        username: response.username,
        role: response.role,
      }

      // Set state first
      setToken(response.token)
      setUser(userData)

      // Save to localStorage with error handling
      try {
        localStorage.setItem("token", response.token)
        localStorage.setItem("user", JSON.stringify(userData))
        
        // Verify storage
        const savedToken = localStorage.getItem("token")
        const savedUser = localStorage.getItem("user")
        
        if (!savedToken || !savedUser) {
          throw new Error("Failed to save authentication data to localStorage")
        }
      } catch (storageError) {
        throw new Error(`Storage error: ${storageError}`)
      }

      apiService.setAuthToken(response.token)
      router.push("/dashboard")
    } catch (error: any) {
      throw new Error(error.message || "Login failed")
    }
  }

  const register = async (data: RegisterData) => {
    try {
      const response = await apiService.register(data)

      // Check if response has the expected structure
      if (!response.token || !response.userId || !response.username || !response.role) {
        throw new Error("Invalid response format from server")
      }

      const userData = {
        id: response.userId,
        username: response.username,
        role: response.role,
      }

      // Set state first
      setToken(response.token)
      setUser(userData)

      // Save to localStorage with error handling
      try {
        localStorage.setItem("token", response.token)
        localStorage.setItem("user", JSON.stringify(userData))
        
        // Verify storage
        const savedToken = localStorage.getItem("token")
        const savedUser = localStorage.getItem("user")
        
        if (!savedToken || !savedUser) {
          throw new Error("Failed to save authentication data to localStorage")
        }
      } catch (storageError) {
        throw new Error(`Storage error: ${storageError}`)
      }

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

  const checkAuth = async (): Promise<boolean> => {
    if (!token) return false
    
    try {
      // Try to fetch user profile to validate token
      await apiService.getProfile()
      return true
    } catch (error) {
      // Token is invalid, clear it
      logout()
      return false
    }
  }

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, loading, checkAuth }}>{children}</AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
