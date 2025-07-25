// front-user/app/withdraw/page.tsx
"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Banknote, Wallet, AlertTriangle, DollarSign } from "lucide-react"
import Link from "next/link"

interface BalanceData {
  totalBalance: number
  frozenBalance: number
  withdrawableBalance: number
  referralEarnings: number
}

interface WalletData {
  usdtAddress: string
  isLocked: boolean
}

export default function WithdrawPage() {
  const [balance, setBalance] = useState<BalanceData | null>(null)
  const [wallet, setWallet] = useState<WalletData | null>(null)
  const [amount, setAmount] = useState("")
  const [walletAddress, setWalletAddress] = useState("")
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState("")
  const [success, setSuccess] = useState("")

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const [balanceData, walletData] = await Promise.all([
        apiService.getBalance(),
        apiService
          .getWallet()
          .catch(() => null), // Wallet might not exist
      ])
      setBalance(balanceData)
      setWallet(walletData)
      if (walletData?.usdtAddress) {
        setWalletAddress(walletData.usdtAddress)
      }
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setSuccess("")

    if (!amount || Number.parseFloat(amount) <= 0) {
      setError("يرجى إدخال مبلغ صحيح")
      return
    }

    if (Number.parseFloat(amount) < 10) {
      setError("الحد الأدنى للسحب هو $10")
      return
    }

    if (balance && Number.parseFloat(amount) > balance.withdrawableBalance) {
      setError(`الرصيد غير كافي. المتاح: $${balance.withdrawableBalance.toFixed(2)}`)
      return
    }

    if (!walletAddress) {
      setError("يرجى إدخال عنوان محفظة USDT TRC20")
      return
    }

    // Basic TRC20 address validation
    if (!walletAddress.startsWith("T") || walletAddress.length !== 34) {
      setError("تنسيق عنوان محفظة USDT TRC20 غير صحيح")
      return
    }

    setSubmitting(true)

    try {
      const response = await apiService.createWithdrawal({
        amount: Number.parseFloat(amount),
        walletAddress: walletAddress,
      })

      setSuccess("تم إرسال طلب السحب بنجاح! سيتم مراجعته من قبل الإدارة.")
      setAmount("")

      // Refresh balance
      await fetchData()
    } catch (err: any) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const calculateFee = (amount: number) => {
    return amount * 0.02 // 2% fee
  }

  const calculateNetAmount = (amount: number) => {
    return amount - calculateFee(amount)
  }

  if (loading) {
    return (
      <ProtectedRoute>
        <div className="min-h-screen bg-background flex items-center justify-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary"></div>
        </div>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-background fischer-gradient-bg">
        <Navbar />

        <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-foreground">سحب الأموال</h1>
            <p className="text-muted-foreground mt-2">اسحب أرباحك المتاحة</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6 bg-destructive/10 border-destructive/20">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {success && (
            <Alert className="mb-6 bg-primary/10 border-primary/20">
              <AlertDescription className="text-primary">{success}</AlertDescription>
            </Alert>
          )}

          <div className="space-y-6">
            {/* Balance Overview */}
            <Card className="fischer-card">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 space-x-reverse">
                  <Banknote className="h-5 w-5" />
                  <span>الرصيد المتاح</span>
                </CardTitle>
                <CardDescription>أرباحك الحالية القابلة للسحب</CardDescription>
              </CardHeader>
              <CardContent>
                {balance && (
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div className="fischer-balance-card">
                        <div className="text-center">
                          <p className="text-sm text-muted-foreground">إجمالي الرصيد</p>
                          <p className="text-2xl font-bold text-foreground">${balance.totalBalance.toFixed(2)}</p>
                        </div>
                      </div>
                      <div className="fischer-balance-card">
                        <div className="text-center">
                          <p className="text-sm text-muted-foreground">الرصيد المجمد</p>
                          <p className="text-2xl font-bold text-muted-foreground">${balance.frozenBalance.toFixed(2)}</p>
                        </div>
                      </div>
                    </div>

                    <div className="fischer-balance-card bg-gradient-to-r from-primary/10 to-primary/20 border-primary/30">
                      <div className="text-center">
                        <p className="text-sm text-primary">متاح للسحب</p>
                        <p className="text-3xl font-bold text-primary">${balance.withdrawableBalance.toFixed(2)}</p>
                      </div>
                    </div>

                    <div className="fischer-balance-card">
                      <div className="text-center">
                        <p className="text-sm text-muted-foreground">أرباح الإحالات</p>
                        <p className="text-2xl font-bold text-foreground">${balance.referralEarnings.toFixed(2)}</p>
                      </div>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Withdrawal Form */}
            <Card className="fischer-card">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 space-x-reverse">
                  <Wallet className="h-5 w-5" />
                  <span>طلب السحب</span>
                </CardTitle>
                <CardDescription>أدخل تفاصيل السحب</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="amount" className="text-foreground">مبلغ السحب ($)</Label>
                    <Input
                      id="amount"
                      type="number"
                      step="0.01"
                      min="10"
                      max={balance?.withdrawableBalance || 0}
                      placeholder="أدخل المبلغ (الحد الأدنى $10)"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      className="fischer-input"
                      required
                    />
                    {amount && Number.parseFloat(amount) >= 10 && (
                      <div className="p-3 bg-secondary rounded-lg space-y-1 text-sm">
                        <div className="flex justify-between">
                          <span className="text-muted-foreground">المبلغ:</span>
                          <span className="text-foreground">${Number.parseFloat(amount).toFixed(2)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-muted-foreground">الرسوم (2%):</span>
                          <span className="text-destructive">-${calculateFee(Number.parseFloat(amount)).toFixed(2)}</span>
                        </div>
                        <div className="flex justify-between font-medium border-t border-border pt-1">
                          <span className="text-foreground">صافي المبلغ:</span>
                          <span className="text-primary">${calculateNetAmount(Number.parseFloat(amount)).toFixed(2)}</span>
                        </div>
                      </div>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="walletAddress" className="text-foreground">عنوان المحفظة USDT TRC20</Label>
                    <Input
                      id="walletAddress"
                      type="text"
                      placeholder="أدخل عنوان محفظة USDT TRC20"
                      value={walletAddress}
                      onChange={(e) => setWalletAddress(e.target.value)}
                      className="fischer-input font-mono text-sm"
                      required
                      dir="ltr"
                    />
                    {wallet?.isLocked && (
                      <p className="text-sm text-primary">✅ استخدام عنوان المحفظة المحفوظ</p>
                    )}
                    {!wallet && (
                      <p className="text-sm text-muted-foreground">
                        <Link href="/wallet" className="text-primary hover:text-primary/80 underline">
                          احفظ عنوان محفظتك
                        </Link>{" "}
                        لعمليات سحب أسرع
                      </p>
                    )}
                  </div>

                  <Alert className="bg-yellow-500/10 border-yellow-500/20">
                    <AlertTriangle className="h-4 w-4 text-yellow-500" />
                    <AlertDescription className="text-yellow-600">
                      <strong>معلومات مهمة:</strong>
                      <ul className="mt-1 space-y-1 text-sm">
                        <li>• الحد الأدنى للسحب: $10</li>
                        <li>• رسوم السحب: 2%</li>
                        <li>• وقت المعالجة: 1-24 ساعة</li>
                        <li>• عناوين USDT TRC20 فقط مقبولة</li>
                      </ul>
                    </AlertDescription>
                  </Alert>

                  <Button 
                    type="submit" 
                    className="w-full fischer-button-primary h-12"
                    disabled={submitting || !balance?.withdrawableBalance}
                  >
                    {submitting ? (
                      <div className="flex items-center space-x-2 space-x-reverse">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                        <span>جاري المعالجة...</span>
                      </div>
                    ) : (
                      "إرسال طلب السحب"
                    )}
                  </Button>
                </form>

                <div className="mt-4 text-center">
                  <Link href="/withdrawal-history">
                    <Button variant="outline" size="sm" className="fischer-button-secondary">
                      عرض سجل السحوبات
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>

            {/* Withdrawal Instructions */}
            <Card className="fischer-card">
              <CardHeader>
                <CardTitle className="text-foreground text-center">كيفية السحب</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4 text-sm">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <h4 className="font-semibold text-foreground mb-2">خطوات السحب</h4>
                      <ol className="space-y-2 text-muted-foreground">
                        <li>1. أدخل مبلغ السحب المطلوب</li>
                        <li>2. أدخل عنوان محفظة USDT TRC20</li>
                        <li>3. راجع الرسوم والمبلغ الصافي</li>
                        <li>4. أرسل طلب السحب</li>
                        <li>5. انتظر موافقة الإدارة</li>
                      </ol>
                    </div>
                    <div>
                      <h4 className="font-semibold text-foreground mb-2">ملاحظات مهمة</h4>
                      <ul className="space-y-2 text-muted-foreground">
                        <li>• تحقق من عنوان المحفظة جيداً</li>
                        <li>• العنوان الخاطئ قد يؤدي لفقدان الأموال</li>
                        <li>• يتم خصم 2% كرسوم معالجة</li>
                        <li>• السحب متاح 24/7</li>
                        <li>• يمكنك تتبع حالة السحب في السجل</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}