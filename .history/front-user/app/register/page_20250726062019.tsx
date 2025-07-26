"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useAuth } from "@/contexts/AuthContext"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { User, Phone, Lock, Mail, Hash, UserPlus } from "lucide-react"
import Link from "next/link"
import { useSearchParams } from "next/navigation"


export default function RegisterPage() {
  const [formData, setFormData] = useState({
    fullName: "",
    username: "",
    phoneNumber: "+",
    password: "",
    confirmPassword: "",
    referralCode: "",
  })
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const searchParams = useSearchParams()

  // Extract referral code from URL parameters on component mount
  useEffect(() => {
    const refCode = searchParams.get('ref')
    if (refCode) {
      setFormData(prev => ({ ...prev, referralCode: refCode }))
    }
  }, [searchParams])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")

    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match")
      return
    }

    if (formData.password.length < 6) {
      setError("Password must be at least 6 characters long")
      return
    }

    setLoading(true)

    try {
      await register(formData)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (field: string, value: string) => {
    if (field === "phoneNumber") {
      // Ensure the value always starts with +
      if (!value.startsWith('+')) {
        value = '+' + value.replace(/^\+/, '')
      }
    }
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-black py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-foreground tracking-wider">FISCHER</h1>
        </div>

        {/* Register Card */}
        <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
          <CardContent className="pt-8 pb-6 px-6">
            <div className="text-center mb-6">
              <h2 className="text-2xl font-bold text-foreground mb-2">إنشاء حساب جديد</h2>
              <p className="text-sm text-muted-foreground">أدخل بياناتك لإنشاء حساب جديد</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <Alert variant="destructive">
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <div className="space-y-2">
                <Label htmlFor="referralCode" className="text-foreground flex items-center">
                  كود الدعوة *
                  <Mail className="h-4 w-4 ml-1 text-green-400" />
                </Label>
                <div className="relative">
                  <Mail className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="referralCode"
                    type="text"
                    placeholder="أدخل كود الدعوة"
                    value={formData.referralCode}
                    onChange={(e) => handleInputChange("referralCode", e.target.value)}
                    className="pl-10 bg-gradient-to-r bg-card border-white text-foreground placeholder:text-muted-foreground"
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="username" className="text-foreground">اسم المستخدم *</Label>
                <div className="relative">
                  <Hash className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="username"
                    type="text"
                    placeholder="أدخل اسم المستخدم"
                    value={formData.username}
                    onChange={(e) => handleInputChange("username", e.target.value)}
                    className="pl-10 bg-gradient-to-r bg-card border-white text-foreground placeholder:text-muted-foreground"
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="fullName" className="text-foreground">الاسم *</Label>
                <div className="relative">
                  <User className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="fullName"
                    type="text"
                    placeholder="أدخل اسمك الكامل"
                    value={formData.fullName}
                    onChange={(e) => handleInputChange("fullName", e.target.value)}
                    className="pl-10 bg-gradient-to-r bg-card border-white text-foreground placeholder:text-muted-foreground"
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="phoneNumber" className="text-foreground">رقم الهاتف *</Label>
                <div className="relative">
                  <Phone className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="phoneNumber"
                    type="tel"
                    placeholder="أدخل رقم الهاتف"
                    value={formData.phoneNumber}
                    onChange={(e) => handleInputChange("phoneNumber", e.target.value)}
                    className="pl-10 bg-gradient-to-r bg-card border-white text-foreground placeholder:text-muted-foreground"
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="text-foreground">كلمة المرور *</Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="password"
                    type="password"
                    placeholder="أدخل كلمة المرور"
                    value={formData.password}
                    onChange={(e) => handleInputChange("password", e.target.value)}
                    className="pl-10 bg-gradient-to-r bg-card border-white text-foreground placeholder:text-muted-foreground"
                    required
                  />
                </div>
                <p className="text-xs text-muted-foreground">كلمة المرور يجب أن تحتوي على 8 أحرف على الأقل</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="text-foreground">تأكيد كلمة المرور *</Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    id="confirmPassword"
                    type="password"
                    placeholder="تأكيد كلمة المرور"
                    value={formData.confirmPassword}
                    onChange={(e) => handleInputChange("confirmPassword", e.target.value)}
                    className="pl-10 bg-gradient-to-r bg-card border-white text-foreground placeholder:text-muted-foreground"
                    required
                  />
                </div>
              </div>

                             <Button type="submit" className="w-full bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-foreground" disabled={loading}>
                {loading ? (
                  <div className="flex items-center space-x-2">
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                    <span>إنشاء الحساب...</span>
                  </div>
                ) : (
                  <div className="flex items-center space-x-2">
                    <UserPlus className="h-4 w-4" />
                    <span>إنشاء الحساب</span>
                  </div>
                )}
              </Button>
            </form>

            <div className="mt-6 text-center">
              <p className="text-sm text-muted-foreground">
                لديك حساب بالفعل؟{" "}
                <Link href="/login" className="font-medium text-green-400 hover:text-green-300">
                  تسجيل الدخول هنا
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
