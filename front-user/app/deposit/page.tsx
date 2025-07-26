"use client"

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
import { TrendingUp, DollarSign, Calendar, Percent, Copy, Clock, QrCode } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { FrontendPlan, DepositInfo, convertBackendPlanToFrontend } from "@/types"
import { useLanguage } from "@/contexts/LanguageContext"


// Default data in case API fails
const defaultPlans: FrontendPlan[] = [
  {
    id: 1,
    name: "المستوى الأول",
    description: "Level 1 Investment Plan",
    minAmount: 60,
    maxAmount: 600,
    duration: 30,
    dailyProfit: 1.5,
    totalProfit: 45,
    features: []
  },
  {
    id: 2,
    name: "المستوى الثاني",
    description: "Level 2 Investment Plan",
    minAmount: 150,
    maxAmount: 1500,
    duration: 30,
    dailyProfit: 3.65,
    totalProfit: 110,
    features: []
  },
  {
    id: 3,
    name: "المستوى الثالث",
    description: "Level 3 Investment Plan",
    minAmount: 300,
    maxAmount: 3000,
    duration: 30,
    dailyProfit: 5.9,
    totalProfit: 175,
    features: []
  }
]

const defaultDepositInfo: DepositInfo = {
  minDeposit: 60,
  maxDeposit: 100000,
  processingTime: "Instant"
}

export default function DepositPage() {
  const [plans, setPlans] = useState<FrontendPlan[]>(defaultPlans)
  const [depositInfo, setDepositInfo] = useState<DepositInfo>(defaultDepositInfo)
  const [selectedPlan, setSelectedPlan] = useState<FrontendPlan | null>(defaultPlans[0])
  const [amount, setAmount] = useState("")
  const [promoCode, setPromoCode] = useState("")
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState("")
  const [apiError, setApiError] = useState("")
  const { toast } = useToast()
  const { t } = useLanguage()

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      setApiError("")
      const plansData = await apiService.getPlans().catch(err => {
        console.error("Error fetching plans:", err)
        return null
      })
      
      // Handle plans data with correct API structure
      if (plansData && plansData.plans && Array.isArray(plansData.plans) && plansData.plans.length > 0) {
        // Convert backend plans to frontend format
        const convertedPlans = plansData.plans.map(convertBackendPlanToFrontend)
        setPlans(convertedPlans)
        
        // Set first plan as selected if available
        if (convertedPlans.length > 0) {
          setSelectedPlan(convertedPlans[0])
        }
      } else {
        console.warn("API returned empty or invalid data for plans:", plansData)
        setPlans(defaultPlans)
        setSelectedPlan(defaultPlans[0])
      }
      
      // Use default deposit info since the endpoint doesn't exist
      setDepositInfo(defaultDepositInfo)
    } catch (err: any) {
      console.error("Error in fetchData:", err)
      setApiError("Failed to load data. Please try again later.")
      // Keep default data on error
      setPlans(defaultPlans)
      setDepositInfo(defaultDepositInfo)
      setSelectedPlan(defaultPlans[0])
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedPlan) return

    const numAmount = parseFloat(amount)
    if (isNaN(numAmount) || numAmount < selectedPlan.minAmount || numAmount > selectedPlan.maxAmount) {
      setError(`Amount must be between $${selectedPlan.minAmount} and $${selectedPlan.maxAmount}`)
      return
    }

    setSubmitting(true)
    setError("")

    try {
      await apiService.createDeposit({
        amount: numAmount,
        planId: selectedPlan.id,
        promoCode: promoCode || undefined
      })
      
      toast({
        title: "Success!",
        description: "Deposit request submitted successfully",
      })
      
      // Reset form
      setAmount("")
      setPromoCode("")
    } catch (err: any) {
      console.error("Error creating deposit:", err)
      setError(err.message || "Failed to submit deposit request")
    } finally {
      setSubmitting(false)
    }
  }

  const handlePlanChange = (planId: string) => {
    const plan = plans.find(p => p.id === parseInt(planId))
    setSelectedPlan(plan || null)
    setAmount("")
    setError("")
  }

  const copyAdminAddress = () => {
    navigator.clipboard.writeText("TSGA528EkEJTwNctQnRWUvQ9urJxzPPZmy")
    toast({
      title: t('copied'),
      description: t('admin_address_copied'),
    })
  }

  if (loading) {
    return (
      <ProtectedRoute>
        <div className="min-h-screen bg-black flex items-center justify-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-400">        </div>
      </div>
    </ProtectedRoute>
  )
}

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-gradient-to-br from-black via-gray-900 to-black">
        <Navbar />

        <div className="max-w-5xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 gap-4">
            <h1 className="text-4xl font-extrabold text-foreground tracking-tight drop-shadow-lg">{t('deposit')}</h1>
            <Link href="/dashboard">
              <Button variant="outline" className="bg-gradient-to-br bg-card border-border hover:from-gray-700 hover:to-custom-2e2e2e text-foreground font-semibold shadow-md">
                {t('back_to_dashboard')}
              </Button>
            </Link>
          </div>

          {apiError && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{apiError}</AlertDescription>
            </Alert>
          )}

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Deposit Form */}
            <Card className="col-span-1 bg-gradient-to-br from-gray-900 to-custom-2e2e2e border border-gray-700 shadow-2xl">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground text-2xl font-bold">
                  <TrendingUp className="h-6 w-6 text-green-400" />
                  <span>{t('new_deposit')}</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground text-sm mt-1">{t('choose_plan_deposit')}</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-6">
                  <div className="space-y-2">
                    <Label htmlFor="plan" className="text-foreground font-semibold">{t('investment_plan')}</Label>
                    <Select value={selectedPlan?.id.toString()} onValueChange={handlePlanChange}>
                      <SelectTrigger className="bg-gradient-to-r bg-card border-border text-foreground font-semibold">
                        <SelectValue placeholder={t('select_plan')} />
                      </SelectTrigger>
                      <SelectContent>
                        {plans.map((plan) => (
                          <SelectItem key={plan.id} value={plan.id.toString()} className="font-semibold">
                            {plan.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {selectedPlan && (
                    <div className="space-y-2">
                      <Label htmlFor="amount" className="text-foreground font-semibold">{t('amount_usd')}</Label>
                      <div className="relative">
                        <DollarSign className="absolute left-3 top-3 h-5 w-5 text-green-400" />
                        <Input
                          id="amount"
                          type="number"
                          placeholder={`${selectedPlan.minAmount} - ${selectedPlan.maxAmount}`}
                          value={amount}
                          onChange={(e) => setAmount(e.target.value)}
                          className="pl-12 bg-gradient-to-r bg-card border-border text-foreground placeholder:text-muted-foreground text-lg font-semibold"
                          required
                        />
                      </div>
                      <p className="text-xs text-muted-foreground font-medium">
                        Min: <span className="text-green-400 font-bold">${selectedPlan.minAmount}</span> &nbsp;|&nbsp; Max: <span className="font-bold" style={{ color: '#2E2E2E' }}>${selectedPlan.maxAmount}</span>
                      </p>
                    </div>
                  )}

                  <div className="space-y-2">
                    <Label htmlFor="promoCode" className="text-foreground font-semibold">{t('promo_code')}</Label>
                    <Input
                      id="promoCode"
                      type="text"
                      placeholder={t('enter_promo_code')}
                      value={promoCode}
                      onChange={(e) => setPromoCode(e.target.value)}
                      className="bg-gradient-to-r bg-card border-border text-foreground placeholder:text-muted-foreground"
                    />
                  </div>

                  <Button 
                    type="submit" 
                    disabled={submitting}
                    className="w-full bg-gradient-to-br from-green-600 to-green-700 hover:from-green-500 hover:to-green-600 text-foreground font-bold text-lg py-3 shadow-lg"
                  >
                    {submitting ? (
                      <div className="flex items-center space-x-2">
                        <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-current"></div>
                        <span>{t('processing')}</span>
                      </div>
                    ) : (
                      t('submit_deposit')
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Plan Details */}
            {selectedPlan && (
              <Card className="col-span-1 bg-gradient-to-br from-gray-900 to-custom-2e2e2e border border-gray-700 shadow-2xl">
                <CardHeader>
                  <CardTitle className="text-foreground text-xl font-bold flex items-center gap-2">
                    <Percent className="h-5 w-5 text-green-400" /> {t('plan_details')}
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between p-3 bg-gradient-to-r from-gray-700 to-custom-2e2e2e rounded-lg">
                      <div className="flex items-center space-x-3">
                        <div className="p-2 bg-gradient-to-r from-green-600 to-green-700 rounded-lg">
                          <Percent className="h-4 w-4 text-foreground" />
                        </div>
                        <span className="text-gray-300 font-semibold">{t('daily_profit_label')}</span>
                      </div>
                      <span className="text-green-400 font-bold text-lg">{selectedPlan.dailyProfit}%</span>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-gradient-to-r from-gray-700 to-custom-2e2e2e rounded-lg">
                      <div className="flex items-center space-x-3">
                        <div className="p-2 bg-gradient-to-r from-gray-600 to-gray-700 rounded-lg">
                          <Calendar className="h-4 w-4 text-foreground" />
                        </div>
                        <span className="text-gray-300 font-semibold">{t('duration_label')}</span>
                      </div>
                      <span className="font-bold text-lg" style={{ color: '#2E2E2E' }}>{selectedPlan.duration} {t('days')}</span>
                    </div>
                    <div className="p-4 bg-gradient-to-r from-gray-700/20 to-custom-2e2e2e/20 border border-gray-600 rounded-lg">
                      <div className="text-center">
                        <p className="text-gray-300 text-sm mb-1">{t('monthly_profit_label')}</p>
                        <p className="text-green-400 text-2xl font-extrabold">{selectedPlan.totalProfit}%</p>
                      </div>
                    </div>
                    <div>
                      <h4 className="text-foreground font-semibold mb-2">{t('plan_features_label')}</h4>
                      <ul className="text-sm text-gray-300 space-y-1">
                        <li>• {t('daily_profit_distribution')}</li>
                        <li>• {t('support_24_7')}</li>
                        <li>• {t('secure_transactions')}</li>
                        <li>• {t('instant_activation')}</li>
                      </ul>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Admin TRC20 Address */}
            <Card className="col-span-1 bg-gradient-to-br from-gray-900 to-custom-2e2e2e border border-gray-700 shadow-2xl">
              <CardHeader>
                <CardTitle className="text-foreground flex items-center space-x-2 text-xl font-bold">
                  <Copy className="h-5 w-5 text-green-400" />
                  <span>{t('admin_trc20_address')}</span>
                </CardTitle>
                <CardDescription className="text-gray-300">
                  {t('send_deposit_to_address')}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="p-4 bg-gradient-to-r from-gray-700 to-custom-2e2e2e border border-gray-600 rounded-lg flex items-center justify-between">
                    <div className="flex-1">
                      <p className="text-sm text-muted-foreground mb-2">{t('usdt_trc20_address')}</p>
                      <p className="font-mono text-lg text-foreground break-all select-all">TSGA528EkEJTwNctQnRWUvQ9urJxzPPZmy</p>
                    </div>
                    <Button
                      onClick={copyAdminAddress}
                      variant="outline"
                      size="sm"
                      className="ml-4 bg-gradient-to-r from-green-600 to-green-700 border-green-500 hover:from-green-500 hover:to-green-600 text-foreground font-bold shadow-md"
                    >
                      <Copy className="h-4 w-4 mr-2" />
                      {t('copy')}
                    </Button>
                  </div>
                  <div className="p-4 bg-gradient-to-r from-custom-2e2e2e to-gray-900 border border-gray-700 rounded-lg">
                    <h4 className="font-semibold text-foreground mb-2">{t('important_notes_label')}</h4>
                    <ul className="text-sm text-gray-300 space-y-1">
                      <li>• <span className="text-green-400 font-bold">{t('only_send_usdt_trc20')}</span></li>
                      <li>• {t('double_check_address')}</li>
                      <li>• {t('deposits_processed_automatically')}</li>
                      <li>• <span className="text-red-400 font-bold">{t('contact_support_wrong_address')}</span></li>
                    </ul>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* QR Code Section */}
            <Card className="col-span-1 bg-gradient-to-br from-gray-900 to-custom-2e2e2e border border-gray-700 shadow-2xl">
              <CardHeader>
                <CardTitle className="text-foreground flex items-center space-x-2 text-xl font-bold">
                  <QrCode className="h-5 w-5 text-green-400" />
                  <span>{t('scan_qr_code')}</span>
                </CardTitle>
                <CardDescription className="text-gray-300">
                  {t('scan_qr_description')}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex justify-center">
                    <div className="p-4 bg-white rounded-lg shadow-lg">
                      <img 
                        src="/assets/QR.jpg" 
                        alt={t('usdt_deposit_qr_code')} 
                        className="w-48 h-48 object-contain rounded-lg"
                        onError={(e) => {
                          console.error("Failed to load QR code image");
                          e.currentTarget.style.display = 'none';
                        }}
                      />
                    </div>
                  </div>
                  <div className="text-center">
                    <p className="text-sm text-gray-300 mb-2">
                      <span className="text-green-400 font-bold">{t('how_to_use')}</span>
                    </p>
                    <ul className="text-xs text-muted-foreground space-y-1">
                      <li>• {t('open_usdt_wallet')}</li>
                      <li>• {t('tap_send_transfer')}</li>
                      <li>• {t('scan_qr_code_step')}</li>
                      <li>• {t('enter_deposit_amount')}</li>
                      <li>• {t('confirm_transaction')}</li>
                    </ul>
                  </div>
                  <div className="p-3 bg-gradient-to-r from-gray-600/20 to-gray-700/20 border border-gray-500/30 rounded-lg">
                    <p className="text-xs text-gray-300 text-center">
                      <span className="font-bold">{t('tip_trc20_network')}</span>
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Deposit Info */}
          <div className="mt-10">
            <Card className="bg-gradient-to-br from-gray-900 to-custom-2e2e2e border border-gray-700 shadow-2xl">
              <CardHeader>
                <CardTitle className="text-foreground text-xl font-bold flex items-center gap-2">
                  <DollarSign className="h-5 w-5 text-green-400" /> {t('deposit_information')}
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="text-center p-6 bg-gradient-to-r from-gray-700 to-custom-2e2e2e rounded-lg border border-gray-600">
                    <div className="p-2 bg-gradient-to-r from-green-600 to-green-700 rounded-lg w-fit mx-auto mb-2">
                      <DollarSign className="h-5 w-5 text-foreground" />
                    </div>
                    <p className="text-sm text-gray-300 mb-1">{t('minimum_deposit')}</p>
                    <p className="text-green-400 font-bold text-lg">${depositInfo.minDeposit}</p>
                  </div>
                  <div className="text-center p-6 bg-gradient-to-r from-gray-700 to-custom-2e2e2e rounded-lg border border-gray-600">
                    <div className="p-2 bg-gradient-to-r from-gray-600 to-gray-700 rounded-lg w-fit mx-auto mb-2">
                      <TrendingUp className="h-5 w-5 text-foreground" />
                    </div>
                    <p className="text-sm text-gray-300 mb-1">{t('maximum_deposit')}</p>
                    <p className="font-bold text-lg" style={{ color: '#2E2E2E' }}>${depositInfo.maxDeposit.toLocaleString()}</p>
                  </div>
                  <div className="text-center p-6 bg-gradient-to-r from-gray-700 to-custom-2e2e2e rounded-lg border border-gray-600">
                    <div className="p-2 bg-gradient-to-r from-purple-600 to-purple-700 rounded-lg w-fit mx-auto mb-2">
                      <Clock className="h-5 w-5 text-foreground" />
                    </div>
                    <p className="text-sm text-gray-300 mb-1">{t('processing_time')}</p>
                    <p className="text-purple-400 font-bold text-lg">{t('instant')}</p>
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
