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
  Timer,
} from "lucide-react"
import Link from "next/link"

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
  const [countdown, setCountdown] = useState(86400) // 24 hours in seconds
  const [isTimerActive, setIsTimerActive] = useState(false)

  useEffect(() => {
    fetchDashboardData()
  }, [])

  const fetchDashboardData = async () => {
    try {
      const data = await apiService.getDashboard()
      setDashboardData(data)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleActivateCounter = async () => {
    setCounterLoading(true)
    try {
      await apiService.activateCounter()
      await fetchDashboardData()
    } catch (err: any) {
      setError(err.message)
    } finally {
      setCounterLoading(false)
    }
  }

  const handleCompleteCounter = async () => {
    setCounterLoading(true)
    try {
      await apiService.completeCounter()
      await fetchDashboardData()
    } catch (err: any) {
      setError(err.message)
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

  const copyReferralCode = () => {
    if (dashboardData?.username) {
      navigator.clipboard.writeText(dashboardData.username)
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

        <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {dashboardData && (
            <>
              {/* User Info Section */}
              <div className="mb-8">
                <h1 className="text-2xl font-bold text-foreground mb-2">{dashboardData.fullName}</h1>
                <div className="flex items-center space-x-4">
                  <span className="text-muted-foreground">Referral Code:</span>
                  <div className="flex items-center space-x-2 bg-card px-3 py-2 rounded-md border border-border">
                    <span className="text-foreground font-mono">{dashboardData.username}</span>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={copyReferralCode}
                      className="h-6 w-6 p-0 text-muted-foreground hover:text-foreground"
                    >
                      <Copy className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
              </div>

              {/* Balance Cards */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
                <Card className="bg-card border-border card-hover">
                  <CardContent className="p-4">
                    <div className="text-sm text-muted-foreground mb-1">Total Amount</div>
                    <div className="text-xl font-bold text-foreground">${dashboardData.totalBalance.toFixed(2)}</div>
                  </CardContent>
                </Card>

                <Card className="bg-card border-border card-hover">
                  <CardContent className="p-4">
                    <div className="text-sm text-muted-foreground mb-1">Total Profit</div>
                    <div className="text-xl font-bold text-green-500">${dashboardData.totalProfits.toFixed(2)}</div>
                  </CardContent>
                </Card>

                <Card className="bg-card border-border card-hover">
                  <CardContent className="p-4">
                    <div className="text-sm text-muted-foreground mb-1">Today's Profit</div>
                    <div className="text-xl font-bold text-blue-500">${dashboardData.dailyProfit.toFixed(2)}</div>
                  </CardContent>
                </Card>

                <Card className="bg-card border-border card-hover">
                  <CardContent className="p-4">
                    <div className="text-sm text-muted-foreground mb-1">Bonus Profit</div>
                    <div className="text-xl font-bold text-yellow-500">$125.00</div>
                  </CardContent>
                </Card>
              </div>

              {/* Daily Timer Card */}
              <Card className="mb-8 bg-card border-border">
                <CardHeader>
                  <CardTitle className="text-foreground flex items-center space-x-2">
                    <Timer className="h-5 w-5" />
                    <span>Daily Timer</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {!dashboardData.counterStatus.isActive && !dashboardData.counterStatus.isCompleted && (
                    <div className="text-center py-8">
                      <div className="w-32 h-32 mx-auto mb-4 rounded-full border-4 border-muted flex items-center justify-center">
                        <span className="text-2xl font-mono text-foreground">00:00:00</span>
                      </div>
                      <p className="text-muted-foreground mb-4">Remaining Today</p>
                      <Button 
                        onClick={handleActivateCounter} 
                        disabled={counterLoading}
                        className="bg-primary hover:bg-primary/90 text-primary-foreground btn-animate"
                      >
                        {counterLoading ? (
                          <div className="flex items-center space-x-2">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                            <span>Activating...</span>
                          </div>
                        ) : (
                          <div className="flex items-center space-x-2">
                            <Play className="h-4 w-4" />
                            <span>Activate Timer</span>
                          </div>
                        )}
                      </Button>
                    </div>
                  )}

                  {dashboardData.counterStatus.isActive && dashboardData.counterStatus.remainingSeconds > 0 && (
                    <div className="text-center py-8">
                      <div className="w-32 h-32 mx-auto mb-4 rounded-full border-4 border-primary flex items-center justify-center">
                        <span className="text-2xl font-mono text-foreground">
                          {formatTime(dashboardData.counterStatus.remainingSeconds)}
                        </span>
                      </div>
                      <p className="text-muted-foreground mb-4">Remaining Today</p>
                      <Badge variant="outline" className="bg-primary/10 text-primary border-primary">
                        Active
                      </Badge>
                    </div>
                  )}

                  {dashboardData.counterStatus.isActive && dashboardData.counterStatus.remainingSeconds === 0 && (
                    <div className="text-center py-8">
                      <div className="w-32 h-32 mx-auto mb-4 rounded-full border-4 border-green-500 flex items-center justify-center">
                        <CheckCircle className="h-12 w-12 text-green-500" />
                      </div>
                      <p className="text-green-500 mb-4">Counter completed! Claim your profit.</p>
                      <Button
                        onClick={handleCompleteCounter}
                        disabled={counterLoading}
                        className="bg-green-600 hover:bg-green-700 text-white btn-animate"
                      >
                        {counterLoading ? (
                          <div className="flex items-center space-x-2">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                            <span>Claiming...</span>
                          </div>
                        ) : (
                          <div className="flex items-center space-x-2">
                            <CheckCircle className="h-4 w-4" />
                            <span>Claim Profit</span>
                          </div>
                        )}
                      </Button>
                    </div>
                  )}

                  {dashboardData.counterStatus.isCompleted && dashboardData.counterStatus.needsReset && (
                    <div className="text-center py-8">
                      <p className="text-muted-foreground mb-4">Profit claimed! You can activate the counter again.</p>
                      <Button 
                        onClick={handleActivateCounter} 
                        disabled={counterLoading}
                        className="bg-primary hover:bg-primary/90 text-primary-foreground btn-animate"
                      >
                        {counterLoading ? (
                          <div className="flex items-center space-x-2">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                            <span>Activating...</span>
                          </div>
                        ) : (
                          <div className="flex items-center space-x-2">
                            <Play className="h-4 w-4" />
                            <span>Activate Timer</span>
                          </div>
                        )}
                      </Button>
                    </div>
                  )}

                  {dashboardData.counterStatus.isCompleted && !dashboardData.counterStatus.needsReset && (
                    <div className="text-center py-8">
                      <p className="text-muted-foreground">Counter completed for today. Come back tomorrow!</p>
                      <Badge variant="secondary">Completed</Badge>
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* Navigation Buttons */}
              <div className="grid grid-cols-3 md:grid-cols-6 gap-4">
                <Link href="/deposit">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-card border-border hover:bg-accent">
                    <CreditCard className="h-6 w-6 text-foreground" />
                    <span className="text-sm text-foreground">Deposit</span>
                  </Button>
                </Link>

                <Link href="/withdraw">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-card border-border hover:bg-accent">
                    <Banknote className="h-6 w-6 text-foreground" />
                    <span className="text-sm text-foreground">Withdraw</span>
                  </Button>
                </Link>

                <Link href="/profile">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-card border-border hover:bg-accent">
                    <User className="h-6 w-6 text-foreground" />
                    <span className="text-sm text-foreground">About</span>
                  </Button>
                </Link>

                <Link href="/withdrawal-history">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-card border-border hover:bg-accent">
                    <History className="h-6 w-6 text-foreground" />
                    <span className="text-sm text-foreground">Transactions</span>
                  </Button>
                </Link>

                <Link href="/plans">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-card border-border hover:bg-accent">
                    <BarChart3 className="h-6 w-6 text-foreground" />
                    <span className="text-sm text-foreground">Plans</span>
                  </Button>
                </Link>

                <Link href="/referrals">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-card border-border hover:bg-accent">
                    <Users className="h-6 w-6 text-foreground" />
                    <span className="text-sm text-foreground">Referrals</span>
                  </Button>
                </Link>
              </div>
            </>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}
