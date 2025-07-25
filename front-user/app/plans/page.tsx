"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { BarChart3, TrendingUp, DollarSign, Calendar } from "lucide-react"
import Link from "next/link"

interface Plan {
  id: number
  name: string
  price: number
  monthlyProfit: number
  dailyProfitMin: number
  dailyProfitMax: number
  planLevel: number
}

interface PlansData {
  plans: Plan[]
}

export default function PlansPage() {
  const [plansData, setPlansData] = useState<PlansData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    fetchPlans()
  }, [])

  const fetchPlans = async () => {
    try {
      const data = await apiService.getPlans()
      setPlansData(data)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const getPlanColor = (level: number) => {
    const colors = [
      "border-gray-200 bg-gray-50",
      "border-blue-200 bg-blue-50",
      "border-green-200 bg-green-50",
      "border-purple-200 bg-purple-50",
      "border-yellow-200 bg-yellow-50",
    ]
    return colors[level - 1] || colors[0]
  }

  const getPlanIcon = (level: number) => {
    const icons = ["🥉", "🥈", "🥇", "💎", "👑"]
    return icons[level - 1] || "📊"
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

        <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="mb-8 text-center">
            <h1 className="text-3xl font-bold text-gray-900">📊 Investment Plans</h1>
            <p className="text-gray-600 mt-2">Choose the perfect investment plan for your goals</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {plansData && (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                {plansData.plans
                  .sort((a, b) => a.planLevel - b.planLevel)
                  .map((plan) => (
                    <Card
                      key={plan.id}
                      className={`relative overflow-hidden transition-all hover:shadow-lg ${getPlanColor(plan.planLevel)}`}
                    >
                      <CardHeader className="text-center">
                        <div className="text-4xl mb-2">{getPlanIcon(plan.planLevel)}</div>
                        <CardTitle className="text-2xl font-bold">{plan.name}</CardTitle>
                        <CardDescription>Level {plan.planLevel} Investment Plan</CardDescription>
                        <div className="mt-4">
                          <div className="text-3xl font-bold text-gray-900">${plan.price}</div>
                          <p className="text-sm text-gray-600">Minimum Investment</p>
                        </div>
                      </CardHeader>

                      <CardContent className="space-y-4">
                        <div className="space-y-3">
                          <div className="flex items-center justify-between p-3 bg-white rounded-lg">
                            <div className="flex items-center space-x-2">
                              <Calendar className="h-4 w-4 text-blue-600" />
                              <span className="text-sm font-medium">Monthly Profit</span>
                            </div>
                            <Badge variant="secondary" className="bg-blue-100 text-blue-800">
                              ${plan.monthlyProfit}
                            </Badge>
                          </div>

                          <div className="flex items-center justify-between p-3 bg-white rounded-lg">
                            <div className="flex items-center space-x-2">
                              <TrendingUp className="h-4 w-4 text-green-600" />
                              <span className="text-sm font-medium">Daily Profit Range</span>
                            </div>
                            <Badge variant="secondary" className="bg-green-100 text-green-800">
                              ${plan.dailyProfitMin} - ${plan.dailyProfitMax}
                            </Badge>
                          </div>

                          <div className="p-3 bg-white rounded-lg">
                            <div className="flex items-center space-x-2 mb-2">
                              <BarChart3 className="h-4 w-4 text-purple-600" />
                              <span className="text-sm font-medium">Key Features</span>
                            </div>
                            <ul className="text-sm text-gray-600 space-y-1">
                              <li>• Daily profit generation</li>
                              <li>• 30-day investment cycle</li>
                              <li>• Progressive profit increase</li>
                              <li>• Referral commission eligible</li>
                            </ul>
                          </div>
                        </div>

                        <div className="pt-4">
                          <Link href={`/deposit?planId=${plan.id}`}>
                            <Button className="w-full" size="lg">
                              <DollarSign className="h-4 w-4 mr-2" />
                              Choose This Plan
                            </Button>
                          </Link>
                        </div>
                      </CardContent>

                      {plan.planLevel === 5 && (
                        <div className="absolute top-0 right-0 bg-gradient-to-l from-yellow-400 to-yellow-600 text-white px-3 py-1 text-xs font-bold">
                          PREMIUM
                        </div>
                      )}
                    </Card>
                  ))}
              </div>

              <Card className="bg-blue-50 border-blue-200">
                <CardHeader>
                  <CardTitle className="text-center text-blue-900">💡 How Investment Plans Work</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <h4 className="font-semibold text-blue-900 mb-2">Investment Process</h4>
                      <ul className="space-y-2 text-sm text-blue-800">
                        <li>1. Choose your preferred investment plan</li>
                        <li>2. Make a deposit (minimum plan amount)</li>
                        <li>3. Wait for admin approval</li>
                        <li>4. Start earning daily profits</li>
                        <li>5. Activate daily counter to claim profits</li>
                      </ul>
                    </div>
                    <div>
                      <h4 className="font-semibold text-blue-900 mb-2">Profit System</h4>
                      <ul className="space-y-2 text-sm text-blue-800">
                        <li>• Daily profits increase progressively</li>
                        <li>• 30-day investment cycle per plan</li>
                        <li>• Automatic plan upgrades available</li>
                        <li>• Withdraw profits anytime</li>
                        <li>• Earn referral commissions</li>
                      </ul>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}
