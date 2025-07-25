"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { BarChart3, ArrowLeft, DollarSign, Calendar, Percent, TrendingUp, RefreshCw, AlertTriangle } from "lucide-react"
import Link from "next/link"
import { FrontendPlan, PlansApiResponse, convertBackendPlanToFrontend } from "@/types"
import { useLanguage } from "@/contexts/LanguageContext"

// Default plans in case API fails
const defaultPlans: FrontendPlan[] = [
  {
    id: 1,
    name: "المستوى الأول",
    description: "Level 1 Investment Plan",
    price: 60,
    monthlyProfit: 45,
    dailyProfitMin: 1.2,
    dailyProfitMax: 1.8,
    planLevel: 1,
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
    price: 150,
    monthlyProfit: 110,
    dailyProfitMin: 2.4,
    dailyProfitMax: 4.9,
    planLevel: 2,
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
    price: 300,
    monthlyProfit: 175,
    dailyProfitMin: 4.9,
    dailyProfitMax: 6.9,
    planLevel: 3,
    minAmount: 300,
    maxAmount: 3000,
    duration: 30,
    dailyProfit: 5.9,
    totalProfit: 175,
    features: []
  }
]

export default function PlansPage() {
  const [plans, setPlans] = useState<FrontendPlan[]>(defaultPlans)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState("")
  const [usingFallbackData, setUsingFallbackData] = useState(false)
  const { t } = useLanguage()

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
            <div>
              <h1 className="text-3xl font-bold text-foreground mb-2">{t('investment_plans')}</h1>
              <p className="text-muted-foreground">{t('select_perfect_plan')}</p>
            </div>
            <div className="flex items-center space-x-2">
              <Button 
                variant="outline" 
                onClick={handleRefresh}
                disabled={refreshing}
                className="flex items-center space-x-2 bg-card border-border hover:bg-muted"
              >
                <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
                <span>{refreshing ? t('loading') : t('refresh')}</span>
              </Button>
              <Link href="/dashboard">
                <Button variant="outline" className="flex items-center space-x-2 bg-card border-border hover:bg-muted">
                  <ArrowLeft className="h-4 w-4" />
                  <span>{t('back_to_dashboard')}</span>
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
            <h2 className="text-2xl font-bold text-foreground mb-4">{t('choose_your_plan')}</h2>
            <p className="text-muted-foreground max-w-2xl mx-auto">
              {t('select_perfect_plan')}
            </p>
          </div>

          {/* Plans Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 lg:gap-6 mb-8">
            {plans && plans.length > 0 ? (
              plans.map((plan) => (
                <Card key={plan.id} className="bg-card border-border hover:shadow-lg transition-shadow">
                  <CardHeader className="text-center pb-4">
                    <CardTitle className="text-lg sm:text-xl text-foreground">{plan.name}</CardTitle>
                    <CardDescription className="text-muted-foreground text-sm">{plan.description}</CardDescription>
                    <Badge variant="outline" className="w-fit mx-auto">
                      {t('level')} {plan.planLevel}
                    </Badge>
                  </CardHeader>
                  
                  <CardContent className="space-y-4">
                    {/* Price */}
                    <div className="text-center">
                      <div className="text-2xl sm:text-3xl font-bold text-foreground mb-1">
                        ${plan.price.toLocaleString()}
                      </div>
                      <p className="text-sm text-muted-foreground">{t('investment_amount')}</p>
                    </div>

                    {/* Daily Profit Range */}
                    <div className="text-center p-3 bg-muted rounded-lg">
                      <Percent className="h-5 w-5 sm:h-6 sm:w-6 mx-auto mb-2 text-primary" />
                      <p className="text-sm text-muted-foreground mb-1">{t('daily_profit_range')}</p>
                      <p className="font-semibold text-foreground">
                        ${plan.dailyProfitMin.toFixed(1)} - ${plan.dailyProfitMax.toFixed(1)}
                      </p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {t('avg')}: ${plan.dailyProfit.toFixed(1)}
                      </p>
                    </div>

                    {/* Monthly Profit */}
                    <div className="text-center p-3 bg-primary/10 rounded-lg">
                      <Calendar className="h-5 w-5 sm:h-6 sm:w-6 mx-auto mb-2 text-primary" />
                      <p className="text-sm text-muted-foreground mb-1">{t('monthly_profit')}</p>
                      <p className="text-lg sm:text-xl font-bold text-primary">${plan.monthlyProfit.toLocaleString()}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {((plan.monthlyProfit / plan.price) * 100).toFixed(1)}% {t('return')}
                      </p>
                    </div>

                    {/* Plan Details */}
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">{t('plan_level')}:</span>
                        <span className="font-medium text-foreground">{plan.planLevel}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">{t('duration')}:</span>
                        <span className="font-medium text-foreground">{plan.duration} {t('days')}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">{t('investment')}:</span>
                        <span className="font-medium text-foreground">${plan.price.toLocaleString()}</span>
                      </div>
                    </div>

                    {/* Action Button */}
                    <Link href={`/deposit?plan=${plan.id}`}>
                      <Button 
                        className="w-full bg-primary hover:bg-primary/90 text-primary-foreground" 
                        size="lg"
                        disabled={usingFallbackData}
                      >
                        <TrendingUp className="h-4 w-4 mr-2" />
                        {usingFallbackData ? t('demo_mode') : t('invest_now')}
                      </Button>
                    </Link>
                  </CardContent>
                </Card>
              ))
            ) : (
              <div className="col-span-full text-center py-12">
                <p className="text-muted-foreground">{t('no_plans_available')}</p>
              </div>
            )}
          </div>

          {/* Investment Information */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* How It Works */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="text-foreground">{t('how_investment_works')}</CardTitle>
                <CardDescription className="text-muted-foreground">{t('understanding_investment_process')}</CardDescription>
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
                    <Calendar className="h-4 w-4 text-purple-600" />
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">Flexible Terms</h4>
                    <p className="text-sm text-muted-foreground">
                      Choose from various investment amounts and profit levels
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



