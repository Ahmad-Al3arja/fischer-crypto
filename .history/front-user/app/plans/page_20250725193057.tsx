"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { BarChart3, ArrowLeft, DollarSign, Calendar, Percent, TrendingUp, Check, RefreshCw, AlertTriangle } from "lucide-react"
import Link from "next/link"

interface Plan {
  id: number
  name: string
  description: string
  minAmount: number
  maxAmount: number
  duration: number
  dailyProfit: number
  totalProfit: number
  features: string[]
  isPopular?: boolean
}

// Backend API response structure
interface BackendPlan {
  id: number
  name: string
  price: number
  monthlyProfit: number
  dailyProfitMin: number
  dailyProfitMax: number
  planLevel: number
}

interface PlansApiResponse {
  plans: BackendPlan[]
}

// Default plans in case API fails
const defaultPlans: Plan[] = [
  {
    id: 1,
    name: "Starter Plan",
    description: "Perfect for beginners",
    minAmount: 100,
    maxAmount: 1000,
    duration: 30,
    dailyProfit: 2.5,
    totalProfit: 75,
    features: [
      "Daily profit distribution",
      "24/7 support",
      "Secure transactions",
      "Instant activation"
    ]
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
    features: [
      "Higher daily profits",
      "Priority support",
      "Advanced analytics",
      "Referral bonuses"
    ],
    isPopular: true
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
    features: [
      "Maximum returns",
      "VIP support",
      "Exclusive features",
      "Higher referral rates"
    ]
  }
]

// Function to convert backend plan to frontend plan
const convertBackendPlanToFrontend = (backendPlan: BackendPlan): Plan => {
  // Calculate derived fields
  const avgDailyProfit = (backendPlan.dailyProfitMin + backendPlan.dailyProfitMax) / 2
  const duration = 30 // Default duration in days
  const totalProfit = avgDailyProfit * duration
  
  // Generate features based on plan level
  const features = [
    "Daily profit distribution",
    "24/7 support",
    "Secure transactions",
    "Instant activation"
  ]
  
  if (backendPlan.planLevel >= 2) {
    features.push("Higher daily profits", "Priority support")
  }
  
  if (backendPlan.planLevel >= 3) {
    features.push("Advanced analytics", "Referral bonuses")
  }
  
  if (backendPlan.planLevel >= 4) {
    features.push("Maximum returns", "VIP support", "Exclusive features")
  }
  
  return {
    id: backendPlan.id,
    name: backendPlan.name,
    description: `Level ${backendPlan.planLevel} Investment Plan`,
    minAmount: backendPlan.price,
    maxAmount: backendPlan.price * 10, // 10x the base price
    duration: duration,
    dailyProfit: avgDailyProfit,
    totalProfit: totalProfit,
    features: features,
    isPopular: backendPlan.planLevel === 2 // Level 2 is most popular
  }
}

export default function PlansPage() {
  const [plans, setPlans] = useState<Plan[]>(defaultPlans)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState("")
  const [usingFallbackData, setUsingFallbackData] = useState(false)

  useEffect(() => {
    fetchPlans()
  }, [])

  const fetchPlans = async (isRefresh = false) => {
    try {
      if (isRefresh) {
        setRefreshing(true)
      } else {
        setLoading(true)
      }
      setError("")
      setUsingFallbackData(false)

      const data = await apiService.getPlans()
      console.log("Raw API response:", data)
      
      // Handle the correct API response structure
      if (data && data.plans && Array.isArray(data.plans) && data.plans.length > 0) {
        // Convert backend plans to frontend format
        const convertedPlans = data.plans.map(convertBackendPlanToFrontend)
        setPlans(convertedPlans)
        setUsingFallbackData(false)
        console.log("Successfully loaded and converted plans from API:", convertedPlans)
      } else {
        console.warn("API returned empty or invalid data for plans:", data)
        setPlans(defaultPlans)
        setUsingFallbackData(true)
        setError("Unable to load plans from database. Showing demo plans.")
      }
    } catch (err: any) {
      console.error("Error fetching plans:", err)
      setError(err.message || "Failed to load plans from database")
      setPlans(defaultPlans)
      setUsingFallbackData(true)
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  const handleRefresh = () => {
    fetchPlans(true)
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

        <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-2">
              <BarChart3 className="h-6 w-6" />
              <h1 className="text-3xl font-bold text-foreground">Investment Plans</h1>
            </div>
            <div className="flex items-center space-x-2">
              <Button 
                variant="outline" 
                onClick={handleRefresh} 
                disabled={refreshing}
                className="bg-card border-border hover:bg-muted"
              >
                <RefreshCw className={`h-4 w-4 mr-2 ${refreshing ? 'animate-spin' : ''}`} />
                {refreshing ? 'Refreshing...' : 'Refresh'}
              </Button>
              <Link href="/dashboard">
                <Button variant="outline" className="flex items-center space-x-2 bg-card border-border hover:bg-muted">
                  <ArrowLeft className="h-4 w-4" />
                  <span>Back to Dashboard</span>
                </Button>
              </Link>
            </div>
          </div>

          {/* Fallback Data Warning */}
          {usingFallbackData && (
            <Alert className="mb-6 bg-yellow-50 border-yellow-200">
              <AlertTriangle className="h-4 w-4 text-yellow-600" />
              <AlertDescription className="text-yellow-800">
                <strong>Demo Mode:</strong> Unable to connect to the database. Showing sample investment plans. 
                Please check your backend connection and try refreshing.
              </AlertDescription>
            </Alert>
          )}

          {error && !usingFallbackData && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {/* Header Section */}
          <div className="text-center mb-8">
            <h2 className="text-2xl font-bold text-foreground mb-4">Choose Your Investment Plan</h2>
            <p className="text-muted-foreground max-w-2xl mx-auto">
              Select the perfect investment plan that matches your goals. All plans offer daily profit distribution 
              and are designed to maximize your returns with secure, transparent operations.
            </p>
          </div>

          {/* Plans Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            {plans && plans.length > 0 ? (
              plans.map((plan) => (
                <Card key={plan.id} className={`relative bg-card border-border ${plan.isPopular ? 'ring-2 ring-primary' : ''}`}>
                  {plan.isPopular && (
                    <div className="absolute -top-3 left-1/2 transform -translate-x-1/2">
                      <Badge className="bg-primary text-primary-foreground px-3 py-1">
                        Most Popular
                      </Badge>
                    </div>
                  )}
                  
                  <CardHeader className="text-center">
                    <CardTitle className="text-xl text-foreground">{plan.name}</CardTitle>
                    <CardDescription className="text-muted-foreground">{plan.description}</CardDescription>
                  </CardHeader>
                  
                  <CardContent className="space-y-6">
                    {/* Price Range */}
                    <div className="text-center">
                      <div className="text-3xl font-bold text-foreground mb-2">
                        ${plan.minAmount.toLocaleString()} - ${plan.maxAmount.toLocaleString()}
                      </div>
                      <p className="text-sm text-muted-foreground">Investment Range</p>
                    </div>

                    {/* Key Metrics */}
                    <div className="grid grid-cols-2 gap-4">
                      <div className="text-center p-3 bg-muted rounded-lg">
                        <Calendar className="h-6 w-6 mx-auto mb-2 text-primary" />
                        <p className="text-sm text-muted-foreground mb-1">Duration</p>
                        <p className="font-semibold text-foreground">{plan.duration} days</p>
                      </div>
                      <div className="text-center p-3 bg-muted rounded-lg">
                        <Percent className="h-6 w-6 mx-auto mb-2 text-primary" />
                        <p className="text-sm text-muted-foreground mb-1">Daily Profit</p>
                        <p className="font-semibold text-foreground">{plan.dailyProfit.toFixed(2)}%</p>
                      </div>
                    </div>

                    {/* Total Profit */}
                    <div className="text-center p-4 bg-primary/10 rounded-lg">
                      <p className="text-sm text-muted-foreground mb-1">Total Profit</p>
                      <p className="text-2xl font-bold text-primary">{plan.totalProfit.toFixed(0)}%</p>
                    </div>

                    {/* Features */}
                    <div className="space-y-3">
                      <h4 className="font-semibold text-sm text-foreground">Plan Features:</h4>
                      <ul className="space-y-2">
                        {plan.features && plan.features.map((feature, index) => (
                          <li key={index} className="flex items-center space-x-2 text-sm">
                            <Check className="h-4 w-4 text-green-600 flex-shrink-0" />
                            <span className="text-muted-foreground">{feature}</span>
                          </li>
                        ))}
                      </ul>
                    </div>

                    {/* Action Button */}
                    <Link href={`/deposit?plan=${plan.id}`}>
                      <Button 
                        className="w-full bg-primary hover:bg-primary/90 text-primary-foreground" 
                        size="lg"
                        disabled={usingFallbackData}
                      >
                        <TrendingUp className="h-4 w-4 mr-2" />
                        {usingFallbackData ? 'Demo Mode' : 'Invest Now'}
                      </Button>
                    </Link>
                  </CardContent>
                </Card>
              ))
            ) : (
              <div className="col-span-full text-center py-12">
                <p className="text-muted-foreground">No plans available at the moment.</p>
              </div>
            )}
          </div>

          {/* Investment Information */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* How It Works */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="text-foreground">How Investment Works</CardTitle>
                <CardDescription className="text-muted-foreground">Understanding the investment process</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-6 h-6 bg-primary text-primary-foreground rounded-full flex items-center justify-center text-sm font-bold">
                    1
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">Choose Your Plan</h4>
                    <p className="text-sm text-muted-foreground">
                      Select an investment plan that matches your goals and budget
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-6 h-6 bg-primary text-primary-foreground rounded-full flex items-center justify-center text-sm font-bold">
                    2
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">Make Your Deposit</h4>
                    <p className="text-sm text-muted-foreground">
                      Deposit your investment amount using secure payment methods
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-6 h-6 bg-primary text-primary-foreground rounded-full flex items-center justify-center text-sm font-bold">
                    3
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">Earn Daily Profits</h4>
                    <p className="text-sm text-muted-foreground">
                      Receive daily profit distributions directly to your account
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-6 h-6 bg-primary text-primary-foreground rounded-full flex items-center justify-center text-sm font-bold">
                    4
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">Withdraw Anytime</h4>
                    <p className="text-sm text-muted-foreground">
                      Withdraw your profits or reinvest for compound growth
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Investment Benefits */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="text-foreground">Investment Benefits</CardTitle>
                <CardDescription className="text-muted-foreground">Why choose our investment platform</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                    <TrendingUp className="h-4 w-4 text-green-600" />
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">High Returns</h4>
                    <p className="text-sm text-muted-foreground">
                      Competitive daily profit rates with transparent calculations
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
                    <DollarSign className="h-4 w-4 text-blue-600" />
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">Daily Payouts</h4>
                    <p className="text-sm text-muted-foreground">
                      Receive your profits daily, not monthly or yearly
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
                    <Check className="h-4 w-4 text-purple-600" />
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">Secure Platform</h4>
                    <p className="text-sm text-muted-foreground">
                      Advanced security measures to protect your investments
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 bg-orange-100 rounded-lg flex items-center justify-center">
                    <Calendar className="h-4 w-4 text-orange-600" />
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">Flexible Terms</h4>
                    <p className="text-sm text-muted-foreground">
                      Choose from various investment durations and amounts
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Call to Action */}
          <Card className="mt-8 bg-gradient-to-r from-primary to-primary/80 text-primary-foreground">
            <CardContent className="pt-6">
              <div className="text-center">
                <h3 className="text-2xl font-bold mb-4">Ready to Start Investing?</h3>
                <p className="text-primary-foreground/80 mb-6 max-w-2xl mx-auto">
                  Join thousands of investors who are already earning daily profits. 
                  Start your investment journey today with our secure and transparent platform.
                </p>
                <Link href="/deposit">
                  <Button 
                    size="lg" 
                    variant="secondary"
                    disabled={usingFallbackData}
                  >
                    <TrendingUp className="h-5 w-5 mr-2" />
                    {usingFallbackData ? 'Demo Mode - Connect Backend' : 'Start Investing Now'}
                  </Button>
                </Link>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </ProtectedRoute>
  )
}


