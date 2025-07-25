"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
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
  const [localRemainingSeconds, setLocalRemainingSeconds] = useState(0)
  const [isTimerRunning, setIsTimerRunning] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    fetchDashboardData()
  }, [])

  // Real-time countdown timer
  useEffect(() => {
    console.log("Timer effect triggered:", {
      isActive: dashboardData?.counterStatus.isActive,
      remainingSeconds: dashboardData?.counterStatus.remainingSeconds,
      localRemainingSeconds,
      isTimerRunning
    })

    // If timer is not running locally, don't start countdown
    if (!isTimerRunning) {
      setLocalRemainingSeconds(0)
      return
    }

    // Start countdown from 24 hours if timer is running
    if (isTimerRunning && localRemainingSeconds === 0) {
      console.log("Starting timer from 24 hours")
      setLocalRemainingSeconds(24 * 60 * 60) // 24 hours in seconds
    }

    const interval = setInterval(() => {
      setLocalRemainingSeconds(prev => {
        console.log("Timer tick:", prev)
        if (prev <= 1) {
          clearInterval(interval)
          console.log("Timer completed")
          setIsTimerRunning(false)
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(interval)
  }, [isTimerRunning, localRemainingSeconds])

  const fetchDashboardData = async () => {
    try {
      const data = await apiService.getDashboard()
      console.log("Dashboard data received:", data)
      console.log("Counter status:", data.counterStatus)
      setDashboardData(data)
    } catch (err: any) {
      console.error("Dashboard fetch error:", err)
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleActivateCounter = async () => {
    setCounterLoading(true)
    setError("") // Clear previous errors
    console.log("Activating counter...")
    try {
      const response = await apiService.activateCounter()
      console.log("Counter activation response:", response)
      await fetchDashboardData()
      toast({
        title: "Success!",
        description: "Daily timer activated successfully",
      })
    } catch (err: any) {
      console.error("Counter activation error:", err)
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
    setError("") // Clear previous errors
    try {
      await apiService.completeCounter()
      await fetchDashboardData()
      toast({
        title: "Success!",
        description: "Daily profit claimed successfully",
      })
    } catch (err: any) {
      console.error("Counter completion error:", err)
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
    return (elapsed / totalSeconds) * 100
  }

  const getTimerColor = (remainingSeconds: number) => {
    const progress = getTimerProgress(remainingSeconds)
    if (progress < 25) return "border-green-500"
    if (progress < 50) return "border-blue-500"
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

  // Temporary test function to manually start timer
  const testStartTimer = () => {
    console.log("Testing timer start...")
    setIsTimerRunning(true)
    setLocalRemainingSeconds(24 * 60 * 60) // Start from 24 hours
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
              <Card className="mb-6 bg-card border-border">
                <CardContent className="pt-6">
                  <div className="text-center">
                    <h2 className="text-2xl font-bold text-foreground mb-2">{dashboardData.fullName}</h2>
                    <div className="flex items-center justify-center space-x-2 mb-4">
                      <span className="text-muted-foreground">Referral Code:</span>
                      <span className="font-mono text-primary font-bold">{dashboardData.username}</span>
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={copyReferralCode}
                        className="h-8 w-8 p-0 hover:bg-primary/10"
                      >
                        <Copy className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Balance Cards */}
              <div className="grid grid-cols-2 gap-4 mb-6">
                <Card className="bg-card border-border">
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <p className="text-sm text-muted-foreground mb-1">Total Amount</p>
                      <p className="text-xl font-bold text-foreground">${dashboardData.totalBalance.toFixed(2)}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-card border-border">
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <p className="text-sm text-muted-foreground mb-1">Total Profit</p>
                      <p className="text-xl font-bold text-primary">${dashboardData.totalProfits.toFixed(2)}</p>
                    </div>
                  </CardContent>
                </Card>
              </div>

              <div className="grid grid-cols-2 gap-4 mb-6">
                <Card className="bg-card border-border">
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <p className="text-sm text-muted-foreground mb-1">Today's Profit</p>
                      <p className="text-xl font-bold text-foreground">${dashboardData.dailyProfit.toFixed(2)}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-card border-border">
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <p className="text-sm text-muted-foreground mb-1">Bonus Profit</p>
                      <p className="text-xl font-bold text-primary">$125.00</p>
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Daily Timer Card */}
              <Card className="mb-6 bg-card border-border">
                <CardHeader className="text-center">
                  <CardTitle className="text-foreground">Daily Timer</CardTitle>
                  <CardDescription className="text-muted-foreground">
                    {dashboardData.currentPlanName ? `Current Plan: ${dashboardData.currentPlanName}` : "No active plan"}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex flex-col items-center space-y-4">
                    <div className={`w-32 h-32 rounded-full border-4 ${getTimerColor(localRemainingSeconds)} flex items-center justify-center bg-card relative`}>
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
                          className="text-primary"
                          strokeDasharray={`${getTimerProgress(localRemainingSeconds) * 2.83} 283`}
                          strokeLinecap="round"
                        />
                      </svg>
                      
                      {isTimerRunning && localRemainingSeconds > 0 ? (
                        <>
                          <div className="text-center relative z-10">
                            <div className="text-2xl font-bold text-foreground font-mono">
                              {formatTime(localRemainingSeconds)}
                            </div>
                            <div className="text-xs text-muted-foreground">Countdown</div>
                          </div>
                        </>
                      ) : isTimerRunning && localRemainingSeconds === 0 ? (
                        <div className="text-center relative z-10">
                          <div className="text-2xl font-bold text-primary font-mono">00:00:00</div>
                          <div className="text-xs text-primary">Ready to Claim!</div>
                        </div>
                      ) : (
                        <div className="text-center relative z-10">
                          <div className="text-2xl font-bold text-muted-foreground">24:00:00</div>
                          <div className="text-xs text-muted-foreground">
                            {dashboardData.currentPlanName ? "Click Activate to Start" : "Inactive"}
                          </div>
                        </div>
                      )}
                    </div>

                    {!dashboardData.currentPlanName && (
                      <div className="text-center space-y-2">
                        <p className="text-sm text-muted-foreground">
                          You need an active investment plan to start earning daily profits.
                        </p>
                        <Link href="/plans">
                          <Button className="bg-primary hover:bg-primary/90 text-primary-foreground">
                            <TrendingUp className="h-4 w-4 mr-2" />
                            Choose a Plan
                          </Button>
                        </Link>
                      </div>
                    )}

                    {dashboardData.currentPlanName && !dashboardData.counterStatus.isActive && !dashboardData.counterStatus.isCompleted && (
                      <div className="space-y-2 w-full">
                        <Button 
                          onClick={handleActivateCounter} 
                          disabled={counterLoading}
                          className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
                        >
                          {counterLoading ? (
                            <div className="flex items-center space-x-2">
                              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                              <span>Activating...</span>
                            </div>
                          ) : (
                            "Activate Timer"
                          )}
                        </Button>
                        {/* Temporary test button */}
                        <Button 
                          onClick={testStartTimer}
                          className="w-full bg-yellow-600 hover:bg-yellow-700 text-white text-xs"
                        >
                          Test Timer (24h)
                        </Button>
                      </div>
                    )}

                    {dashboardData.counterStatus.isActive && localRemainingSeconds === 0 && (
                      <div className="space-y-2 w-full">
                        <Button
                          onClick={handleCompleteCounter}
                          disabled={counterLoading}
                          className="bg-green-600 hover:bg-green-700 text-white w-full"
                        >
                          {counterLoading ? (
                            <div className="flex items-center space-x-2">
                              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                              <span>Claiming...</span>
                            </div>
                          ) : (
                            <div className="flex items-center space-x-2">
                              <CheckCircle className="h-4 w-4" />
                              <span>Claim Profit</span>
                            </div>
                          )}
                        </Button>
                        <Button
                          onClick={handleActivateCounter}
                          disabled={counterLoading}
                          className="bg-primary hover:bg-primary/90 text-primary-foreground w-full"
                        >
                          {counterLoading ? (
                            <div className="flex items-center space-x-2">
                              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                              <span>Activating...</span>
                            </div>
                          ) : (
                            <div className="flex items-center space-x-2">
                              <Play className="h-4 w-4" />
                              <span>Start New Timer</span>
                            </div>
                          )}
                        </Button>
                      </div>
                    )}

                    {dashboardData.counterStatus.isCompleted && dashboardData.counterStatus.needsReset && (
                      <Button 
                        onClick={handleActivateCounter} 
                        disabled={counterLoading}
                        className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
                      >
                        {counterLoading ? (
                          <div className="flex items-center space-x-2">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                            <span>Activating...</span>
                          </div>
                        ) : (
                          "Activate Timer"
                        )}
                      </Button>
                    )}

                    {dashboardData.counterStatus.isCompleted && !dashboardData.counterStatus.needsReset && (
                      <div className="text-center">
                        <Badge variant="secondary">
                          Completed
                        </Badge>
                        <p className="text-sm text-muted-foreground mt-2">Come back tomorrow!</p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Navigation Buttons */}
              <div className="grid grid-cols-2 gap-4 mb-6">
                <Link href="/deposit">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <TrendingUp className="h-6 w-6 text-foreground" />
                    <span className="text-sm font-medium text-foreground">Deposit</span>
                  </Button>
                </Link>

                <Link href="/withdraw">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <Banknote className="h-6 w-6 text-foreground" />
                    <span className="text-sm font-medium text-foreground">Withdraw</span>
                  </Button>
                </Link>
              </div>

              <div className="grid grid-cols-2 gap-4 mb-6">
                <Link href="/withdrawal-history">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <History className="h-6 w-6 text-foreground" />
                    <span className="text-sm font-medium text-foreground">Transactions</span>
                  </Button>
                </Link>

                <Link href="/referrals">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <Users className="h-6 w-6 text-foreground" />
                    <span className="text-sm font-medium text-foreground">Network</span>
                  </Button>
                </Link>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <Link href="/profile">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <ProfileIcon className="h-6 w-6 text-foreground" />
                    <span className="text-sm font-medium text-foreground">About</span>
                  </Button>
                </Link>

                <Link href="/plans">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <BarChart3 className="h-6 w-6 text-foreground" />
                    <span className="text-sm font-medium text-foreground">Investment</span>
                  </Button>
                </Link>
              </div>
            </>
          )}
        </div>

        {/* Bottom Navigation Bar */}
        <div className="fixed bottom-0 left-0 right-0 bg-card border-t border-border">
          <div className="flex justify-around py-2">
            <Link href="/dashboard" className="flex flex-col items-center space-y-1 p-2">
              <Home className="h-5 w-5 text-primary" />
              <span className="text-xs text-primary">Home</span>
            </Link>
            <Link href="/plans" className="flex flex-col items-center space-y-1 p-2">
              <BarChart3 className="h-5 w-5 text-foreground" />
              <span className="text-xs text-foreground">Investment</span>
            </Link>
            <Link href="/wallet" className="flex flex-col items-center space-y-1 p-2">
              <Wallet className="h-5 w-5 text-foreground" />
              <span className="text-xs text-foreground">Wallet</span>
            </Link>
            <Link href="/profile" className="flex flex-col items-center space-y-1 p-2">
              <ProfileIcon className="h-5 w-5 text-foreground" />
              <span className="text-xs text-foreground">Profile</span>
            </Link>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
} 