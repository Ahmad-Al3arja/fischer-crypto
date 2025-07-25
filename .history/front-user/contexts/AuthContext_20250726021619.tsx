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
    try {
      console.log("Initializing AuthContext...")
      const savedToken = localStorage.getItem("token")
      const savedUser = localStorage.getItem("user")

      console.log("Saved token found:", !!savedToken)
      console.log("Saved user found:", !!savedUser)

      if (savedToken && savedUser) {
        setToken(savedToken)
        setUser(JSON.parse(savedUser))
        apiService.setAuthToken(savedToken)
        console.log("Restored authentication from localStorage")
      } else {
        // Clear any existing token in API service if no token in localStorage
        apiService.setAuthToken(null)
        console.log("No saved authentication found")
      }
      
      // Ensure API service is properly initialized
      apiService.reinitializeToken()
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
      console.log("AuthContext: Token synchronized with API service")
    }
  }, [token])

  const login = async (phoneNumber: string, password: string) => {
    try {
      console.log("Starting login process...")
      const response = await apiService.login({ phoneNumber, password })
      console.log("Login response received:", response)

      // Check if response has the expected structure
      if (!response.token || !response.userId || !response.username || !response.role) {
        console.error("Invalid response structure:", response)
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
        console.log("Saving to localStorage...")
        localStorage.setItem("token", response.token)
        localStorage.setItem("user", JSON.stringify(userData))
        
        // Verify storage
        const savedToken = localStorage.getItem("token")
        const savedUser = localStorage.getItem("user")
        
        console.log("Saved token:", savedToken ? "Present" : "Missing")
        console.log("Saved user:", savedUser ? "Present" : "Missing")
        
        if (!savedToken || !savedUser) {
          throw new Error("Failed to save authentication data to localStorage")
        }
      } catch (storageError) {
        console.error("localStorage error:", storageError)
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
