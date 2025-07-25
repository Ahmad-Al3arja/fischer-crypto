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
import { TrendingUp, DollarSign, Calendar, Percent } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { FrontendPlan, DepositInfo, convertBackendPlanToFrontend } from "@/types"

// Default data in case API fails
const defaultPlans: FrontendPlan[] = [
  {
    id: 1,
    name: "Starter Plan",
    description: "Perfect for beginners",
    minAmount: 100,
    maxAmount: 1000,
    duration: 30,
    dailyProfit: 2.5,
    totalProfit: 75,
    features: []
  },
  {
    id: 2,
    name: "Premium Plan",
    description: "Most popular choice",
    minAmount: 1000,
    maxAmount: 10000,
    duration: 60,
    dailyProfit: 3.0,
    totalProfit: 180,
    features: []
  },
  {
    id: 3,
    name: "VIP Plan",
    description: "For serious investors",
    minAmount: 10000,
    maxAmount: 100000,
    duration: 90,
    dailyProfit: 3.5,
    totalProfit: 315,
    features: []
  }
]

const defaultDepositInfo: DepositInfo = {
  minDeposit: 100,
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

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      setApiError("")
      const [plansData, depositInfoData] = await Promise.all([
        apiService.getPlans().catch(err => {
          console.error("Error fetching plans:", err)
          return null
        }),
        apiService.getDepositInfo().catch(err => {
          console.error("Error fetching deposit info:", err)
          return defaultDepositInfo
        })
      ])
      
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
      
      // Ensure we have valid deposit info
      const validDepositInfo = {
        minDeposit: depositInfoData?.minDeposit || 100,
        maxDeposit: depositInfoData?.maxDeposit || 100000,
        processingTime: depositInfoData?.processingTime || "Instant"
      }
      setDepositInfo(validDepositInfo)
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
      <div className="min-h-screen bg-background">
        <Navbar />

        <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-3xl font-bold text-foreground">Deposit</h1>
            <Link href="/dashboard">
              <Button variant="outline" className="bg-card border-border hover:bg-muted">
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
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <TrendingUp className="h-5 w-5" />
                  <span>New Deposit</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">Choose a plan and enter deposit amount</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="plan" className="text-foreground">Investment Plan</Label>
                    <Select value={selectedPlan?.id.toString()} onValueChange={handlePlanChange}>
                      <SelectTrigger className="bg-muted border-border text-foreground">
                        <SelectValue placeholder="Select a plan" />
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
                      <Label htmlFor="amount" className="text-foreground">Amount (USD)</Label>
                      <div className="relative">
                        <DollarSign className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                        <Input
                          id="amount"
                          type="number"
                          placeholder={`${selectedPlan.minAmount} - ${selectedPlan.maxAmount}`}
                          value={amount}
                          onChange={(e) => setAmount(e.target.value)}
                          className="pl-10 bg-muted border-border text-foreground placeholder:text-muted-foreground"
                          required
                        />
                      </div>
                      <p className="text-xs text-muted-foreground">
                        Min: ${selectedPlan.minAmount} | Max: ${selectedPlan.maxAmount}
                      </p>
                    </div>
                  )}

                  <div className="space-y-2">
                    <Label htmlFor="promoCode" className="text-foreground">Promo Code (Optional)</Label>
                    <Input
                      id="promoCode"
                      type="text"
                      placeholder="Enter promo code"
                      value={promoCode}
                      onChange={(e) => setPromoCode(e.target.value)}
                      className="bg-muted border-border text-foreground placeholder:text-muted-foreground"
                    />
                  </div>

                  <Button 
                    type="submit" 
                    className="w-full bg-primary hover:bg-primary/90 text-primary-foreground" 
                    disabled={submitting || !selectedPlan}
                  >
                    {submitting ? (
                      <div className="flex items-center space-x-2">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                        <span>Processing...</span>
                      </div>
                    ) : (
                      "Submit Deposit"
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Plan Details */}
            {selectedPlan && (
              <Card className="bg-card border-border">
                <CardHeader>
                  <CardTitle className="text-foreground">Plan Details</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="text-center p-3 bg-muted rounded-lg">
                      <Calendar className="h-6 w-6 mx-auto mb-2 text-primary" />
                      <p className="text-sm text-muted-foreground">Duration</p>
                      <p className="font-semibold text-foreground">{selectedPlan.duration} days</p>
                    </div>
                    <div className="text-center p-3 bg-muted rounded-lg">
                      <Percent className="h-6 w-6 mx-auto mb-2 text-primary" />
                      <p className="text-sm text-muted-foreground">Daily Profit</p>
                      <p className="font-semibold text-foreground">{selectedPlan.dailyProfit.toFixed(2)}%</p>
                    </div>
                  </div>
                  
                  <div className="text-center p-4 bg-primary/10 rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Total Profit</p>
                    <p className="text-2xl font-bold text-primary">{selectedPlan.totalProfit.toFixed(0)}%</p>
                  </div>

                  <div className="space-y-2">
                    <h4 className="font-semibold text-foreground">Plan Features:</h4>
                    <ul className="text-sm text-muted-foreground space-y-1">
                      <li>• Daily profit distribution</li>
                      <li>• 24/7 support</li>
                      <li>• Secure transactions</li>
                      <li>• Instant activation</li>
                    </ul>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Deposit Info */}
            <Card className="lg:col-span-2 bg-card border-border">
              <CardHeader>
                <CardTitle className="text-foreground">Deposit Information</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground">Minimum Deposit</p>
                    <p className="font-semibold text-foreground">${depositInfo.minDeposit}</p>
                  </div>
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground">Maximum Deposit</p>
                    <p className="font-semibold text-foreground">${depositInfo.maxDeposit}</p>
                  </div>
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground">Processing Time</p>
                    <p className="font-semibold text-foreground">{depositInfo.processingTime}</p>
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