"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { BarChart3, ArrowLeft, DollarSign, Calendar, Percent, TrendingUp, Check } from "lucide-react"
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

export default function PlansPage() {
  const [plans, setPlans] = useState<Plan[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    fetchPlans()
  }, [])

  const fetchPlans = async () => {
    try {
      const data = await apiService.getPlans()
      setPlans(data)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
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
      <div className="min-h-screen bg-background">
        <Navbar />

        <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-2">
              <BarChart3 className="h-6 w-6" />
              <h1 className="text-3xl font-bold text-foreground">Investment Plans</h1>
            </div>
            <Link href="/dashboard">
              <Button variant="outline" className="flex items-center space-x-2">
                <ArrowLeft className="h-4 w-4" />
                <span>Back to Dashboard</span>
              </Button>
            </Link>
          </div>

          {error && (
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
            {plans.map((plan) => (
              <Card key={plan.id} className={`relative ${plan.isPopular ? 'ring-2 ring-primary' : ''}`}>
                {plan.isPopular && (
                  <div className="absolute -top-3 left-1/2 transform -translate-x-1/2">
                    <Badge className="bg-primary text-primary-foreground px-3 py-1">
                      Most Popular
                    </Badge>
                  </div>
                )}
                
                <CardHeader className="text-center">
                  <CardTitle className="text-xl">{plan.name}</CardTitle>
                  <CardDescription>{plan.description}</CardDescription>
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
                      <p className="font-semibold">{plan.duration} days</p>
                    </div>
                    <div className="text-center p-3 bg-muted rounded-lg">
                      <Percent className="h-6 w-6 mx-auto mb-2 text-primary" />
                      <p className="text-sm text-muted-foreground mb-1">Daily Profit</p>
                      <p className="font-semibold">{plan.dailyProfit}%</p>
                    </div>
                  </div>

                  {/* Total Profit */}
                  <div className="text-center p-4 bg-primary/10 rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Total Profit</p>
                    <p className="text-2xl font-bold text-primary">{plan.totalProfit}%</p>
                  </div>

                  {/* Features */}
                  <div className="space-y-3">
                    <h4 className="font-semibold text-sm">Plan Features:</h4>
                    <ul className="space-y-2">
                      {plan.features.map((feature, index) => (
                        <li key={index} className="flex items-center space-x-2 text-sm">
                          <Check className="h-4 w-4 text-green-600 flex-shrink-0" />
                          <span className="text-muted-foreground">{feature}</span>
                        </li>
                      ))}
                    </ul>
                  </div>

                  {/* Action Button */}
                  <Link href={`/deposit?plan=${plan.id}`}>
                    <Button className="w-full" size="lg">
                      <TrendingUp className="h-4 w-4 mr-2" />
                      Invest Now
                    </Button>
                  </Link>
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Investment Information */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* How It Works */}
            <Card>
              <CardHeader>
                <CardTitle>How Investment Works</CardTitle>
                <CardDescription>Understanding the investment process</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-6 h-6 bg-primary text-primary-foreground rounded-full flex items-center justify-center text-sm font-bold">
                    1
                  </div>
                  <div>
                    <h4 className="font-medium">Choose Your Plan</h4>
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
                    <h4 className="font-medium">Make Your Deposit</h4>
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
                    <h4 className="font-medium">Earn Daily Profits</h4>
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
                    <h4 className="font-medium">Withdraw Anytime</h4>
                    <p className="text-sm text-muted-foreground">
                      Withdraw your profits or reinvest for compound growth
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Investment Benefits */}
            <Card>
              <CardHeader>
                <CardTitle>Investment Benefits</CardTitle>
                <CardDescription>Why choose our investment platform</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                    <TrendingUp className="h-4 w-4 text-green-600" />
                  </div>
                  <div>
                    <h4 className="font-medium">High Returns</h4>
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
                    <h4 className="font-medium">Daily Payouts</h4>
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
                    <h4 className="font-medium">Secure Platform</h4>
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
                    <h4 className="font-medium">Flexible Terms</h4>
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
                  <Button size="lg" variant="secondary">
                    <TrendingUp className="h-5 w-5 mr-2" />
                    Start Investing Now
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
