"use client"

import { useEffect, useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"
import { apiClient } from "@/lib/api"
import { 
  TrendingUp, 
  TrendingDown, 
  Users, 
  DollarSign, 
  CreditCard, 
  Banknote,
  Activity,
  BarChart3
} from "lucide-react"

interface AnalyticsData {
  totalUsers: number
  activeUsers: number
  totalDeposited: string
  totalWithdrawn: string
  pendingDeposits: number
  pendingWithdrawals: number
  monthlyStats?: {
    newUsers: number
    totalDeposits: string
    totalWithdrawals: string
  }
}

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<AnalyticsData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    loadAnalytics()
  }, [])

  const loadAnalytics = async () => {
    try {
      setLoading(true)
      const data = await apiClient.getDashboardStats()
      setAnalytics(data)
    } catch (err: any) {
      setError(err.message || "Failed to load analytics")
    } finally {
      setLoading(false)
    }
  }

  const formatCurrency = (amount: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(parseFloat(amount))
  }

  if (loading) {
    return (
      <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
        <DashboardHeader title="Analytics" description="Platform statistics and insights" />
        <div className="grid gap-3 sm:gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <Card key={i}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <Skeleton className="h-4 w-20 sm:w-24" />
                <Skeleton className="h-4 w-4" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-6 sm:h-8 w-12 sm:w-16 mb-2" />
                <Skeleton className="h-3 w-24 sm:w-32" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
      <DashboardHeader title="Analytics" description="Platform statistics and insights" />

      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Key Metrics */}
      <div className="grid gap-3 sm:gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs sm:text-sm font-medium">Total Users</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-xl sm:text-2xl font-bold">{analytics?.totalUsers || 0}</div>
            <p className="text-xs text-muted-foreground">
              +{analytics?.monthlyStats?.newUsers || 0} this month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs sm:text-sm font-medium">Active Users</CardTitle>
            <Activity className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-xl sm:text-2xl font-bold">{analytics?.activeUsers || 0}</div>
            <p className="text-xs text-muted-foreground">
              Currently active
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs sm:text-sm font-medium">Total Deposited</CardTitle>
            <TrendingUp className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-xl sm:text-2xl font-bold">
              {analytics?.totalDeposited ? formatCurrency(analytics.totalDeposited) : '$0'}
            </div>
            <p className="text-xs text-muted-foreground">
              All time deposits
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs sm:text-sm font-medium">Total Withdrawn</CardTitle>
            <TrendingDown className="h-4 w-4 text-red-500" />
          </CardHeader>
          <CardContent>
            <div className="text-xl sm:text-2xl font-bold">
              {analytics?.totalWithdrawn ? formatCurrency(analytics.totalWithdrawn) : '$0'}
            </div>
            <p className="text-xs text-muted-foreground">
              All time withdrawals
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Pending Transactions */}
      <div className="grid gap-3 sm:gap-4 grid-cols-1 sm:grid-cols-2">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs sm:text-sm font-medium">Pending Deposits</CardTitle>
            <CreditCard className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-xl sm:text-2xl font-bold">{analytics?.pendingDeposits || 0}</div>
            <p className="text-xs text-muted-foreground">
              Awaiting approval
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs sm:text-sm font-medium">Pending Withdrawals</CardTitle>
            <Banknote className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-xl sm:text-2xl font-bold">{analytics?.pendingWithdrawals || 0}</div>
            <p className="text-xs text-muted-foreground">
              Awaiting approval
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Platform Overview */}
      <div className="grid gap-4 grid-cols-1 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg sm:text-xl">Platform Overview</CardTitle>
            <CardDescription>Key performance indicators</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 sm:space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-xs sm:text-sm font-medium">User Growth Rate</span>
                <span className="text-xs sm:text-sm text-green-600">
                  +{analytics?.monthlyStats?.newUsers || 0} users/month
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-xs sm:text-sm font-medium">Deposit Volume</span>
                <span className="text-xs sm:text-sm text-green-600">
                  {analytics?.monthlyStats?.totalDeposits ? formatCurrency(analytics.monthlyStats.totalDeposits) : '$0'}/month
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-xs sm:text-sm font-medium">Withdrawal Volume</span>
                <span className="text-xs sm:text-sm text-red-600">
                  {analytics?.monthlyStats?.totalWithdrawals ? formatCurrency(analytics.monthlyStats.totalWithdrawals) : '$0'}/month
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-xs sm:text-sm font-medium">Active Rate</span>
                <span className="text-xs sm:text-sm text-blue-600">
                  {analytics?.totalUsers ? Math.round((analytics.activeUsers / analytics.totalUsers) * 100) : 0}%
                </span>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg sm:text-xl">Quick Actions</CardTitle>
            <CardDescription>Common administrative tasks</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2 sm:space-y-3">
              <div className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
                <div>
                  <p className="font-medium text-sm sm:text-base">Review Deposits</p>
                  <p className="text-xs sm:text-sm text-muted-foreground">{analytics?.pendingDeposits || 0} pending</p>
                </div>
                <CreditCard className="h-4 w-4 text-muted-foreground" />
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
                <div>
                  <p className="font-medium text-sm sm:text-base">Review Withdrawals</p>
                  <p className="text-xs sm:text-sm text-muted-foreground">{analytics?.pendingWithdrawals || 0} pending</p>
                </div>
                <Banknote className="h-4 w-4 text-muted-foreground" />
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
                <div>
                  <p className="font-medium text-sm sm:text-base">User Management</p>
                  <p className="text-xs sm:text-sm text-muted-foreground">{analytics?.totalUsers || 0} total users</p>
                </div>
                <Users className="h-4 w-4 text-muted-foreground" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Placeholder for Charts */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg sm:text-xl">Revenue Trends</CardTitle>
          <CardDescription>Monthly deposit and withdrawal trends</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center h-48 sm:h-64 bg-muted/20 rounded-lg">
            <div className="text-center">
              <BarChart3 className="h-8 w-8 sm:h-12 sm:w-12 text-muted-foreground mx-auto mb-2 sm:mb-4" />
              <p className="text-muted-foreground text-sm sm:text-base">Chart visualization coming soon</p>
              <p className="text-xs sm:text-sm text-muted-foreground">
                This will show detailed revenue and user growth charts
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
} 