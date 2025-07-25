// front-user/app/deposit/page.tsx
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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Copy, CreditCard, Gift, Wallet, QrCode, AlertTriangle } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { useSearchParams } from "next/navigation"

interface Plan {
  id: number
  name: string
  price: number
  monthlyProfit: number
  dailyProfitMin: number
  dailyProfitMax: number
  planLevel: number
}

interface DepositInfo {
  usdtWalletAddress: string
}

export default function DepositPage() {
  const [plans, setPlans] = useState<Plan[]>([])
  const [depositInfo, setDepositInfo] = useState<DepositInfo | null>(null)
  const [selectedPlanId, setSelectedPlanId] = useState<string>("")
  const [amount, setAmount] = useState("")
  const [promoCode, setPromoCode] = useState("")
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState("")
  const [success, setSuccess] = useState("")
  const { toast } = useToast()
  const searchParams = useSearchParams()

  useEffect(() => {
    fetchData()

    // Check if planId is provided in URL
    const planId = searchParams.get("planId")
    if (planId) {
      setSelectedPlanId(planId)
    }
  }, [searchParams])

  const fetchData = async () => {
    try {
      const [plansData, depositInfoData] = await Promise.all([apiService.getPlans(), apiService.getDepositInfo()])
      setPlans(plansData.plans)
      setDepositInfo(depositInfoData)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const selectedPlan = plans.find((plan) => plan.id.toString() === selectedPlanId)

  useEffect(() => {
    if (selectedPlan) {
      setAmount(selectedPlan.price.toString())
    }
  }, [selectedPlan])

  const copyWalletAddress = () => {
    if (depositInfo?.usdtWalletAddress) {
      navigator.clipboard.writeText(depositInfo.usdtWalletAddress)
      toast({
        title: "تم النسخ!",
        description: "تم نسخ عنوان المحفظة",
      })
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setSuccess("")

    if (!selectedPlanId) {
      setError("يرجى اختيار خطة الاستثمار")
      return
    }

    if (!amount || Number.parseFloat(amount) <= 0) {
      setError("يرجى إدخال مبلغ صحيح")
      return
    }

    if (selectedPlan && Number.parseFloat(amount) < selectedPlan.price) {
      setError(`الحد الأدنى للمبلغ لخطة ${selectedPlan.name} هو $${selectedPlan.price}`)
      return
    }

    setSubmitting(true)

    try {
      const response = await apiService.createDeposit({
        amount: Number.parseFloat(amount),
        planId: Number.parseInt(selectedPlanId),
        promoCode: promoCode || undefined,
      })

      setSuccess(response.message)
      setAmount("")
      setPromoCode("")
      setSelectedPlanId("")
    } catch (err: any) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
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
            <h1 className="text-3xl font-bold text-foreground">إيداع</h1>
            <p className="text-muted-foreground mt-2">قم بإيداع USDT لحسابك</p>
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
            {/* Payment Method */}
            <Card className="fischer-card">
              <CardHeader className="text-center">
                <CardTitle className="text-foreground">طريقة الإيداع</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-center">
                  <div className="inline-flex items-center justify-center px-6 py-3 bg-primary rounded-lg">
                    <span className="text-primary-foreground font-bold">USDT TRC20</span>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* QR Code and Address */}
            <Card className="fischer-card">
              <CardHeader className="text-center">
                <CardTitle className="text-foreground">عنوان الإيداع</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {/* QR Code Placeholder */}
                  <div className="flex justify-center">
                    <div className="w-48 h-48 bg-muted rounded-xl flex items-center justify-center">
                      <QrCode className="h-32 w-32 text-muted-foreground" />
                    </div>
                  </div>

                  {/* Wallet Address */}
                  <div className="space-y-2">
                    <Label className="text-foreground text-center block">عنوان محفظة USDT TRC20</Label>
                    <div className="bg-input border border-border rounded-lg p-4 flex items-center justify-between">
                      <span className="text-sm font-mono text-foreground break-all flex-1">
                        {depositInfo?.usdtWalletAddress}
                      </span>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={copyWalletAddress}
                        className="ml-2 flex-shrink-0 hover:bg-primary/10"
                      >
                        <Copy className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>

                  {/* Warning */}
                  <Alert className="bg-yellow-500/10 border-yellow-500/20">
                    <AlertTriangle className="h-4 w-4 text-yellow-500" />
                    <AlertDescription className="text-yellow-600">
                      <strong>تنبيه هام</strong>
                      <br />
                      تأكد من إرسال USDT على شبكة TRC20 فقط، إرسال العملات على شبكة أخرى قد
                      يؤدي إلى فقدانها نهائياً
                    </AlertDescription>
                  </Alert>
                </div>
              </CardContent>
            </Card>

            {/* Deposit Form */}
            <Card className="fischer-card">
              <CardHeader>
                <CardTitle className="text-foreground text-center">تفاصيل الإيداع</CardTitle>
                <CardDescription className="text-center">اختر خطة الاستثمار وأدخل المبلغ</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="plan" className="text-foreground">اختر خطة الاستثمار</Label>
                    <Select value={selectedPlanId} onValueChange={setSelectedPlanId}>
                      <SelectTrigger className="fischer-input">
                        <SelectValue placeholder="اختر خطة" />
                      </SelectTrigger>
                      <SelectContent className="bg-card border-border">
                        {plans.map((plan) => (
                          <SelectItem key={plan.id} value={plan.id.toString()}>
                            {plan.name} - ${plan.price} (${plan.dailyProfitMin}-${plan.dailyProfitMax}/يوم)
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {selectedPlan && (
                    <div className="p-4 bg-primary/10 rounded-lg border border-primary/20">
                      <h4 className="font-medium text-primary mb-2">{selectedPlan.name} خطة</h4>
                      <div className="space-y-1 text-sm text-primary/80">
                        <p>الحد الأدنى للاستثمار: ${selectedPlan.price}</p>
                        <p>الربح الشهري: ${selectedPlan.monthlyProfit}</p>
                        <p>
                          نطاق الربح اليومي: ${selectedPlan.dailyProfitMin} - ${selectedPlan.dailyProfitMax}
                        </p>
                      </div>
                    </div>
                  )}

                  <div className="space-y-2">
                    <Label htmlFor="amount" className="text-foreground">المبلغ ($)</Label>
                    <Input
                      id="amount"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="أدخل مبلغ الإيداع"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      className="fischer-input"
                      required
                    />
                    {selectedPlan && Number.parseFloat(amount) < selectedPlan.price && (
                      <p className="text-sm text-destructive">الحد الأدنى للمبلغ لهذه الخطة هو ${selectedPlan.price}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="promoCode" className="text-foreground">كود الخصم (اختياري)</Label>
                    <div className="relative">
                      <Gift className="absolute right-3 top-3 h-4 w-4 text-muted-foreground" />
                      <Input
                        id="promoCode"
                        type="text"
                        placeholder="أدخل كود الخصم للحصول على مكافأة"
                        value={promoCode}
                        onChange={(e) => setPromoCode(e.target.value)}
                        className="fischer-input pr-10"
                      />
                    </div>
                  </div>

                  <Button 
                    type="submit" 
                    className="w-full fischer-button-primary h-12"
                    disabled={submitting}
                  >
                    {submitting ? (
                      <div className="flex items-center space-x-2 space-x-reverse">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                        <span>جاري الإرسال...</span>
                      </div>
                    ) : (
                      "إرسال طلب الإيداع"
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Instructions */}
            <Card className="fischer-card">
              <CardHeader>
                <CardTitle className="text-foreground text-center">التعليمات</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <h4 className="font-semibold text-foreground mb-2">خطوات الإيداع</h4>
                      <ol className="space-y-2 text-sm text-muted-foreground">
                        <li>1. اختر خطة الاستثمار المناسبة</li>
                        <li>2. أدخل مبلغ الإيداع</li>
                        <li>3. أرسل طلب الإيداع</li>
                        <li>4. أرسل USDT إلى العنوان المحدد</li>
                        <li>5. انتظر موافقة الإدارة</li>
                      </ol>
                    </div>
                    <div>
                      <h4 className="font-semibold text-foreground mb-2">معلومات مهمة</h4>
                      <ul className="space-y-2 text-sm text-muted-foreground">
                        <li>• شبكة TRC20 فقط مدعومة</li>
                        <li>• تحقق من العنوان قبل الإرسال</li>
                        <li>• العناوين الخاطئة قد تؤدي لفقدان الأموال</li>
                        <li>• احتفظ بمعرف المعاملة للمراجعة</li>
                        <li>• وقت المعالجة: 1-24 ساعة</li>
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