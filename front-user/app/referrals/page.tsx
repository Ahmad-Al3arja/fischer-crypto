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

import { useLanguage } from "@/contexts/LanguageContext"

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

// Default data in case API fails
const defaultReferralStats: ReferralStats = {
  totalReferrals: 0,
  activeReferrals: 0,
  totalEarnings: 0,
  referralCode: "DEMO123",
  referralLink: "https://fischer.com/register?ref=DEMO123"
}

const defaultReferralUsers: ReferralUser[] = [
  {
    id: 1,
    username: "demo_user1",
    fullName: "Demo User 1",
    joinedAt: "2024-01-15T10:30:00Z",
    status: "Active",
    totalDeposits: 150,
    commissionEarned: 18
  },
  {
    id: 2,
    username: "demo_user2", 
    fullName: "Demo User 2",
    joinedAt: "2024-01-20T14:45:00Z",
    status: "Active",
    totalDeposits: 300,
    commissionEarned: 36
  }
]

export default function ReferralsPage() {
  const [referralStats, setReferralStats] = useState<ReferralStats>(defaultReferralStats)
  const [referralUsers, setReferralUsers] = useState<ReferralUser[]>(defaultReferralUsers)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [apiError, setApiError] = useState("")
  const { toast } = useToast()
  const { t } = useLanguage()

  useEffect(() => {
    fetchReferralData()
  }, [])

  // Always generate the referral link using the current frontend origin
  const getReferralLink = () => {
    if (typeof window !== 'undefined' && referralStats.referralCode) {
      return `${window.location.origin}/register?ref=${referralStats.referralCode}`
    }
    return referralStats.referralLink
  }

  const fetchReferralData = async () => {
    try {
      setApiError("")
      const data = await apiService.getReferralStats().catch(err => {
        console.error("Error fetching referral stats:", err)
        return null
      })
      
      if (data) {
        // Handle the correct TeamStatsResponse structure
        const validStats = {
          totalReferrals: data.totalReferrals || 0,
          activeReferrals: data.directReferrals || 0, // Use direct referrals as active
          totalEarnings: data.totalReferralEarnings || 0,
          referralCode: data.referralCode || "DEMO123",
          referralLink: data.referralLink || "https://fischer.com/register?ref=DEMO123"
        }
        
        // Convert backend ReferralDetail to frontend ReferralUser
        const validUsers = Array.isArray(data.recentReferrals) ? 
          data.recentReferrals.map(ref => ({
            id: 0, // Backend doesn't provide ID
            username: ref.username || "",
            fullName: ref.username || "", // Use username as fullName since backend doesn't provide fullName
            joinedAt: ref.joinedAt || new Date().toISOString(),
            status: "Active", // Default status
            totalDeposits: ref.investmentAmount || 0,
            commissionEarned: ref.commissionEarned || 0
          })) : []
        
        setReferralStats(validStats)
        setReferralUsers(validUsers)
        setApiError("")
      } else {
        // Use default data if API fails
        setReferralStats(defaultReferralStats)
        setReferralUsers(defaultReferralUsers)
        setApiError(t('failed_to_load_referral_data'))
      }
    } catch (err: any) {
      console.error("Error in fetchReferralData:", err)
      setApiError(t('failed_to_load_referral_data_retry'))
      // Keep default data on error
      setReferralStats(defaultReferralStats)
      setReferralUsers(defaultReferralUsers)
    } finally {
      setLoading(false)
    }
  }

  const copyReferralCode = () => {
    if (referralStats?.referralCode) {
      navigator.clipboard.writeText(referralStats.referralCode)
      toast({
        title: t('copied'),
        description: t('referral_code_copied'),
      })
    }
  }

  const copyReferralLink = () => {
    if (referralStats?.referralLink) {
      navigator.clipboard.writeText(referralStats.referralLink)
      toast({
        title: t('copied'),
        description: t('referral_link_copied'),
      })
    }
  }

  const getStatusBadge = (status: string | undefined) => {
    // Handle undefined or null status
    if (!status) {
      return <Badge className="bg-green-100 text-green-800">{t('active')}</Badge>
    }

    switch (status.toLowerCase()) {
      case 'active':
        return <Badge className="bg-green-100 text-green-800">{t('active')}</Badge>
      case 'inactive':
        return <Badge variant="secondary">{t('inactive')}</Badge>
      case 'pending':
        return <Badge className="bg-yellow-100 text-yellow-800">{t('pending')}</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) {
      return "N/A"
    }
    
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    } catch (error) {
      return "Invalid Date"
    }
  }

  if (loading) {
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

        <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-2">
              <Users className="h-6 w-6" />
              <h1 className="text-3xl font-bold text-foreground">{t('referral_network')}</h1>
            </div>
            <Link href="/dashboard">
              <Button variant="outline" className="flex items-center space-x-2 bg-gradient-to-br bg-card border-border hover:from-gray-700 hover:to-custom-2e2e2e text-foreground">
                <ArrowLeft className="h-4 w-4" />
                <span>{t('back_to_dashboard')}</span>
              </Button>
            </Link>
          </div>

          {apiError && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{apiError}</AlertDescription>
            </Alert>
          )}

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {/* Referral Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
              <CardContent className="pt-4">
                <div className="text-center">
                  <UserPlus className="h-8 w-8 mx-auto mb-2 text-green-400" />
                  <p className="text-sm text-muted-foreground mb-1">{t('total_referrals')}</p>
                  <p className="text-2xl font-bold text-foreground">{referralStats.totalReferrals}</p>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
              <CardContent className="pt-4">
                <div className="text-center">
                  <TrendingUp className="h-8 w-8 mx-auto mb-2 text-green-600" />
                  <p className="text-sm text-muted-foreground mb-1">{t('direct_referrals')}</p>
                  <p className="text-2xl font-bold text-green-600">{referralStats.activeReferrals}</p>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
              <CardContent className="pt-4">
                <div className="text-center">
                  <Users className="h-8 w-8 mx-auto mb-2" style={{ color: '#2E2E2E' }} />
                  <p className="text-sm text-muted-foreground mb-1">{t('second_level')}</p>
                  <p className="text-2xl font-bold" style={{ color: '#2E2E2E' }}>{referralStats.totalReferrals - referralStats.activeReferrals}</p>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
              <CardContent className="pt-4">
                <div className="text-center">
                  <DollarSign className="h-8 w-8 mx-auto mb-2 text-green-400" />
                  <p className="text-sm text-muted-foreground mb-1">{t('total_earnings')}</p>
                  <p className="text-2xl font-bold text-green-400">${referralStats.totalEarnings.toFixed(2)}</p>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Referral Code Card */}
          <Card className="mb-6 bg-gradient-to-br bg-card border-border shadow-lg">
                <CardHeader>
              <CardTitle className="text-foreground">{t('your_referral_code')}</CardTitle>
              <CardDescription className="text-muted-foreground">
                {t('share_referral_code_description')}
              </CardDescription>
                </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label className="text-sm font-medium text-foreground">{t('referral_code')}</Label>
                    <div className="flex items-center space-x-2">
                    <div className="flex-1 p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg font-mono text-center text-foreground">
                      {referralStats.referralCode}
                    </div>
                    <Button size="sm" onClick={copyReferralCode} className="flex-shrink-0 bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-foreground">
                      <Copy className="h-4 w-4" />
                      </Button>
                  </div>
                    </div>

                <div className="space-y-2">
                  <Label className="text-sm font-medium text-foreground">{t('referral_link')}</Label>
                  <div className="flex items-center space-x-2">
                    <div className="flex-1 p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg font-mono text-sm truncate text-foreground">
                      {getReferralLink()}
                    </div>
                    <Button size="sm" onClick={() => {navigator.clipboard.writeText(getReferralLink()); toast({title: t('copied'), description: t('referral_link_copied')})}} className="flex-shrink-0 bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-foreground">
                      <Copy className="h-4 w-4" />
                    </Button>
                  </div>
                      </div>
                    </div>

              <div className="p-4 bg-gradient-to-r from-custom-2e2e2e to-gray-900 border border-gray-700 rounded-lg">
                <h4 className="font-semibold text-foreground mb-2">{t('how_it_works')}:</h4>
                <ul className="text-sm text-gray-300 space-y-1">
                  <li>• {t('share_referral_code_step')}</li>
                  <li>• {t('they_register_step')}</li>
                  <li>• {t('earn_12_percent_step')}</li>
                  <li>• {t('earn_6_percent_step')}</li>
                  <li>• {t('commissions_paid_instantly')}</li>
                </ul>
              </div>
                </CardContent>
              </Card>

          {/* Referral Users Table */}
          <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
                <CardHeader>
              <CardTitle className="text-foreground">{t('your_referrals')}</CardTitle>
              <CardDescription className="text-muted-foreground">
                {t('view_referrals_description')}
              </CardDescription>
                </CardHeader>
                <CardContent>
              {referralUsers.length === 0 ? (
                <div className="text-center py-8">
                  <Users className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                  <h3 className="text-lg font-semibold text-muted-foreground mb-2">{t('no_referrals_yet')}</h3>
                  <p className="text-muted-foreground mb-4">
                    {t('start_sharing_referral_code')}
                  </p>
                  <div className="flex items-center justify-center space-x-2">
                                       <span className="font-mono text-green-400 font-bold">{referralStats.referralCode}</span>
                   <Button size="sm" onClick={copyReferralCode} className="bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-foreground">
                      <Copy className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead className="text-foreground">{t('user')}</TableHead>
                        <TableHead className="text-foreground">{t('status')}</TableHead>
                        <TableHead className="text-foreground">{t('joined')}</TableHead>
                        <TableHead className="text-foreground">{t('total_deposits')}</TableHead>
                        <TableHead className="text-foreground">{t('commission_earned')}</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {referralUsers.map((user) => (
                        <TableRow key={user.id}>
                          <TableCell>
                            <div>
                              <p className="font-medium text-foreground">{user.fullName}</p>
                              <p className="text-sm text-muted-foreground">@{user.username}</p>
                            </div>
                          </TableCell>
                          <TableCell>
                            {getStatusBadge(user.status)}
                          </TableCell>
                          <TableCell className="text-sm text-muted-foreground">
                            {formatDate(user.joinedAt)}
                          </TableCell>
                          <TableCell className="font-semibold text-foreground">
                            ${user.totalDeposits.toFixed(2)}
                          </TableCell>
                          <TableCell className="font-semibold text-green-400">
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
        </div>
        
        
      </div>
    </ProtectedRoute>
  )
}
