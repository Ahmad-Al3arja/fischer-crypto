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
import { Copy, CreditCard, Gift, Wallet, AlertTriangle } from "lucide-react"
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
        title: "Copied!",
        description: "Wallet address copied to clipboard",
      })
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setSuccess("")

    if (!selectedPlanId) {
      setError("Please select a plan")
      return
    }

    if (!amount || Number.parseFloat(amount) <= 0) {
      setError("Please enter a valid amount")
      return
    }

    if (selectedPlan && Number.parseFloat(amount) < selectedPlan.price) {
      setError(`Minimum amount for ${selectedPlan.name} plan is $${selectedPlan.price}`)
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
        <div className="min-h-screen flex items-center justify-center bg-background">
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
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-foreground">إيداع</h1>
            <p className="text-muted-foreground">قم بإيداع USDT لحسابك</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {success && (
            <Alert className="mb-6 border-green-200 bg-green-900/20">
              <AlertDescription className="text-green-400">{success}</AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Deposit Form */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <CreditCard className="h-5 w-5" />
                  <span>Deposit Details</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">Select a plan and enter deposit amount</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="plan" className="text-foreground">Select Investment Plan</Label>
                    <Select value={selectedPlanId} onValueChange={setSelectedPlanId}>
                      <SelectTrigger className="bg-input border-border text-foreground">
                        <SelectValue placeholder="Choose a plan" />
                      </SelectTrigger>
                      <SelectContent className="bg-card border-border">
                        {plans.map((plan) => (
                          <SelectItem key={plan.id} value={plan.id.toString()} className="text-foreground">
                            {plan.name} - ${plan.price} (${plan.dailyProfitMin}-${plan.dailyProfitMax}/day)
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {selectedPlan && (
                    <div className="p-4 bg-primary/10 rounded-lg border border-primary/20">
                      <h4 className="font-medium text-primary">{selectedPlan.name} Plan</h4>
                      <div className="mt-2 space-y-1 text-sm text-muted-foreground">
                        <p>Minimum Investment: ${selectedPlan.price}</p>
                        <p>Monthly Profit: ${selectedPlan.monthlyProfit}</p>
                        <p>
                          Daily Profit Range: ${selectedPlan.dailyProfitMin} - ${selectedPlan.dailyProfitMax}
                        </p>
                      </div>
                    </div>
                  )}

                  <div className="space-y-2">
                    <Label htmlFor="amount" className="text-foreground">Amount ($)</Label>
                    <Input
                      id="amount"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="Enter deposit amount"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      className="bg-input border-border text-foreground placeholder:text-muted-foreground focus-ring"
                      required
                    />
                    {selectedPlan && Number.parseFloat(amount) < selectedPlan.price && (
                      <p className="text-sm text-red-400">Minimum amount for this plan is ${selectedPlan.price}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="promoCode" className="text-foreground">Promo Code (Optional)</Label>
                    <div className="relative">
                      <Gift className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                      <Input
                        id="promoCode"
                        type="text"
                        placeholder="Enter promo code for bonus"
                        value={promoCode}
                        onChange={(e) => setPromoCode(e.target.value)}
                        className="pl-10 bg-input border-border text-foreground placeholder:text-muted-foreground focus-ring"
                      />
                    </div>
                  </div>

                  <Button type="submit" className="w-full bg-primary hover:bg-primary/90 text-primary-foreground btn-animate" disabled={submitting}>
                    {submitting ? (
                      <div className="flex items-center space-x-2">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                        <span>Processing...</span>
                      </div>
                    ) : (
                      "Submit Deposit Request"
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Payment Information */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <Wallet className="h-5 w-5" />
                  <span>Payment Information</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">Send your deposit to this USDT TRC20 address</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div>
                    <Label className="text-foreground">USDT TRC20 Wallet Address</Label>
                    <div className="mt-2 p-4 bg-input rounded-lg border border-border">
                      <div className="flex items-center justify-between">
                        <p className="text-sm font-mono break-all text-foreground">{depositInfo?.usdtWalletAddress}</p>
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={copyWalletAddress}
                          className="ml-2 flex-shrink-0 bg-card border-border hover:bg-accent"
                        >
                          <Copy className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </div>

                  <Alert className="bg-card border-border">
                    <AlertDescription className="text-foreground">
                      <strong>Important Instructions:</strong>
                      <ul className="mt-2 space-y-1 text-sm text-muted-foreground">
                        <li>• Only send USDT on TRC20 network</li>
                        <li>• Your deposit will be reviewed by admin</li>
                        <li>• Processing time: 1-24 hours</li>
                        <li>• Keep your transaction hash for reference</li>
                      </ul>
                    </AlertDescription>
                  </Alert>

                  <div className="p-4 bg-yellow-900/20 rounded-lg border border-yellow-500/20">
                    <h4 className="font-medium text-yellow-400 flex items-center gap-2">
                      <AlertTriangle className="h-4 w-4" />
                      Next Steps:
                    </h4>
                    <ol className="mt-2 space-y-1 text-sm text-muted-foreground">
                      <li>1. Submit the deposit form above</li>
                      <li>2. Send USDT to the wallet address</li>
                      <li>3. Wait for admin approval</li>
                      <li>4. Your balance will be updated automatically</li>
                    </ol>
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
