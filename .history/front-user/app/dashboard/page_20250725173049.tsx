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
  const { toast } = useToast()

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
      toast({
        title: "Success!",
        description: "Daily timer activated successfully",
      })
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
      toast({
        title: "Success!",
        description: "Daily profit claimed successfully",
      })
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
      toast({
        title: "Copied!",
        description: "Referral code copied to clipboard",
      })
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
                </CardHeader>
                <CardContent>
                  <div className="flex flex-col items-center space-y-4">
                    <div className="w-32 h-32 rounded-full border-4 border-primary flex items-center justify-center bg-card">
                      {dashboardData.counterStatus.isActive && dashboardData.counterStatus.remainingSeconds > 0 ? (
                        <>
                          <div className="text-center">
                            <div className="text-2xl font-bold text-foreground font-mono">
                              {formatTime(dashboardData.counterStatus.remainingSeconds)}
                            </div>
                            <div className="text-xs text-muted-foreground">Remaining Today</div>
                          </div>
                        </>
                      ) : (
                        <div className="text-center">
                          <div className="text-2xl font-bold text-muted-foreground">00:00:00</div>
                          <div className="text-xs text-muted-foreground">Inactive</div>
                        </div>
                      )}
                    </div>

                    {!dashboardData.counterStatus.isActive && !dashboardData.counterStatus.isCompleted && (
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

                    {dashboardData.counterStatus.isActive && dashboardData.counterStatus.remainingSeconds === 0 && (
                      <Button
                        onClick={handleCompleteCounter}
                        disabled={counterLoading}
                        className="bg-primary hover:bg-primary/90 text-primary-foreground w-full"
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
                    <TrendingUp className="h-6 w-6" />
                    <span className="text-sm font-medium">Deposit</span>
                  </Button>
                </Link>

                <Link href="/withdraw">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <Banknote className="h-6 w-6" />
                    <span className="text-sm font-medium">Withdraw</span>
                  </Button>
                </Link>
              </div>

              <div className="grid grid-cols-2 gap-4 mb-6">
                <Link href="/withdrawal-history">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <History className="h-6 w-6" />
                    <span className="text-sm font-medium">Transactions</span>
                  </Button>
                </Link>

                <Link href="/referrals">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <Users className="h-6 w-6" />
                    <span className="text-sm font-medium">Network</span>
                  </Button>
                </Link>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <Link href="/profile">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <ProfileIcon className="h-6 w-6" />
                    <span className="text-sm font-medium">About</span>
                  </Button>
                </Link>

                <Link href="/plans">
                  <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2 bg-card border-border hover:bg-muted">
                    <BarChart3 className="h-6 w-6" />
                    <span className="text-sm font-medium">Investment</span>
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
              <BarChart3 className="h-5 w-5 text-muted-foreground" />
              <span className="text-xs text-muted-foreground">Investment</span>
            </Link>
            <Link href="/wallet" className="flex flex-col items-center space-y-1 p-2">
              <Wallet className="h-5 w-5 text-muted-foreground" />
              <span className="text-xs text-muted-foreground">Wallet</span>
            </Link>
            <Link href="/profile" className="flex flex-col items-center space-y-1 p-2">
              <ProfileIcon className="h-5 w-5 text-muted-foreground" />
              <span className="text-xs text-muted-foreground">Profile</span>
            </Link>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
} 