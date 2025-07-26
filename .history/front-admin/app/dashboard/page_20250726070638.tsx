"use client"

import { useEffect, useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { StatsCard } from "@/components/stats-card"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { apiClient } from "@/lib/api"
import { Users, CreditCard, Banknote, TrendingUp, DollarSign, UserCheck } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"

interface DashboardStats {
  totalUsers: number
  activeUsers: number
  pendingDeposits: number
  pendingWithdrawals: number
  totalDeposited: string
  totalWithdrawn: string
}

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    try {
      setLoading(true)
      const data = await apiClient.getDashboardStats()
      setStats(data)
    } catch (err: any) {
      setError(err.message || "Failed to load dashboard stats")
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
        <DashboardHeader title="Dashboard" description="Overview of your investment platform" />
        <div className="grid gap-3 sm:gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {Array.from({ length: 6 }).map((_, i) => (
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

  if (error) {
    return (
      <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
        <DashboardHeader title="Dashboard" description="Overview of your investment platform" />
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
      <DashboardHeader title="Dashboard" description="Overview of your investment platform" />

      {/* Stats Cards */}
      <div className="grid gap-3 sm:gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        <StatsCard title="Total Users" value={stats?.totalUsers || 0} description="Registered users" icon={Users} />
        <StatsCard
          title="Active Users"
          value={stats?.activeUsers || 0}
          description="Currently active"
          icon={UserCheck}
        />
        <StatsCard
          title="Pending Deposits"
          value={stats?.pendingDeposits || 0}
          description="Awaiting approval"
          icon={CreditCard}
        />
        <StatsCard
          title="Pending Withdrawals"
          value={stats?.pendingWithdrawals || 0}
          description="Awaiting approval"
          icon={Banknote}
        />
        <StatsCard
          title="Total Deposited"
          value={`$${Number.parseFloat(stats?.totalDeposited || "0").toLocaleString()}`}
          description="All time deposits"
          icon={TrendingUp}
        />
        <StatsCard
          title="Total Withdrawn"
          value={`$${Number.parseFloat(stats?.totalWithdrawn || "0").toLocaleString()}`}
          description="All time withdrawals"
          icon={DollarSign}
        />
      </div>

      {/* Recent Activity and Quick Actions */}
      <div className="grid gap-4 grid-cols-1 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg sm:text-xl">Recent Activity</CardTitle>
            <CardDescription>Latest platform activities</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 sm:space-y-4">
              <div className="flex items-start space-x-3 sm:space-x-4">
                <div className="w-2 h-2 bg-green-500 rounded-full mt-2 flex-shrink-0"></div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium">New user registered</p>
                  <p className="text-xs text-muted-foreground">2 minutes ago</p>
                </div>
              </div>
              <div className="flex items-start space-x-3 sm:space-x-4">
                <div className="w-2 h-2 bg-blue-500 rounded-full mt-2 flex-shrink-0"></div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium">Deposit approved</p>
                  <p className="text-xs text-muted-foreground">5 minutes ago</p>
                </div>
              </div>
              <div className="flex items-start space-x-3 sm:space-x-4">
                <div className="w-2 h-2 bg-yellow-500 rounded-full mt-2 flex-shrink-0"></div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium">Withdrawal pending</p>
                  <p className="text-xs text-muted-foreground">10 minutes ago</p>
                </div>
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
              <button className="w-full text-left p-3 rounded-lg hover:bg-muted transition-colors">
                <p className="font-medium text-sm sm:text-base">Review Pending Deposits</p>
                <p className="text-xs sm:text-sm text-muted-foreground">{stats?.pendingDeposits || 0} pending</p>
              </button>
              <button className="w-full text-left p-3 rounded-lg hover:bg-muted transition-colors">
                <p className="font-medium text-sm sm:text-base">Review Pending Withdrawals</p>
                <p className="text-xs sm:text-sm text-muted-foreground">{stats?.pendingWithdrawals || 0} pending</p>
              </button>
              <button className="w-full text-left p-3 rounded-lg hover:bg-muted transition-colors">
                <p className="font-medium text-sm sm:text-base">Manage Users</p>
                <p className="text-xs sm:text-sm text-muted-foreground">{stats?.totalUsers || 0} total users</p>
              </button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
