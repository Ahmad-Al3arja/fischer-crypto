"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Users, ArrowLeft, Copy, UserPlus, DollarSign, TrendingUp } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { Label } from "@/components/ui/label"

interface ReferralStats {
  totalReferrals: number
  activeReferrals: number
  totalEarnings: number
  referralCode: string
  referralLink: string
}

interface ReferralUser {
  id: number
  username: string
  fullName: string
  joinedAt: string
  status: string
  totalDeposits: number
  commissionEarned: number
}

export default function ReferralsPage() {
  const [referralStats, setReferralStats] = useState<ReferralStats | null>(null)
  const [referralUsers, setReferralUsers] = useState<ReferralUser[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const { toast } = useToast()

  useEffect(() => {
    fetchReferralData()
  }, [])

  const fetchReferralData = async () => {
    try {
      const data = await apiService.getReferralStats()
      setReferralStats(data.stats)
      setReferralUsers(data.referrals || [])
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const copyReferralCode = () => {
    if (referralStats?.referralCode) {
      navigator.clipboard.writeText(referralStats.referralCode)
      toast({
        title: "Copied!",
        description: "Referral code copied to clipboard",
      })
    }
  }

  const copyReferralLink = () => {
    if (referralStats?.referralLink) {
      navigator.clipboard.writeText(referralStats.referralLink)
      toast({
        title: "Copied!",
        description: "Referral link copied to clipboard",
      })
    }
  }

  const getStatusBadge = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return <Badge className="bg-green-100 text-green-800">Active</Badge>
      case 'inactive':
        return <Badge variant="secondary">Inactive</Badge>
      case 'pending':
        return <Badge className="bg-yellow-100 text-yellow-800">Pending</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
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
              <Users className="h-6 w-6" />
              <h1 className="text-3xl font-bold text-foreground">Referral Network</h1>
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

          {referralStats && (
            <>
              {/* Referral Stats Cards */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                <Card>
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <UserPlus className="h-8 w-8 mx-auto mb-2 text-primary" />
                      <p className="text-sm text-muted-foreground mb-1">Total Referrals</p>
                      <p className="text-2xl font-bold text-foreground">{referralStats.totalReferrals}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <TrendingUp className="h-8 w-8 mx-auto mb-2 text-green-600" />
                      <p className="text-sm text-muted-foreground mb-1">Active Referrals</p>
                      <p className="text-2xl font-bold text-green-600">{referralStats.activeReferrals}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <DollarSign className="h-8 w-8 mx-auto mb-2 text-primary" />
                      <p className="text-sm text-muted-foreground mb-1">Total Earnings</p>
                      <p className="text-2xl font-bold text-primary">${referralStats.totalEarnings.toFixed(2)}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="pt-4">
                    <div className="text-center">
                      <div className="h-8 w-8 mx-auto mb-2 bg-primary/10 rounded-lg flex items-center justify-center">
                        <span className="text-primary font-bold text-sm">%</span>
                      </div>
                      <p className="text-sm text-muted-foreground mb-1">Commission Rate</p>
                      <p className="text-2xl font-bold text-foreground">5%</p>
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Referral Code Card */}
              <Card className="mb-6">
                <CardHeader>
                  <CardTitle>Your Referral Code</CardTitle>
                  <CardDescription>
                    Share your referral code with friends and earn 5% commission on their deposits
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="text-sm font-medium">Referral Code</Label>
                      <div className="flex items-center space-x-2">
                        <div className="flex-1 p-3 bg-muted rounded-lg font-mono text-center">
                          {referralStats.referralCode}
                        </div>
                        <Button size="sm" onClick={copyReferralCode} className="flex-shrink-0">
                          <Copy className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label className="text-sm font-medium">Referral Link</Label>
                      <div className="flex items-center space-x-2">
                        <div className="flex-1 p-3 bg-muted rounded-lg font-mono text-sm truncate">
                          {referralStats.referralLink}
                        </div>
                        <Button size="sm" onClick={copyReferralLink} className="flex-shrink-0">
                          <Copy className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </div>

                  <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
                    <h4 className="font-semibold text-blue-900 mb-2">How it works:</h4>
                    <ul className="text-sm text-blue-800 space-y-1">
                      <li>• Share your referral code with friends</li>
                      <li>• They register using your code</li>
                      <li>• You earn 5% commission on their deposits</li>
                      <li>• Commissions are paid instantly</li>
                    </ul>
                  </div>
                </CardContent>
              </Card>

              {/* Referral Users Table */}
              <Card>
                <CardHeader>
                  <CardTitle>Your Referrals</CardTitle>
                  <CardDescription>
                    View all users who registered using your referral code
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {referralUsers.length === 0 ? (
                    <div className="text-center py-8">
                      <Users className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                      <h3 className="text-lg font-semibold text-muted-foreground mb-2">No Referrals Yet</h3>
                      <p className="text-muted-foreground mb-4">
                        Start sharing your referral code to earn commissions!
                      </p>
                      <div className="flex items-center justify-center space-x-2">
                        <span className="font-mono text-primary font-bold">{referralStats.referralCode}</span>
                        <Button size="sm" onClick={copyReferralCode}>
                          <Copy className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <div className="overflow-x-auto">
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>User</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Joined</TableHead>
                            <TableHead>Total Deposits</TableHead>
                            <TableHead>Commission Earned</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {referralUsers.map((user) => (
                            <TableRow key={user.id}>
                              <TableCell>
                                <div>
                                  <p className="font-medium">{user.fullName}</p>
                                  <p className="text-sm text-muted-foreground">@{user.username}</p>
                                </div>
                              </TableCell>
                              <TableCell>
                                {getStatusBadge(user.status)}
                              </TableCell>
                              <TableCell className="text-sm text-muted-foreground">
                                {formatDate(user.joinedAt)}
                              </TableCell>
                              <TableCell className="font-semibold">
                                ${user.totalDeposits.toFixed(2)}
                              </TableCell>
                              <TableCell className="font-semibold text-primary">
                                ${user.commissionEarned.toFixed(2)}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </div>
                  )}
                </CardContent>
              </Card>
            </>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}
