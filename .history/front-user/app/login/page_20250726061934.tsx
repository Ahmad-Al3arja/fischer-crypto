"use client"

import type React from "react"

import { useState } from "react"
import { useAuth } from "@/contexts/AuthContext"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Phone, Lock, LogIn } from "lucide-react"
import Link from "next/link"


export default function LoginPage() {
  const [phoneNumber, setPhoneNumber] = useState("+")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setLoading(true)

    try {
      await login(phoneNumber, password)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-black py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-foreground tracking-wider">FISCHER</h1>
        </div>

        {/* Login Card */}
        <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
          <CardContent className="pt-8 pb-6 px-6">
            <div className="text-center mb-6">
              <h2 className="text-2xl font-bold text-foreground mb-2">مرحباً بك مجدداً</h2>
              <p className="text-sm text-muted-foreground">قم بتسجيل الدخول للوصول إلى حسابك</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <Alert variant="destructive">
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <div className="space-y-2">
                <Label htmlFor="phoneNumber" className="text-foreground">اسم المستخدم أو البريد الإلكتروني</Label>
                <div className="relative">
                  <Phone className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="phoneNumber"
                    type="tel"
                    placeholder="أدخل اسم المستخدم أو البريد الإلكتروني"
                    value={phoneNumber}
                    onChange={(e) => {
                      const value = e.target.value
                      // Ensure the value always starts with +
                      if (!value.startsWith('+')) {
                        setPhoneNumber('+' + value.replace(/^\+/, ''))
                      } else {
                        setPhoneNumber(value)
                      }
                    }}
                    className="pl-10 bg-gradient-to-r bg-card border-white text-foreground placeholder:text-muted-foreground"
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="text-foreground">كلمة المرور</Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="password"
                    type="password"
                    placeholder="أدخل كلمة المرور"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="pl-10 bg-gradient-to-r bg-card border-white text-foreground placeholder:text-muted-foreground"
                    required
                  />
                </div>
              </div>

              <div className="flex items-center">
                <div className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    id="remember"
                    className="rounded border-gray-700 bg-gradient-to-r from-custom-2e2e2e to-gray-900"
                  />
                  <Label htmlFor="remember" className="text-sm text-foreground">تذكرني</Label>
                </div>
              </div>

                             <Button type="submit" className="w-full bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-foreground" disabled={loading}>
                {loading ? (
                  <div className="flex items-center space-x-2">
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                    <span>تسجيل الدخول...</span>
                  </div>
                ) : (
                  <div className="flex items-center space-x-2">
                    <LogIn className="h-4 w-4" />
                    <span>تسجيل الدخول</span>
                  </div>
                )}
              </Button>
            </form>

            <div className="mt-6 text-center">
              <p className="text-sm text-muted-foreground">
                ليس لديك حساب ؟{" "}
                <Link href="/register" className="font-medium text-green-400 hover:text-green-300">
                  إنشاء حساب جديد
                </Link>
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Footer */}
        <div className="text-center mt-8">
          <p className="text-xs text-muted-foreground">FISCHER 2023 © جميع الحقوق محفوظة</p>
        </div>
        
        
      </div>
    </div>
  )
} 
