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
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">🏠 Dashboard</h1>
            <p className="text-gray-600">Welcome back to your investment platform</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}



          {dashboardData && (
            <>
              {/* User Info Card */}
              <Card className="mb-6">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <User className="h-5 w-5" />
                    <span>Account Information</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <p className="text-sm text-gray-500">Full Name</p>
                      <p className="font-medium">{dashboardData.fullName}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Username</p>
                      <p className="font-medium">{dashboardData.username}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Phone</p>
                      <p className="font-medium">{dashboardData.phoneNumber}</p>
                    </div>
                  </div>
                  <div className="mt-4">
                    <p className="text-sm text-gray-500">Current Plan</p>
                    <Badge variant="secondary" className="mt-1">
                      {dashboardData.currentPlanName}
                    </Badge>
                  </div>
                </CardContent>
              </Card>

              {/* Balance Cards */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Total Balance 💰</CardTitle>
                    <DollarSign className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">${dashboardData.totalBalance.toFixed(2)}</div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Total Profits 📈</CardTitle>
                    <TrendingUp className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold text-green-600">${dashboardData.totalProfits.toFixed(2)}</div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Daily Profit</CardTitle>
                    <Clock className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold text-blue-600">${dashboardData.dailyProfit.toFixed(2)}</div>
                  </CardContent>
                </Card>
              </div>

              {/* Daily Counter Card */}
              <Card className="mb-6">
                <CardHeader>
                  <CardTitle>Daily Counter Status</CardTitle>
                  <CardDescription>Manage your daily profit counter</CardDescription>
                </CardHeader>
                <CardContent>
                  {!dashboardData.counterStatus.isActive && !dashboardData.counterStatus.isCompleted && (
                    <div className="text-center py-4">
                      <p className="text-gray-600 mb-4">Counter is not active</p>
                      <Button onClick={handleActivateCounter} disabled={counterLoading}>
                        {counterLoading ? (
                          <div className="flex items-center space-x-2">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                            <span>Activating...</span>
                          </div>
                        ) : (
                          <div className="flex items-center space-x-2">
                            <Play className="h-4 w-4" />
                            <span>Activate Counter</span>
                          </div>
                        )}
                      </Button>
                    </div>
                  )}

                  {dashboardData.counterStatus.isActive && dashboardData.counterStatus.remainingSeconds > 0 && (
                    <div className="text-center py-4">
                      <p className="text-gray-600 mb-2">Counter is running</p>
                      <div className="text-3xl font-mono font-bold text-blue-600 mb-4">
                        {formatTime(dashboardData.counterStatus.remainingSeconds)}
                      </div>
                      <Badge variant="outline">Active</Badge>
                    </div>
                  )}

                  {dashboardData.counterStatus.isActive && dashboardData.counterStatus.remainingSeconds === 0 && (
                    <div className="text-center py-4">
                      <p className="text-green-600 mb-4">Counter completed! Claim your profit.</p>
                      <Button
                        onClick={handleCompleteCounter}
                        disabled={counterLoading}
                        className="bg-green-600 hover:bg-green-700"
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
                    <div className="text-center py-4">
                      <p className="text-gray-600 mb-4">Profit claimed! You can activate the counter again.</p>
                      <Button onClick={handleActivateCounter} disabled={counterLoading}>
                        {counterLoading ? (
                          <div className="flex items-center space-x-2">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                            <span>Activating...</span>
                          </div>
                        ) : (
                          <div className="flex items-center space-x-2">
                            <Play className="h-4 w-4" />
                            <span>Activate Counter</span>
                          </div>
                        )}
                      </Button>
                    </div>
                  )}

                  {dashboardData.counterStatus.isCompleted && !dashboardData.counterStatus.needsReset && (
                    <div className="text-center py-4">
                      <p className="text-gray-600">Counter completed for today. Come back tomorrow!</p>
                      <Badge variant="secondary">Completed</Badge>
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* Navigation Buttons */}
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                <Link href="/profile">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-transparent">
                    <User className="h-6 w-6" />
                    <span className="text-sm">Profile 👤</span>
                  </Button>
                </Link>

                <Link href="/deposit">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-transparent">
                    <CreditCard className="h-6 w-6" />
                    <span className="text-sm">Deposit 💸</span>
                  </Button>
                </Link>

                <Link href="/withdraw">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-transparent">
                    <Banknote className="h-6 w-6" />
                    <span className="text-sm">Withdraw 💳</span>
                  </Button>
                </Link>

                <Link href="/withdrawal-history">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-transparent">
                    <History className="h-6 w-6" />
                    <span className="text-sm">History 📜</span>
                  </Button>
                </Link>

                <Link href="/plans">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-transparent">
                    <BarChart3 className="h-6 w-6" />
                    <span className="text-sm">Plans 📊</span>
                  </Button>
                </Link>

                <Link href="/referrals">
                  <Button variant="outline" className="w-full h-20 flex flex-col items-center space-y-2 bg-transparent">
                    <Users className="h-6 w-6" />
                    <span className="text-sm">Referrals 👥</span>
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
