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
      
      // Handle the correct API response structure
      if (data && data.plans && Array.isArray(data.plans) && data.plans.length > 0) {
        // Convert backend plans to frontend format
        const convertedPlans = data.plans.map(convertBackendPlanToFrontend)
        setPlans(convertedPlans)
        setUsingFallbackData(false)
      } else {
        setPlans(defaultPlans)
        setUsingFallbackData(true)
        setError("Unable to load plans from database. Showing demo plans.")
      }
    } catch (err: any) {
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
        <div className="min-h-screen bg-black overflow-x-hidden">
          <Navbar />
          <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8 max-w-full overflow-x-hidden">
            <div className="flex items-center justify-center h-64">
              <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-400"></div>
            </div>
          </div>
        </div>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-black overflow-x-hidden">
        <Navbar />

        <div className="w-full max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex flex-wrap items-center justify-between gap-2 mb-6">
            <div>
              <h1 className="text-3xl font-bold text-foreground mb-2">{t('investment_plans')}</h1>
              <p className="text-muted-foreground">{t('select_perfect_plan')}</p>
            </div>
            <div className="flex flex-col sm:flex-row gap-2 w-full sm:w-auto">
              <Button 
                variant="outline" 
                onClick={handleRefresh}
                disabled={refreshing}
                className="flex items-center space-x-2 bg-gradient-to-br bg-card border-border hover:from-gray-700 hover:to-custom-2e2e2e text-foreground"
              >
                <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
                <span>{refreshing ? t('loading') : t('refresh')}</span>
              </Button>
              <Link href="/dashboard">
                <Button variant="outline" className="flex items-center space-x-2 bg-gradient-to-br bg-card border-border hover:from-gray-700 hover:to-custom-2e2e2e text-foreground w-full">
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
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 lg:gap-6 mb-8 w-full">
            {plans && plans.length > 0 ? (
              plans.map((plan) => (
                <Card key={plan.id} className="bg-gradient-to-br bg-card border-border hover:shadow-lg transition-shadow w-full">
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
                    <div className="text-center p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                      <Percent className="h-5 w-5 sm:h-6 sm:w-6 mx-auto mb-2 text-green-400" />
                      <p className="text-sm text-muted-foreground mb-1">{t('daily_profit_range')}</p>
                      <p className="font-semibold text-foreground">
                        ${plan.dailyProfitMin.toFixed(1)} - ${plan.dailyProfitMax.toFixed(1)}
                      </p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {t('avg')}: ${plan.dailyProfit.toFixed(1)}
                      </p>
                    </div>

                    {/* Monthly Profit */}
                    <div className="text-center p-3 bg-gray-700/20 rounded-lg">
                      <Calendar className="h-5 w-5 sm:h-6 sm:w-6 mx-auto mb-2 text-green-400" />
                      <p className="text-sm text-muted-foreground mb-1">{t('monthly_profit')}</p>
                      <p className="text-lg sm:text-xl font-bold text-green-400">${plan.monthlyProfit.toLocaleString()}</p>
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
                        className="w-full bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-foreground" 
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
            <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
              <CardHeader>
                <CardTitle className="text-foreground">{t('how_investment_works')}</CardTitle>
                <CardDescription className="text-muted-foreground">{t('understanding_investment_process')}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-6 h-6 bg-gray-700 text-foreground rounded-full flex items-center justify-center text-sm font-bold">
                    1
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">{t('choose_your_plan_step')}</h4>
                    <p className="text-sm text-muted-foreground">
                      {t('choose_plan_description')}
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-6 h-6 bg-gray-700 text-foreground rounded-full flex items-center justify-center text-sm font-bold">
                    2
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">{t('make_deposit_step')}</h4>
                    <p className="text-sm text-muted-foreground">
                      {t('make_deposit_description')}
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-6 h-6 bg-gray-700 text-foreground rounded-full flex items-center justify-center text-sm font-bold">
                    3
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">{t('earn_daily_profits_step')}</h4>
                    <p className="text-sm text-muted-foreground">
                      {t('earn_daily_profits_description')}
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-6 h-6 bg-gray-700 text-foreground rounded-full flex items-center justify-center text-sm font-bold">
                    4
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">{t('withdraw_anytime_step')}</h4>
                    <p className="text-sm text-muted-foreground">
                      {t('withdraw_anytime_description')}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Investment Benefits */}
            <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
              <CardHeader>
                <CardTitle className="text-foreground">{t('investment_benefits')}</CardTitle>
                <CardDescription className="text-muted-foreground">{t('why_choose_platform')}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                    <TrendingUp className="h-4 w-4 text-green-600" />
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">{t('high_returns')}</h4>
                    <p className="text-sm text-muted-foreground">
                      {t('high_returns_description')}
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 bg-gradient-to-r from-gray-700 to-custom-2e2e2e rounded-lg flex items-center justify-center">
                    <DollarSign className="h-4 w-4 text-green-400" />
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">{t('daily_payouts')}</h4>
                    <p className="text-sm text-muted-foreground">
                      {t('daily_payouts_description')}
                    </p>
                  </div>
                </div>
                
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
                    <Calendar className="h-4 w-4 text-purple-600" />
                  </div>
                  <div>
                    <h4 className="font-medium text-foreground">{t('flexible_terms')}</h4>
                    <p className="text-sm text-muted-foreground">
                      {t('flexible_terms_description')}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Call to Action */}
                      <Card className="mt-8 bg-gradient-to-r from-custom-2e2e2e to-gray-900 text-foreground">
              <CardHeader className="text-center">
                <CardTitle className="text-foreground">{t('investment_process')}</CardTitle>
                <CardDescription className="text-gray-300 mb-6 max-w-2xl mx-auto">
                  {t('join_thousands')}
                </CardDescription>
              </CardHeader>
            <CardContent className="pt-6">
              <div className="text-center">
                <h3 className="text-2xl font-bold mb-4">{t('ready_to_start')}</h3>
                <p className="text-primary-foreground/80 mb-6 max-w-2xl mx-auto">
                  {t('join_thousands')}
                </p>
                <Link href="/deposit">
                  <Button 
                    size="lg" 
                    variant="secondary"
                    disabled={usingFallbackData}
                  >
                    <TrendingUp className="h-5 w-5 mr-2" />
                    {usingFallbackData ? t('demo_mode') : t('start_investing_now')}
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



