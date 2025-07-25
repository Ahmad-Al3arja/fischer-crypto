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

interface Plan {
  id: number
  name: string
  minAmount: number
  maxAmount: number
  duration: number
  dailyProfit: number
  totalProfit: number
}

interface DepositInfo {
  minDeposit: number
  maxDeposit: number
  processingTime: string
}

export default function DepositPage() {
  const [plans, setPlans] = useState<Plan[]>([])
  const [depositInfo, setDepositInfo] = useState<DepositInfo | null>(null)
  const [selectedPlan, setSelectedPlan] = useState<Plan | null>(null)
  const [amount, setAmount] = useState("")
  const [promoCode, setPromoCode] = useState("")
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState("")
  const { toast } = useToast()

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const [plansData, depositInfoData] = await Promise.all([
        apiService.getPlans(),
        apiService.getDepositInfo()
      ])
      setPlans(plansData)
      setDepositInfo(depositInfoData)
      if (plansData.length > 0) {
        setSelectedPlan(plansData[0])
      }
    } catch (err: any) {
      setError(err.message)
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
      setError(err.message)
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
              <Button variant="outline">Back to Dashboard</Button>
            </Link>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Deposit Form */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <TrendingUp className="h-5 w-5" />
                  <span>New Deposit</span>
                </CardTitle>
                <CardDescription>Choose a plan and enter deposit amount</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="plan">Investment Plan</Label>
                    <Select value={selectedPlan?.id.toString()} onValueChange={handlePlanChange}>
                      <SelectTrigger>
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
                      <Label htmlFor="amount">Amount (USD)</Label>
                      <div className="relative">
                        <DollarSign className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                        <Input
                          id="amount"
                          type="number"
                          placeholder={`${selectedPlan.minAmount} - ${selectedPlan.maxAmount}`}
                          value={amount}
                          onChange={(e) => setAmount(e.target.value)}
                          className="pl-10"
                          required
                        />
                      </div>
                      <p className="text-xs text-muted-foreground">
                        Min: ${selectedPlan.minAmount} | Max: ${selectedPlan.maxAmount}
                      </p>
                    </div>
                  )}

                  <div className="space-y-2">
                    <Label htmlFor="promoCode">Promo Code (Optional)</Label>
                    <Input
                      id="promoCode"
                      type="text"
                      placeholder="Enter promo code"
                      value={promoCode}
                      onChange={(e) => setPromoCode(e.target.value)}
                    />
                  </div>

                  <Button type="submit" className="w-full" disabled={submitting || !selectedPlan}>
                    {submitting ? (
                      <div className="flex items-center space-x-2">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
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
              <Card>
                <CardHeader>
                  <CardTitle>Plan Details</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="text-center p-3 bg-muted rounded-lg">
                      <Calendar className="h-6 w-6 mx-auto mb-2 text-primary" />
                      <p className="text-sm text-muted-foreground">Duration</p>
                      <p className="font-semibold">{selectedPlan.duration} days</p>
                    </div>
                    <div className="text-center p-3 bg-muted rounded-lg">
                      <Percent className="h-6 w-6 mx-auto mb-2 text-primary" />
                      <p className="text-sm text-muted-foreground">Daily Profit</p>
                      <p className="font-semibold">{selectedPlan.dailyProfit}%</p>
                    </div>
                  </div>
                  
                  <div className="text-center p-4 bg-primary/10 rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Total Profit</p>
                    <p className="text-2xl font-bold text-primary">{selectedPlan.totalProfit}%</p>
                  </div>

                  <div className="space-y-2">
                    <h4 className="font-semibold">Plan Features:</h4>
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
            {depositInfo && (
              <Card className="lg:col-span-2">
                <CardHeader>
                  <CardTitle>Deposit Information</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="text-center p-3 bg-muted rounded-lg">
                      <p className="text-sm text-muted-foreground">Minimum Deposit</p>
                      <p className="font-semibold">${depositInfo.minDeposit}</p>
                    </div>
                    <div className="text-center p-3 bg-muted rounded-lg">
                      <p className="text-sm text-muted-foreground">Maximum Deposit</p>
                      <p className="font-semibold">${depositInfo.maxDeposit}</p>
                    </div>
                    <div className="text-center p-3 bg-muted rounded-lg">
                      <p className="text-sm text-muted-foreground">Processing Time</p>
                      <p className="font-semibold">{depositInfo.processingTime}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
} 