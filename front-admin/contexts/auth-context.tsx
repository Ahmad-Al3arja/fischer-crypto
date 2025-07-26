"use client"

import type React from "react"
import { createContext, useContext, useState, useEffect } from "react"
import { useRouter } from "next/navigation"

interface User {
  id: number
  username: string
  role: string
  token: string
}

interface AuthContextType {
  user: User | null
  login: (phoneNumber: string, password: string) => Promise<void>
  logout: () => void
  loading: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const router = useRouter()

  useEffect(() => {
    const token = localStorage.getItem("admin_token")
    const userData = localStorage.getItem("admin_user")

    if (token && userData) {
      try {
        const parsedUser = JSON.parse(userData)
        if (parsedUser.role === "ADMIN") {
          setUser({ ...parsedUser, token })
        } else {
          localStorage.removeItem("admin_token")
          localStorage.removeItem("admin_user")
        }
      } catch (error) {
        localStorage.removeItem("admin_token")
        localStorage.removeItem("admin_user")
      }
    }
    setLoading(false)
  }, [])

  const login = async (phoneNumber: string, password: string) => {
    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ phoneNumber, password }),
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || "Login failed")
      }

      const data = await response.json()

      if (data.role !== "ADMIN") {
        throw new Error("Access denied. Admin privileges required.")
      }

      const userData = {
        id: data.userId,
        username: data.username,
        role: data.role,
      }

      setUser({ ...userData, token: data.token })
      localStorage.setItem("admin_token", data.token)
      localStorage.setItem("admin_user", JSON.stringify(userData))

      router.push("/dashboard")
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem("admin_token")
    localStorage.removeItem("admin_user")
    router.push("/login")
  }

  return <AuthContext.Provider value={{ user, login, logout, loading }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
