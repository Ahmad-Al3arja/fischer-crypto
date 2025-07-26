"use client"

import { useState, useEffect, useCallback } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { useAuth } from "@/contexts/AuthContext"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import {
  User,
  DollarSign,
  TrendingUp,
  Clock,
  Play,
  CheckCircle,
  CreditCard,
  Banknote,
  History,
  BarChart3,
  Users,
  Copy,
  Home,
  Wallet,
  User as ProfileIcon,
} from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { useLanguage } from "@/contexts/LanguageContext"


interface DashboardData {
  fullName: string
  username: string
  phoneNumber: string
  currentPlanName: string
  totalBalance: number
  totalProfits: number
  dailyProfit: number
  counterStatus: {
    isActive: boolean
    isCompleted: boolean
    remainingSeconds: number
    needsReset: boolean
  }
  activationPending: boolean
  activationMessage?: string
}

export default function DashboardPage() {
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [counterLoading, setCounterLoading] = useState(false)
  
  // Timer state - simplified to rely on backend data
  const [displayTimeSeconds, setDisplayTimeSeconds] = useState(0)
  const [isTimerActive, setIsTimerActive] = useState(false)
  const [lastSyncTime, setLastSyncTime] = useState(Date.now())
  
  const { toast } = useToast()
  const { t } = useLanguage()
  const { user, loading: authLoading, checkAuth } = useAuth()

  // Fetch dashboard data from backend
  const fetchDashboardData = useCallback(async () => {
    try {
      const data = await apiService.getDashboard()
      setDashboardData(data)
      
      // Update timer state based on backend data
      if (data.counterStatus) {
        const { isActive, remainingSeconds, isCompleted } = data.counterStatus
        
        // Set timer active state based on backend
        // If boolean flags are undefined, use remainingSeconds as fallback
        const timerActive = isActive !== undefined ? isActive : (remainingSeconds > 0)
        const timerCompleted = isCompleted !== undefined ? isCompleted : false
        
        setIsTimerActive(timerActive && !timerCompleted)
        
        // Set display time to backend remaining seconds
        setDisplayTimeSeconds(Math.max(0, remainingSeconds))
        setLastSyncTime(Date.now())
      }
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  // Initial data fetch - wait for authentication to be ready
  useEffect(() => {
    if (!authLoading && user) {
      // Validate token before fetching data
      checkAuth().then(isValid => {
        if (isValid) {
          fetchDashboardData()
        }
      })
    }
  }, [fetchDashboardData, authLoading, user, checkAuth])

  // Periodic sync with backend every 30 seconds
  useEffect(() => {
    const syncInterval = setInterval(() => {
      if (dashboardData?.counterStatus?.isActive) {
        fetchDashboardData()
      }
    }, 30000) // Sync every 30 seconds

    return () => clearInterval(syncInterval)
  }, [dashboardData?.counterStatus?.isActive, fetchDashboardData])

  // Local countdown timer for smooth UI updates
  useEffect(() => {
    if (!isTimerActive || displayTimeSeconds <= 0) {
      return
    }

    const interval = setInterval(() => {
      setDisplayTimeSeconds(prev => {
        // Calculate elapsed time since last sync to ensure accuracy
        const now = Date.now()
        const elapsedSinceSync = Math.floor((now - lastSyncTime) / 1000)
        const backendTime = dashboardData?.counterStatus?.remainingSeconds || 0
        const correctedTime = Math.max(0, backendTime - elapsedSinceSync)
        
        // If the corrected time is significantly different, use it
        if (Math.abs(correctedTime - prev) > 5) {
          return correctedTime
        }
        
        // Normal countdown
        const newTime = Math.max(0, prev - 1)
        
        // If timer reaches 0, mark as inactive and sync with backend
        if (newTime <= 0) {
          setIsTimerActive(false)
          // Sync with backend to get updated state
          setTimeout(() => fetchDashboardData(), 1000)
        }
        
        return newTime
      })
    }, 1000)

    return () => clearInterval(interval)
  }, [isTimerActive, displayTimeSeconds, lastSyncTime, dashboardData?.counterStatus?.remainingSeconds, fetchDashboardData])

  const handleActivateCounter = async () => {
    setCounterLoading(true)
    setError("")
    
    try {
      const response = await apiService.activateCounter()
      
      // Fetch fresh data to get the new timer state
      await fetchDashboardData()
      
      toast({
        title: "Success!",
        description: "Daily timer activated successfully",
      })
    } catch (err: any) {
      // Show specific error messages based on the backend response
      if (err.message.includes("active plan")) {
        setError("You must purchase a plan first to activate the timer. Please visit the Plans page to invest.")
      } else if (err.message.includes("activated by admin")) {
        setError("Your account must be activated by admin first. Please contact support.")
      } else if (err.message.includes("already active")) {
        setError("Daily timer is already active. Please wait for it to complete.")
      } else {
        setError(err.message || "Failed to activate timer. Please try again.")
      }
    } finally {
      setCounterLoading(false)
    }
  }

  const handleCompleteCounter = async () => {
    setCounterLoading(true)
    setError("")
    
    try {
      await apiService.completeCounter()
      await fetchDashboardData()
      
      toast({
        title: "Success!",
        description: "Daily profit claimed successfully",
      })
    } catch (err: any) {
      setError(err.message || "Failed to claim profit. Please try again.")
    } finally {
      setCounterLoading(false)
    }
  }

  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60
    return `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`
  }

  const getTimerProgress = (remainingSeconds: number) => {
    const totalSeconds = 24 * 60 * 60 // 24 hours in seconds
    const elapsed = totalSeconds - remainingSeconds
    return Math.min(100, Math.max(0, (elapsed / totalSeconds) * 100))
  }

  const getTimerColor = (remainingSeconds: number) => {
    const progress = getTimerProgress(remainingSeconds)
    if (progress < 25) return "border-green-500"
    if (progress < 50) return "border-gray-500"
    if (progress < 75) return "border-yellow-500"
    return "border-red-500"
  }

  const copyReferralCode = () => {
    if (dashboardData?.username) {
      navigator.clipboard.writeText(dashboardData.username)
      toast({
        title: "Copied!",
        description: "Referral code copied to clipboard",
      })
    }
  }

  // Determine timer display state
  const getTimerDisplayState = () => {
    if (!dashboardData) return "loading"
    
    const { counterStatus, currentPlanName } = dashboardData
    
    if (!currentPlanName) return "no-plan"
    
    // Handle undefined boolean flags by using remainingSeconds as fallback
    const isActive = counterStatus.isActive !== undefined ? counterStatus.isActive : (counterStatus.remainingSeconds > 0)
    const isCompleted = counterStatus.isCompleted !== undefined ? counterStatus.isCompleted : false
    
    if (isActive && !isCompleted) return "active"
    if (isCompleted) return "completed"
    if (!isActive && !isCompleted) return "inactive"
    
    return "inactive"
  }

  const timerDisplayState = getTimerDisplayState()

  if (loading || authLoading) {
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

        <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8 pb-32">
          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="text-4xl font-bold text-foreground tracking-wider mb-2">FISCHER</h1>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {dashboardData && (
            <>
              {/* User Info Card */}
              <Card className="mb-6 bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
                <CardContent className="pt-6">
                  <div className="text-center">
                    <h2 className="text-2xl font-bold text-foreground mb-2">{dashboardData.fullName}</h2>
                    <div className="flex items-center justify-center space-x-2 mb-4">
                      <span className="text-muted-foreground">{t('referral_code')}:</span>
                      <span className="font-mono text-green-400 font-bold">{dashboardData.username}</span>
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={copyReferralCode}
                        className="h-8 w-8 p-0 hover:bg-gray-700"
                      >
                        <Copy className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Balance Cards */}
              <div className="grid grid-cols-2 gap-4 mb-6">
                <Card className="bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <p className="text-sm text-gray-400 mb-1">{t('total_amount')}</p>
                      <p className="text-xl font-bold text-white">${dashboardData.totalBalance.toFixed(2)}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <p className="text-sm text-gray-400 mb-1">{t('total_profit')}</p>
                      <p className="text-xl font-bold text-green-400">${dashboardData.totalProfits.toFixed(2)}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <p className="text-sm text-gray-400 mb-1">{t('todays_profit')}</p>
                      <p className="text-xl font-bold text-white">${dashboardData.dailyProfit.toFixed(2)}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <p className="text-sm text-gray-400 mb-1">{t('bonus_profit')}</p>
                      <p className="text-xl font-bold text-green-400">$125.00</p>
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Daily Timer Card */}
              <Card className="mb-6 bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
                <CardHeader className="text-center">
                  <CardTitle className="text-foreground">{t('daily_timer')}</CardTitle>
                  <CardDescription className="text-muted-foreground">
                    {dashboardData.currentPlanName ? `${t('current_plan')}: ${dashboardData.currentPlanName}` : t('no_active_plan')}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex flex-col items-center space-y-4">
                    <div className={`w-32 h-32 rounded-full border-4 ${getTimerColor(displayTimeSeconds)} flex items-center justify-center bg-gradient-to-br from-custom-2e2e2e to-gray-900 relative`}>
                      {/* Progress ring */}
                      <div className="absolute inset-0 rounded-full border-4 border-gray-700"></div>
                      <svg className="absolute inset-0 w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                        <circle
                          cx="50"
                          cy="50"
                          r="45"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="4"
                          className="text-green-400"
                          strokeDasharray={`${getTimerProgress(displayTimeSeconds) * 2.83} 283`}
                          strokeLinecap="round"
                        />
                      </svg>
                      
                      <div className="text-center relative z-10">
                        {timerDisplayState === "active" ? (
                          <>
                            <div className="text-2xl font-bold text-foreground font-mono">
                              {formatTime(displayTimeSeconds)}
                            </div>
                            <div className="text-xs text-muted-foreground">{t('countdown')}</div>
                          </>
                        ) : timerDisplayState === "completed" ? (
                          <>
                            <div className="text-2xl font-bold text-green-400 font-mono">00:00:00</div>
                            <div className="text-xs text-green-400">{t('ready_to_claim')}</div>
                          </>
                        ) : (
                          <>
                            <div className="text-2xl font-bold text-muted-foreground">
                              {dashboardData.counterStatus.isActive !== undefined ? 
                                (dashboardData.counterStatus.isActive ? formatTime(displayTimeSeconds) : "24:00:00") :
                                (displayTimeSeconds > 0 ? formatTime(displayTimeSeconds) : "24:00:00")
                              }
                            </div>
                            <div className="text-xs text-muted-foreground">
                              {dashboardData.currentPlanName ? "Click Activate to Start" : "Inactive"}
                            </div>
                          </>
                        )}
                      </div>
                    </div>

                    {/* Timer Action Buttons */}
                    {timerDisplayState === "no-plan" && (
                      <div className="text-center space-y-2">
                                                 <p className="text-sm text-muted-foreground">
                           {t('need_active_plan_message')}
                         </p>
                        <Link href="/plans">
                          <Button className="bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-white">
                            <TrendingUp className="h-4 w-4 mr-2" />
                            Choose a Plan
                          </Button>
                        </Link>
                      </div>
                    )}

                    {timerDisplayState === "inactive" && dashboardData.currentPlanName && (
                      <Button 
                        onClick={handleActivateCounter} 
                        disabled={counterLoading}
                        className="w-full bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-white"
                      >
                        {counterLoading ? (
                          <div className="flex items-center space-x-2">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                            <span>Activating...</span>
                          </div>
                        ) : (
                          <div className="flex items-center space-x-2">
                            <Play className="h-4 w-4" />
                            <span>Activate Timer</span>
                          </div>
                        )}
                      </Button>
                    )}

                    {timerDisplayState === "active" && (
                      <div className="text-center">
                        <Badge variant="default" className="bg-green-100 text-green-800">
                          <Clock className="h-3 w-3 mr-1" />
                          {t('timer_active_status')}
                        </Badge>
                        <p className="text-sm text-muted-foreground mt-2">
                          {t('come_back_later')}
                        </p>
                      </div>
                    )}

                    {timerDisplayState === "completed" && (
                      <div className="space-y-2 w-full">
                        <Button
                          onClick={handleCompleteCounter}
                          disabled={counterLoading}
                          className="bg-green-600 hover:bg-green-700 text-white w-full"
                        >
                          {counterLoading ? (
                            <div className="flex items-center space-x-2">
                              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                              <span>{t('claiming')}</span>
                            </div>
                          ) : (
                            <div className="flex items-center space-x-2">
                              <CheckCircle className="h-4 w-4" />
                              <span>{t('claim_profit')}</span>
                            </div>
                          )}
                        </Button>
                        <Button
                          onClick={handleActivateCounter}
                          disabled={counterLoading}
                          className="bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-white w-full"
                        >
                          {counterLoading ? (
                            <div className="flex items-center space-x-2">
                              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                              <span>{t('activating')}</span>
                            </div>
                          ) : (
                            <div className="flex items-center space-x-2">
                              <Play className="h-4 w-4" />
                              <span>{t('start_new_timer')}</span>
                            </div>
                          )}
                        </Button>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

                             {/* Navigation Buttons */}
               <div className="grid grid-cols-2 gap-4 mb-6">
                 <Link href="/deposit">
                   <Button className="w-full h-16 sm:h-20 flex flex-col items-center justify-center space-y-2 bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 hover:from-gray-700 hover:to-custom-2e2e2e shadow-lg">
                     <TrendingUp className="h-5 w-5 sm:h-6 sm:w-6 text-white" />
                     <span className="text-xs sm:text-sm font-medium text-white">{t('deposit')}</span>
                   </Button>
                 </Link>

                 <Link href="/withdraw">
                   <Button className="w-full h-16 sm:h-20 flex flex-col items-center justify-center space-y-2 bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 hover:from-gray-700 hover:to-custom-2e2e2e shadow-lg">
                     <Banknote className="h-5 w-5 sm:h-6 sm:w-6 text-white" />
                     <span className="text-xs sm:text-sm font-medium text-white">{t('withdraw')}</span>
                   </Button>
                 </Link>

                 <Link href="/profile">
                   <Button className="w-full h-16 sm:h-20 flex flex-col items-center justify-center space-y-2 bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 hover:from-gray-700 hover:to-custom-2e2e2e shadow-lg">
                     <User className="h-5 w-5 sm:h-6 sm:w-6 text-white" />
                     <span className="text-xs sm:text-sm font-medium text-white">{t('about')}</span>
                   </Button>
                 </Link>

                 <Link href="/withdrawal-history">
                   <Button className="w-full h-16 sm:h-20 flex flex-col items-center justify-center space-y-2 bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 hover:from-gray-700 hover:to-custom-2e2e2e shadow-lg">
                     <History className="h-5 w-5 sm:h-6 sm:w-6 text-white" />
                     <span className="text-xs sm:text-sm font-medium text-white">{t('transactions')}</span>
                   </Button>
                 </Link>
               </div>
            </>
          )}
        </div>

        {/* Bottom Navigation Bar */}
                  <div className="fixed bottom-0 left-0 right-0 bg-black border-t border-gray-800 shadow-lg z-[9999]">
          <div className="flex justify-around py-3">
            <Link href="/dashboard" className="flex flex-col items-center space-y-1 p-2">
                              <Home className="h-5 w-5 text-white" />
                <span className="text-xs text-white">{t('home')}</span>
            </Link>
            <Link href="/plans" className="flex flex-col items-center space-y-1 p-2">
                              <BarChart3 className="h-5 w-5 text-white" />
                <span className="text-xs text-white">{t('investment')}</span>
            </Link>
            <Link href="/wallet" className="flex flex-col items-center space-y-1 p-2">
                              <Wallet className="h-5 w-5 text-white" />
                <span className="text-xs text-white">{t('wallet')}</span>
            </Link>
            <Link href="/referrals" className="flex flex-col items-center space-y-1 p-2">
                              <Users className="h-5 w-5 text-white" />
                <span className="text-xs text-white">{t('referrals')}</span>
            </Link>
            <Link href="/profile" className="flex flex-col items-center space-y-1 p-2">
                              <ProfileIcon className="h-5 w-5 text-white" />
                <span className="text-xs text-white">{t('profile')}</span>
            </Link>
          </div>
        </div>
        
        
      </div>
    </ProtectedRoute>
  )
}
