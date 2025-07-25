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
import { TrendingUp, DollarSign, Calendar, Percent, Copy, Clock } from "lucide-react"
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
      description: "Admin TRC20 address copied to clipboard",
    })
  }

  if (loading) {
    return (
      <ProtectedRoute>
        <div className="min-h-screen bg-black flex items-center justify-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-400"></div>
        </div>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-black">
        <Navbar />

        <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-3xl font-bold text-foreground">Deposit</h1>
            <Link href="/dashboard">
              <Button variant="outline" className="bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 hover:from-gray-700 hover:to-gray-800 text-white">
                Back to Dashboard
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

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Deposit Form */}
            <Card className="bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 shadow-lg">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <TrendingUp className="h-5 w-5" />
                  <span>{t('new_deposit')}</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">{t('choose_plan_deposit')}</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="plan" className="text-foreground">{t('investment_plan')}</Label>
                    <Select value={selectedPlan?.id.toString()} onValueChange={handlePlanChange}>
                      <SelectTrigger className="bg-gradient-to-r from-gray-800 to-gray-900 border-gray-700 text-white">
                        <SelectValue placeholder={t('select_plan')} />
                      </SelectTrigger>
                      <SelectContent>
                        {plans.map((plan) => (
                          <SelectItem key={plan.id} value={plan.id.toString()}>
                            {plan.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {selectedPlan && (
                    <div className="space-y-2">
                      <Label htmlFor="amount" className="text-foreground">{t('amount_usd')}</Label>
                      <div className="relative">
                        <DollarSign className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                        <Input
                          id="amount"
                          type="number"
                          placeholder={`${selectedPlan.minAmount} - ${selectedPlan.maxAmount}`}
                          value={amount}
                          onChange={(e) => setAmount(e.target.value)}
                          className="pl-10 bg-gradient-to-r from-gray-800 to-gray-900 border-gray-700 text-white placeholder:text-gray-400"
                          required
                        />
                      </div>
                      <p className="text-xs text-muted-foreground">
                        {t('min_max')}
                      </p>
                    </div>
                  )}

                  <div className="space-y-2">
                    <Label htmlFor="promoCode" className="text-foreground">{t('promo_code')}</Label>
                    <Input
                      id="promoCode"
                      type="text"
                      placeholder={t('enter_promo_code')}
                      value={promoCode}
                      onChange={(e) => setPromoCode(e.target.value)}
                      className="bg-gradient-to-r from-gray-800 to-gray-900 border-gray-700 text-white placeholder:text-gray-400"
                    />
                  </div>

                  <Button 
                    type="submit" 
                    disabled={submitting}
                    className="w-full bg-gradient-to-r from-gray-700 to-gray-800 hover:from-gray-600 hover:to-gray-700 text-white"
                  >
                    {submitting ? (
                      <div className="flex items-center space-x-2">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                        <span>{t('processing')}</span>
                      </div>
                    ) : (
                      t('submit_deposit')
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Admin TRC20 Address */}
            <Card className="lg:col-span-2 bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 shadow-lg">
              <CardHeader>
                <CardTitle className="text-white flex items-center space-x-2">
                  <Copy className="h-5 w-5 text-green-400" />
                  <span>Admin TRC20 Address for Deposits</span>
                </CardTitle>
                <CardDescription className="text-gray-300">
                  Send your deposit to this USDT (TRC20) address
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="p-4 bg-gradient-to-r from-gray-700 to-gray-800 border border-gray-600 rounded-lg">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <p className="text-sm text-gray-400 mb-2">USDT (TRC20) Address:</p>
                        <p className="font-mono text-lg text-white break-all">TSGA528EkEJTwNctQnRWUvQ9urJxzPPZmy</p>
                      </div>
                      <Button
                        onClick={copyAdminAddress}
                        variant="outline"
                        size="sm"
                        className="ml-4 bg-gradient-to-r from-gray-600 to-gray-700 border-gray-500 hover:from-gray-500 hover:to-gray-600 text-white"
                      >
                        <Copy className="h-4 w-4 mr-2" />
                        Copy
                      </Button>
                    </div>
                  </div>
                  
                  <div className="p-4 bg-gradient-to-r from-gray-800 to-gray-900 border border-gray-700 rounded-lg">
                    <h4 className="font-semibold text-white mb-2">Important Notes:</h4>
                    <ul className="text-sm text-gray-300 space-y-1">
                      <li>• Only send USDT (TRC20) to this address</li>
                      <li>• Double-check the address before sending</li>
                      <li>• Deposits are processed automatically</li>
                      <li>• Contact support if you send to wrong address</li>
                    </ul>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Plan Details */}
            {selectedPlan && (
              <Card className="bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 shadow-lg">
                <CardHeader>
                  <CardTitle className="text-white">Plan Details</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between p-3 bg-gradient-to-r from-gray-700 to-gray-800 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <div className="p-2 bg-gradient-to-r from-green-600 to-green-700 rounded-lg">
                          <Percent className="h-4 w-4 text-white" />
                        </div>
                        <span className="text-gray-300">Daily Profit</span>
                      </div>
                      <span className="text-green-400 font-semibold">1.50%</span>
                    </div>
                    
                    <div className="flex items-center justify-between p-3 bg-gradient-to-r from-gray-700 to-gray-800 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <div className="p-2 bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg">
                          <Calendar className="h-4 w-4 text-white" />
                        </div>
                        <span className="text-gray-300">Duration</span>
                      </div>
                      <span className="text-blue-400 font-semibold">30 days</span>
                    </div>
                    
                    <div className="p-4 bg-gradient-to-r from-gray-700/20 to-gray-800/20 border border-gray-600 rounded-lg">
                      <div className="text-center">
                        <p className="text-gray-300 text-sm mb-1">Monthly Profit</p>
                        <p className="text-green-400 text-2xl font-bold">45%</p>
                      </div>
                    </div>
                    
                    <div>
                      <h4 className="text-white font-semibold mb-2">Plan Features:</h4>
                      <ul className="text-sm text-gray-300 space-y-1">
                        <li>• Daily profit distribution</li>
                        <li>• 24/7 support</li>
                        <li>• Secure transactions</li>
                        <li>• Instant activation</li>
                      </ul>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Deposit Info */}
            <Card className="lg:col-span-2 bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 shadow-lg">
              <CardHeader>
                <CardTitle className="text-white">Deposit Information</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="text-center p-4 bg-gradient-to-r from-gray-700 to-gray-800 rounded-lg border border-gray-600">
                    <div className="p-2 bg-gradient-to-r from-green-600 to-green-700 rounded-lg w-fit mx-auto mb-2">
                      <DollarSign className="h-4 w-4 text-white" />
                    </div>
                    <p className="text-sm text-gray-300 mb-1">Minimum Deposit</p>
                    <p className="text-green-400 font-semibold">$60</p>
                  </div>
                  
                  <div className="text-center p-4 bg-gradient-to-r from-gray-700 to-gray-800 rounded-lg border border-gray-600">
                    <div className="p-2 bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg w-fit mx-auto mb-2">
                      <TrendingUp className="h-4 w-4 text-white" />
                    </div>
                    <p className="text-sm text-gray-300 mb-1">Maximum Deposit</p>
                    <p className="text-blue-400 font-semibold">$100,000</p>
                  </div>
                  
                  <div className="text-center p-4 bg-gradient-to-r from-gray-700 to-gray-800 rounded-lg border border-gray-600">
                    <div className="p-2 bg-gradient-to-r from-purple-600 to-purple-700 rounded-lg w-fit mx-auto mb-2">
                      <Clock className="h-4 w-4 text-white" />
                    </div>
                    <p className="text-sm text-gray-300 mb-1">Processing Time</p>
                    <p className="text-purple-400 font-semibold">Instant</p>
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