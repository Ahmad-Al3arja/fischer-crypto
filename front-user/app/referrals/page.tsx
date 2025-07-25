"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Copy, Users, DollarSign, LinkIcon, Share2 } from "lucide-react"
import { useToast } from "@/hooks/use-toast"

interface ReferralStats {
  totalDirectReferrals: number
  totalSecondLevelReferrals: number
  totalReferralEarnings: number
  referralLink: string
}

export default function ReferralsPage() {
  const [referralStats, setReferralStats] = useState<ReferralStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const { toast } = useToast()

  useEffect(() => {
    fetchReferralStats()
  }, [])

  const fetchReferralStats = async () => {
    try {
      const data = await apiService.getReferralStats()
      setReferralStats(data)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
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

  const shareReferralLink = async () => {
    if (referralStats?.referralLink && navigator.share) {
      try {
        await navigator.share({
          title: "Join Investment Platform",
          text: "Start your investment journey with us!",
          url: referralStats.referralLink,
        })
      } catch (err) {
        // Fallback to copy
        copyReferralLink()
      }
    } else {
      copyReferralLink()
    }
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

        <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">👥 Referral Stats</h1>
            <p className="text-gray-600">Track your referral network and earnings</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {referralStats && (
            <div className="space-y-6">
              {/* Referral Statistics */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Direct Referrals</CardTitle>
                    <Users className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold text-blue-600">{referralStats.totalDirectReferrals}</div>
                    <p className="text-xs text-muted-foreground">People you directly referred</p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Second Level Referrals</CardTitle>
                    <Users className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold text-purple-600">{referralStats.totalSecondLevelReferrals}</div>
                    <p className="text-xs text-muted-foreground">People referred by your referrals</p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Total Earnings</CardTitle>
                    <DollarSign className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold text-green-600">
                      ${referralStats.totalReferralEarnings.toFixed(2)}
                    </div>
                    <p className="text-xs text-muted-foreground">Total referral commissions earned</p>
                  </CardContent>
                </Card>
              </div>

              {/* Referral Link */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <LinkIcon className="h-5 w-5" />
                    <span>Your Referral Link</span>
                  </CardTitle>
                  <CardDescription>Share this link to earn commissions from new users</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <div className="flex-1 p-3 bg-gray-50 rounded-lg border">
                        <p className="text-sm text-gray-700 break-all font-mono">{referralStats.referralLink}</p>
                      </div>
                      <Button onClick={copyReferralLink} size="sm" variant="outline">
                        <Copy className="h-4 w-4" />
                      </Button>
                    </div>

                    <div className="flex space-x-2">
                      <Button onClick={copyReferralLink} className="flex-1">
                        <Copy className="h-4 w-4 mr-2" />📋 Copy Link
                      </Button>
                      <Button onClick={shareReferralLink} variant="outline" className="flex-1 bg-transparent">
                        <Share2 className="h-4 w-4 mr-2" />
                        Share Link
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Commission Structure */}
              <Card>
                <CardHeader>
                  <CardTitle>💰 Commission Structure</CardTitle>
                  <CardDescription>How you earn from referrals</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                      <div className="flex items-center space-x-3 mb-3">
                        <div className="p-2 bg-blue-600 rounded-full">
                          <Users className="h-4 w-4 text-white" />
                        </div>
                        <div>
                          <h4 className="font-semibold text-blue-900">Direct Referrals</h4>
                          <p className="text-sm text-blue-700">Level 1 Commission</p>
                        </div>
                      </div>
                      <div className="text-2xl font-bold text-blue-900 mb-2">12%</div>
                      <p className="text-sm text-blue-700">
                        Earn 12% commission from every deposit made by users you directly refer
                      </p>
                    </div>

                    <div className="p-4 bg-purple-50 rounded-lg border border-purple-200">
                      <div className="flex items-center space-x-3 mb-3">
                        <div className="p-2 bg-purple-600 rounded-full">
                          <Users className="h-4 w-4 text-white" />
                        </div>
                        <div>
                          <h4 className="font-semibold text-purple-900">Second Level</h4>
                          <p className="text-sm text-purple-700">Level 2 Commission</p>
                        </div>
                      </div>
                      <div className="text-2xl font-bold text-purple-900 mb-2">6%</div>
                      <p className="text-sm text-purple-700">
                        Earn 6% commission from deposits made by users referred by your referrals
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* How It Works */}
              <Card>
                <CardHeader>
                  <CardTitle>🚀 How Referral System Works</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-start space-x-3">
                      <div className="flex-shrink-0 w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                        1
                      </div>
                      <div>
                        <h4 className="font-medium">Share Your Link</h4>
                        <p className="text-sm text-gray-600">
                          Share your unique referral link with friends, family, or on social media
                        </p>
                      </div>
                    </div>

                    <div className="flex items-start space-x-3">
                      <div className="flex-shrink-0 w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                        2
                      </div>
                      <div>
                        <h4 className="font-medium">Users Register</h4>
                        <p className="text-sm text-gray-600">
                          When someone uses your link to register, they become your referral
                        </p>
                      </div>
                    </div>

                    <div className="flex items-start space-x-3">
                      <div className="flex-shrink-0 w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                        3
                      </div>
                      <div>
                        <h4 className="font-medium">They Make Deposits</h4>
                        <p className="text-sm text-gray-600">
                          When your referrals make their first deposit, you earn commission
                        </p>
                      </div>
                    </div>

                    <div className="flex items-start space-x-3">
                      <div className="flex-shrink-0 w-6 h-6 bg-green-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                        4
                      </div>
                      <div>
                        <h4 className="font-medium">Earn Commissions</h4>
                        <p className="text-sm text-gray-600">
                          Commissions are automatically added to your balance and can be withdrawn
                        </p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Tips for Success */}
              <Card className="bg-gradient-to-r from-green-50 to-blue-50 border-green-200">
                <CardHeader>
                  <CardTitle className="text-green-800">💡 Tips for Referral Success</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <h4 className="font-medium text-green-800 mb-2">Effective Sharing</h4>
                      <ul className="space-y-1 text-sm text-green-700">
                        <li>• Share on social media platforms</li>
                        <li>• Tell friends and family about the opportunity</li>
                        <li>• Join investment communities and forums</li>
                        <li>• Create content about your investment journey</li>
                      </ul>
                    </div>
                    <div>
                      <h4 className="font-medium text-green-800 mb-2">Building Trust</h4>
                      <ul className="space-y-1 text-sm text-green-700">
                        <li>• Be transparent about the investment</li>
                        <li>• Share your own positive experiences</li>
                        <li>• Help new users understand the platform</li>
                        <li>• Provide support to your referrals</li>
                      </ul>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}
