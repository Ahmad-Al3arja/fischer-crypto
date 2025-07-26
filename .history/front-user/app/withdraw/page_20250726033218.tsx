"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { DollarSign, Wallet, ArrowLeft, AlertTriangle } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { WalletInfo, BalanceInfo } from "@/types"
import { useLanguage } from "@/contexts/LanguageContext"
import DeveloperSection from "@/components/DeveloperSection"

const defaultWalletInfo: WalletInfo = {
  usdtAddress: "",
  isSet: false
}

const defaultBalanceInfo: BalanceInfo = {
  totalBalance: 0,
  frozenBalance: 0,
  withdrawableBalance: 0,
  referralEarnings: 0
}

export default function WithdrawPage() {
  const [walletInfo, setWalletInfo] = useState<WalletInfo>(defaultWalletInfo)
  const [balanceInfo, setBalanceInfo] = useState<BalanceInfo>(defaultBalanceInfo)
  const [amount, setAmount] = useState("")
  const [walletAddress, setWalletAddress] = useState("")
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState("")
  const [apiError, setApiError] = useState("")
  const { toast } = useToast()
  const { t } = useLanguage()

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      setApiError("")
      
      // Fetch wallet data
      const walletData = await apiService.getWallet().catch(err => {
        console.error("Error fetching wallet:", err)
        return defaultWalletInfo
      })

      // Fetch balance data
      const balanceData = await apiService.getBalance().catch(err => {
        console.error("Error fetching balance:", err)
        return {
          totalBalance: 0,
          frozenBalance: 0,
          withdrawableBalance: 0,
          referralEarnings: 0
        }
      })
      
      // Set balance info
      setBalanceInfo({
        totalBalance: balanceData.totalBalance || 0,
        frozenBalance: balanceData.frozenBalance || 0,
        withdrawableBalance: balanceData.withdrawableBalance || 0,
        referralEarnings: balanceData.referralEarnings || 0
      })
      
      // Ensure we have valid wallet data
      const validWalletInfo = {
        usdtAddress: walletData?.usdtAddress || "",
        isSet: walletData?.isSet || false
      }
      setWalletInfo(validWalletInfo)
      
      // Set wallet address if available
      if (validWalletInfo.usdtAddress) {
        setWalletAddress(validWalletInfo.usdtAddress)
      }
    } catch (err: any) {
      console.error("Error in fetchData:", err)
      setApiError("Failed to load data. Please try again later.")
      setWalletInfo(defaultWalletInfo)
      setBalanceInfo(defaultBalanceInfo)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    const numAmount = parseFloat(amount)
    if (isNaN(numAmount) || numAmount <= 0) {
      setError("Please enter a valid amount")
      return
    }

    if (numAmount < 10) {
      setError("Minimum withdrawal amount is $10")
      return
    }

    if (numAmount > balanceInfo.withdrawableBalance) {
      setError(`Insufficient withdrawable balance. Available: $${balanceInfo.withdrawableBalance.toFixed(2)}`)
      return
    }

    if (!walletAddress.trim()) {
      setError("Please enter your USDT wallet address")
      return
    }

    setSubmitting(true)
    setError("")

    try {
      await apiService.createWithdrawal({
        amount: numAmount,
        walletAddress: walletAddress.trim()
      })
      
      toast({
        title: t('success'),
        description: "Withdrawal request submitted successfully",
      })
      
      // Reset form and refresh balance
      setAmount("")
      fetchData()
    } catch (err: any) {
      console.error("Error creating withdrawal:", err)
      setError(err.message || "Failed to submit withdrawal request")
    } finally {
      setSubmitting(false)
    }
  }

  const handleSaveWallet = async () => {
    if (!walletAddress.trim()) {
      setError("Please enter your USDT wallet address")
      return
    }

    setSubmitting(true)
    setError("")

    try {
      await apiService.saveWallet({
        usdtAddress: walletAddress.trim()
      })
      
      setWalletInfo({
        usdtAddress: walletAddress.trim(),
        isSet: true
      })
      
      toast({
        title: t('success'),
        description: "Wallet address saved successfully",
      })
    } catch (err: any) {
      console.error("Error saving wallet:", err)
      setError(err.message || "Failed to save wallet address")
    } finally {
      setSubmitting(false)
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

        <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-3xl font-bold text-foreground">{t('withdraw')}</h1>
            <Link href="/dashboard">
              <Button variant="outline" className="bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 hover:from-gray-700 hover:to-gray-800 text-white">
                <ArrowLeft className="h-4 w-4 mr-2" />
                {t('back_to_dashboard')}
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

          {/* Balance Information */}
          <Card className="mb-6 bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 shadow-lg">
            <CardHeader>
              <CardTitle className="flex items-center space-x-2 text-foreground">
                <DollarSign className="h-5 w-5" />
                <span>Account Balance</span>
              </CardTitle>
              <CardDescription className="text-muted-foreground">
                Your current account balance information
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="bg-gradient-to-r from-green-900/50 to-green-800/50 p-4 rounded-lg border border-green-700">
                  <div className="text-sm text-green-300 font-medium">Withdrawable Balance</div>
                  <div className="text-2xl font-bold text-green-400">
                    ${balanceInfo.withdrawableBalance.toFixed(2)}
                  </div>
                  <div className="text-xs text-green-200 mt-1">Available for withdrawal</div>
                </div>
                
                <div className="bg-gradient-to-r from-blue-900/50 to-blue-800/50 p-4 rounded-lg border border-blue-700">
                  <div className="text-sm text-blue-300 font-medium">Frozen Balance</div>
                  <div className="text-2xl font-bold text-blue-400">
                    ${balanceInfo.frozenBalance.toFixed(2)}
                  </div>
                  <div className="text-xs text-blue-200 mt-1">Investment capital (locked)</div>
                </div>
                
                <div className="bg-gradient-to-r from-purple-900/50 to-purple-800/50 p-4 rounded-lg border border-purple-700">
                  <div className="text-sm text-purple-300 font-medium">Total Balance</div>
                  <div className="text-2xl font-bold text-purple-400">
                    ${balanceInfo.totalBalance.toFixed(2)}
                  </div>
                  <div className="text-xs text-purple-200 mt-1">Total account value</div>
                </div>
                
                <div className="bg-gradient-to-r from-yellow-900/50 to-yellow-800/50 p-4 rounded-lg border border-yellow-700">
                  <div className="text-sm text-yellow-300 font-medium">Referral Earnings</div>
                  <div className="text-2xl font-bold text-yellow-400">
                    ${balanceInfo.referralEarnings.toFixed(2)}
                  </div>
                  <div className="text-xs text-yellow-200 mt-1">From referrals</div>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Withdrawal Form */}
            <Card className="bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 shadow-lg">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <DollarSign className="h-5 w-5" />
                  <span>{t('withdraw_funds')}</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">{t('withdraw_earnings')}</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="amount" className="text-foreground">{t('amount_usd')}</Label>
                    <div className="relative">
                      <DollarSign className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                      <Input
                        id="amount"
                        type="number"
                        placeholder={t('enter_withdrawal_amount')}
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        className="pl-10 pr-16 bg-gradient-to-r from-gray-800 to-gray-900 border-gray-700 text-white placeholder:text-gray-400"
                        required
                        max={balanceInfo.withdrawableBalance}
                        step="0.01"
                      />
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        className="absolute right-1 top-1 h-6 px-2 text-xs bg-gradient-to-r from-gray-700 to-gray-800 border-gray-600 hover:from-gray-600 hover:to-gray-700 text-white"
                        onClick={() => setAmount(balanceInfo.withdrawableBalance.toString())}
                        disabled={balanceInfo.withdrawableBalance <= 0}
                      >
                        Max
                      </Button>
                    </div>
                    <div className="flex justify-between text-xs text-muted-foreground">
                      <span>{t('min_max_withdrawal')}</span>
                      <span>Max: ${balanceInfo.withdrawableBalance.toFixed(2)}</span>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="walletAddress" className="text-foreground">{t('usdt_wallet_address')}</Label>
                    <Input
                      id="walletAddress"
                      type="text"
                      placeholder={t('usdt_wallet_address')}
                      value={walletAddress}
                      onChange={(e) => setWalletAddress(e.target.value)}
                      className="bg-gradient-to-r from-gray-800 to-gray-900 border-gray-700 text-white placeholder:text-gray-400"
                      required
                    />
                  </div>

                  <Button 
                    type="submit" 
                    disabled={submitting}
                    className="w-full bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 hover:from-gray-700 hover:to-gray-800 text-white"
                  >
                    {submitting ? (
                      <div className="flex items-center space-x-2">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                        <span>{t('processing')}</span>
                      </div>
                    ) : (
                      t('submit_withdrawal')
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Wallet Management */}
            <Card className="bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 shadow-lg">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <Wallet className="h-5 w-5" />
                  <span>{t('wallet_management')}</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">{t('save_wallet_address')}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {walletInfo.isSet ? (
                  <div className="space-y-2">
                    <Label className="text-foreground">{t('usdt_wallet_address')}</Label>
                    <div className="p-3 bg-gradient-to-r from-gray-800 to-gray-900 rounded-lg">
                      <p className="text-sm text-muted-foreground break-all">{walletInfo.usdtAddress}</p>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Your wallet address is saved. You can update it below.
                    </p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    <Label className="text-foreground">{t('no_wallet_address')}</Label>
                    <p className="text-sm text-muted-foreground">
                      {t('please_save_wallet')}
                    </p>
                  </div>
                )}

                <div className="space-y-2">
                  <Label htmlFor="newWalletAddress" className="text-foreground">{t('new_wallet_address')}</Label>
                  <Input
                    id="newWalletAddress"
                    type="text"
                    placeholder={t('usdt_wallet_address')}
                    value={walletAddress}
                    onChange={(e) => setWalletAddress(e.target.value)}
                    className="bg-gradient-to-r from-gray-800 to-gray-900 border-gray-700 text-white placeholder:text-gray-400"
                  />
                </div>

                <Button 
                  onClick={handleSaveWallet}
                  className="w-full bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 hover:from-gray-700 hover:to-gray-800 text-white" 
                  disabled={submitting}
                >
                  {submitting ? (
                    <div className="flex items-center space-x-2">
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                      <span>{t('save')}</span>
                    </div>
                  ) : (
                    t('save_wallet_address_btn')
                  )}
                </Button>
              </CardContent>
            </Card>

            {/* Important Notes */}
            <Card className="lg:col-span-2 bg-gradient-to-br from-gray-800 to-gray-900 border-gray-700 shadow-lg">
              <CardHeader>
                <CardTitle className="text-foreground">{t('important_notes')}</CardTitle>
              </CardHeader>
              <CardContent>
                <Alert className="bg-gradient-to-r from-gray-800 to-gray-900 border-gray-700 text-white">
                  <AlertTriangle className="h-4 w-4 text-yellow-400" />
                  <AlertDescription className="text-white">
                    <ul className="space-y-2">
                      <li>• {t('min_withdrawal')}</li>
                      <li>• {t('max_withdrawal')}</li>
                      <li>• {t('withdrawal_processing_time')}</li>
                      <li>• {t('only_usdt_accepted')}</li>
                      <li>• {t('verify_wallet_address')}</li>
                    </ul>
                  </AlertDescription>
                </Alert>
              </CardContent>
            </Card>
          </div>
        </div>
        
        {/* Developer Section */}
        <DeveloperSection />
      </div>
    </ProtectedRoute>
  )
}
