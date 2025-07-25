// front-user/app/register/page.tsx
"use client"

import type React from "react"

import { useState } from "react"
import { useAuth } from "@/contexts/AuthContext"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { User, Phone, Lock, UserPlus, Gift, Eye, EyeOff, CreditCard } from "lucide-react"
import Link from "next/link"

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    fullName: "",
    username: "",
    phoneNumber: "",
    password: "",
    confirmPassword: "",
    referralCode: "",
  })
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")

    if (formData.password !== formData.confirmPassword) {
      setError("كلمات المرور غير متطابقة")
      return
    }

    if (formData.password.length < 6) {
      setError("كلمة المرور يجب أن تحتوي على 6 أحرف على الأقل")
      return
    }

    if (!formData.referralCode.trim()) {
      setError("كود الدعوة مطلوب")
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

  return (
    <div className="min-h-screen bg-background fischer-gradient-bg flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md">
        {/* FISCHER Logo */}
        <div className="text-center mb-8">
          <h1 className="text-5xl font-bold text-foreground tracking-wider">FISCHER</h1>
        </div>

        <Card className="fischer-card fischer-glass">
          <CardHeader className="text-center space-y-2">
            <CardTitle className="text-2xl font-bold text-foreground">إنشاء حساب جديد</CardTitle>
            <CardDescription className="text-muted-foreground">
              أدخل بياناتك لإنشاء حساب جديد
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <Alert variant="destructive" className="bg-destructive/10 border-destructive/20">
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <div className="space-y-2">
                <Label htmlFor="referralCode" className="text-foreground font-medium flex items-center space-x-1 space-x-reverse">
                  <span className="text-destructive">*</span>
                  <span>كود الدعوة</span>
                  <div className="w-4 h-4 bg-primary/10 rounded-full flex items-center justify-center">
                    <Gift className="h-2 w-2 text-primary" />
                  </div>
                </Label>
                <div className="relative">
                  <CreditCard className="absolute right-3 top-3 h-5 w-5 text-muted-foreground" />
                  <Input
                    id="referralCode"
                    name="referralCode"
                    type="text"
                    placeholder="أدخل كود الدعوة"
                    value={formData.referralCode}
                    onChange={handleChange}
                    className="fischer-input pr-12 text-right"
                    required
                    dir="ltr"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="username" className="text-foreground font-medium flex items-center space-x-1 space-x-reverse">
                  <span className="text-destructive">*</span>
                  <span>اسم المستخدم</span>
                </Label>
                <div className="relative">
                  <User className="absolute right-3 top-3 h-5 w-5 text-muted-foreground" />
                  <Input
                    id="username"
                    name="username"
                    type="text"
                    placeholder="أدخل اسم المستخدم"
                    value={formData.username}
                    onChange={handleChange}
                    className="fischer-input pr-12 text-right"
                    required
                    dir="ltr"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="fullName" className="text-foreground font-medium flex items-center space-x-1 space-x-reverse">
                  <span className="text-destructive">*</span>
                  <span>الاسم</span>
                </Label>
                <div className="relative">
                  <User className="absolute right-3 top-3 h-5 w-5 text-muted-foreground" />
                  <Input
                    id="fullName"
                    name="fullName"
                    type="text"
                    placeholder="أدخل اسمك الكامل"
                    value={formData.fullName}
                    onChange={handleChange}
                    className="fischer-input pr-12 text-right"
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="phoneNumber" className="text-foreground font-medium flex items-center space-x-1 space-x-reverse">
                  <span className="text-destructive">*</span>
                  <span>رقم الهاتف</span>
                </Label>
                <div className="relative">
                  <Phone className="absolute right-3 top-3 h-5 w-5 text-muted-foreground" />
                  <Input
                    id="phoneNumber"
                    name="phoneNumber"
                    type="tel"
                    placeholder="أدخل رقم الهاتف"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    className="fischer-input pr-12 text-right"
                    required
                    dir="ltr"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="text-foreground font-medium flex items-center space-x-1 space-x-reverse">
                  <span className="text-destructive">*</span>
                  <span>كلمة المرور</span>
                </Label>
                <div className="relative">
                  <Lock className="absolute right-3 top-3 h-5 w-5 text-muted-foreground" />
                  <Input
                    id="password"
                    name="password"
                    type={showPassword ? "text" : "password"}
                    placeholder="أدخل كلمة المرور"
                    value={formData.password}
                    onChange={handleChange}
                    className="fischer-input pr-12 pl-12 text-right"
                    required
                    dir="ltr"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute left-3 top-3 text-muted-foreground hover:text-foreground transition-colors"
                  >
                    {showPassword ? (
                      <EyeOff className="h-5 w-5" />
                    ) : (
                      <Eye className="h-5 w-5" />
                    )}
                  </button>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="text-foreground font-medium flex items-center space-x-1 space-x-reverse">
                  <span className="text-destructive">*</span>
                  <span>كلمة المرور</span>
                </Label>
                <div className="relative">
                  <Lock className="absolute right-3 top-3 h-5 w-5 text-muted-foreground" />
                  <Input
                    id="confirmPassword"
                    name="confirmPassword"
                    type={showConfirmPassword ? "text" : "password"}
                    placeholder="أدخل كلمة المرور"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className="fischer-input pr-12 pl-12 text-right"
                    required
                    dir="ltr"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute left-3 top-3 text-muted-foreground hover:text-foreground transition-colors"
                  >
                    {showConfirmPassword ? (
                      <EyeOff className="h-5 w-5" />
                    ) : (
                      <Eye className="h-5 w-5" />
                    )}
                  </button>
                </div>
              </div>

              <div className="pt-2">
                <p className="text-xs text-muted-foreground text-center">
                  كلمة المرور يجب أن تحتوي على 8 أحرف على الأقل
                </p>
              </div>

              <Button 
                type="submit" 
                className="w-full fischer-button-primary h-12 text-lg"
                disabled={loading}
              >
                {loading ? (
                  <div className="flex items-center justify-center space-x-2 space-x-reverse">
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-current"></div>
                    <span>جاري إنشاء الحساب...</span>
                  </div>
                ) : (
                  <div className="flex items-center justify-center space-x-2 space-x-reverse">
                    <UserPlus className="h-5 w-5" />
                    <span>إنشاء حساب</span>
                  </div>
                )}
              </Button>
            </form>

            <div className="mt-6 text-center">
              <p className="text-sm text-muted-foreground">
                لديك حساب بالفعل؟{" "}
                <Link 
                  href="/login" 
                  className="font-medium text-primary hover:text-primary/80 transition-colors"
                >
                  تسجيل الدخول
                </Link>
              </p>
            </div>
          </CardContent>
        </Card>

        <div className="mt-6 text-center">
          <p className="text-xs text-muted-foreground">
            © FISCHER 2023. جميع الحقوق محفوظة
          </p>
        </div>
      </div>
    </div>
  )
}