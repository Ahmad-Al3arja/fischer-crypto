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
import { Copy, CreditCard, Gift, Wallet } from "lucide-react"
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
        <div className="min-h-screen flex items-center justify-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
        </div>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-gray-50">
        <Navbar />

        <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">💳 Make Deposit</h1>
            <p className="text-gray-600">Fund your investment account</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {success && (
            <Alert className="mb-6 border-green-200 bg-green-50">
              <AlertDescription className="text-green-800">{success}</AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Deposit Form */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <CreditCard className="h-5 w-5" />
                  <span>Deposit Details</span>
                </CardTitle>
                <CardDescription>Select a plan and enter deposit amount</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="plan">Select Investment Plan</Label>
                    <Select value={selectedPlanId} onValueChange={setSelectedPlanId}>
                      <SelectTrigger>
                        <SelectValue placeholder="Choose a plan" />
                      </SelectTrigger>
                      <SelectContent>
                        {plans.map((plan) => (
                          <SelectItem key={plan.id} value={plan.id.toString()}>
                            {plan.name} - ${plan.price} (${plan.dailyProfitMin}-${plan.dailyProfitMax}/day)
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {selectedPlan && (
                    <div className="p-4 bg-blue-50 rounded-lg">
                      <h4 className="font-medium text-blue-900">{selectedPlan.name} Plan</h4>
                      <div className="mt-2 space-y-1 text-sm text-blue-700">
                        <p>Minimum Investment: ${selectedPlan.price}</p>
                        <p>Monthly Profit: ${selectedPlan.monthlyProfit}</p>
                        <p>
                          Daily Profit Range: ${selectedPlan.dailyProfitMin} - ${selectedPlan.dailyProfitMax}
                        </p>
                      </div>
                    </div>
                  )}

                  <div className="space-y-2">
                    <Label htmlFor="amount">Amount ($)</Label>
                    <Input
                      id="amount"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="Enter deposit amount"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      required
                    />
                    {selectedPlan && Number.parseFloat(amount) < selectedPlan.price && (
                      <p className="text-sm text-red-600">Minimum amount for this plan is ${selectedPlan.price}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="promoCode">Promo Code (Optional)</Label>
                    <div className="relative">
                      <Gift className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                      <Input
                        id="promoCode"
                        type="text"
                        placeholder="Enter promo code for bonus"
                        value={promoCode}
                        onChange={(e) => setPromoCode(e.target.value)}
                        className="pl-10"
                      />
                    </div>
                  </div>

                  <Button type="submit" className="w-full" disabled={submitting}>
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
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Wallet className="h-5 w-5" />
                  <span>Payment Information</span>
                </CardTitle>
                <CardDescription>Send your deposit to this USDT TRC20 address</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div>
                    <Label>USDT TRC20 Wallet Address</Label>
                    <div className="mt-2 p-4 bg-gray-50 rounded-lg border">
                      <div className="flex items-center justify-between">
                        <p className="text-sm font-mono break-all">{depositInfo?.usdtWalletAddress}</p>
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={copyWalletAddress}
                          className="ml-2 flex-shrink-0 bg-transparent"
                        >
                          <Copy className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </div>

                  <Alert>
                    <AlertDescription>
                      <strong>Important Instructions:</strong>
                      <ul className="mt-2 space-y-1 text-sm">
                        <li>• Only send USDT on TRC20 network</li>
                        <li>• Your deposit will be reviewed by admin</li>
                        <li>• Processing time: 1-24 hours</li>
                        <li>• Keep your transaction hash for reference</li>
                      </ul>
                    </AlertDescription>
                  </Alert>

                  <div className="p-4 bg-yellow-50 rounded-lg border border-yellow-200">
                    <h4 className="font-medium text-yellow-800">Next Steps:</h4>
                    <ol className="mt-2 space-y-1 text-sm text-yellow-700">
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
