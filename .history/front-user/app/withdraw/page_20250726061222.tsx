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
              <Button variant="outline" className="bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 hover:from-gray-700 hover:to-custom-2e2e2e text-white">
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
          <Card className="mb-6 bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
            <CardHeader>
              <CardTitle className="flex items-center space-x-2 text-foreground">
                <DollarSign className="h-5 w-5" />
                <span>{t('account_balance')}</span>
              </CardTitle>
              <CardDescription className="text-muted-foreground">
                {t('account_balance_description')}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="bg-gradient-to-r from-green-900/50 to-green-800/50 p-4 rounded-lg border border-green-700 hover:border-green-600 transition-colors">
                  <div className="text-sm text-green-300 font-medium">{t('withdrawable_balance')}</div>
                  <div className="text-2xl font-bold text-green-400">
                    ${balanceInfo.withdrawableBalance.toFixed(2)}
                  </div>
                  <div className="text-xs text-green-200 mt-1">{t('available_for_withdrawal')}</div>
                </div>
                
                <div className="bg-gradient-to-r from-gray-900/50 to-custom-2e2e2e/50 p-4 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors">
                  <div className="text-sm text-gray-300 font-medium">{t('frozen_balance')}</div>
                  <div className="text-2xl font-bold" style={{ color: '#2E2E2E' }}>
                    ${balanceInfo.frozenBalance.toFixed(2)}
                  </div>
                  <div className="text-xs text-gray-300 mt-1">{t('investment_capital_locked')}</div>
                </div>
                
                <div className="bg-gradient-to-r from-purple-900/50 to-purple-800/50 p-4 rounded-lg border border-purple-700 hover:border-purple-600 transition-colors">
                  <div className="text-sm text-purple-300 font-medium">{t('total_balance')}</div>
                  <div className="text-2xl font-bold text-purple-400">
                    ${balanceInfo.totalBalance.toFixed(2)}
                  </div>
                  <div className="text-xs text-purple-200 mt-1">{t('total_account_value')}</div>
                </div>
                
                <div className="bg-gradient-to-r from-yellow-900/50 to-yellow-800/50 p-4 rounded-lg border border-yellow-700 hover:border-yellow-600 transition-colors">
                  <div className="text-sm text-yellow-300 font-medium">{t('referral_earnings')}</div>
                  <div className="text-2xl font-bold text-yellow-400">
                    ${balanceInfo.referralEarnings.toFixed(2)}
                  </div>
                  <div className="text-xs text-yellow-200 mt-1">{t('from_referrals')}</div>
                </div>
              </div>
              
              {/* Withdrawal Limit Info */}
              <div className="mt-4 p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg border border-gray-700">
                <div className="text-sm text-gray-300 font-medium mb-2">{t('withdrawal_limits')}</div>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 text-xs text-gray-400">
                  <div>• {t('min_withdrawal')}</div>
                  <div>• {t('max_withdrawal')}</div>
                  <div>• {t('withdrawal_processing_time')}</div>
                </div>
              </div>
              
              {/* Fee Information */}
              <div className="mt-4 p-3 bg-gradient-to-r from-yellow-900/20 to-yellow-800/20 rounded-lg border border-yellow-700">
                <div className="text-sm text-yellow-300 font-medium mb-2">{t('withdrawal_fee_info')}</div>
                <div className="text-xs text-yellow-200">
                  {t('fee_percentage')} - {t('fee_note')}
                </div>
              </div>
              
              {/* Zero Balance Warning */}
              {balanceInfo.withdrawableBalance <= 0 && (
                <div className="mt-4 p-3 bg-gradient-to-r from-yellow-900/20 to-yellow-800/20 rounded-lg border border-yellow-700">
                  <div className="text-sm text-yellow-300 font-medium mb-1">{t('no_withdrawable_balance')}</div>
                  <div className="text-xs text-yellow-200">
                    {t('no_withdrawable_balance_message')}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Withdrawal Form */}
            <Card className="bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <DollarSign className="h-5 w-5" />
                  <span>{t('withdraw_funds')}</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">{t('withdraw_earnings')}</CardDescription>
              </CardHeader>
              <CardContent>
                {balanceInfo.withdrawableBalance <= 0 ? (
                  <div className="text-center py-8">
                    <div className="text-muted-foreground mb-4">
                      {t('no_withdrawable_balance_message')}
                    </div>
                    <Button 
                      disabled 
                      className="w-full bg-gradient-to-br from-gray-700 to-custom-2e2e2e border-gray-600 text-gray-400 cursor-not-allowed"
                    >
                      {t('submit_withdrawal')}
                    </Button>
                  </div>
                ) : (
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
                          className="pl-10 bg-gradient-to-r from-custom-2e2e2e to-gray-900 border-gray-700 text-white placeholder:text-gray-400"
                          required
                          max={balanceInfo.withdrawableBalance}
                          step="0.01"
                        />
                      </div>
                      <div className="text-xs text-muted-foreground">
                        {t('min_max_withdrawal')}
                      </div>
                    </div>

                    {/* Fee Calculation Display */}
                    {amount && parseFloat(amount) > 0 && (
                      <div className="space-y-3 p-4 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg border border-gray-700">
                        <div className="text-sm font-medium text-foreground">
                          {t('fee_calculation')}
                        </div>
                        
                        <div className="space-y-2">
                          <div className="flex justify-between text-sm">
                            <span className="text-muted-foreground">{t('requested_amount')}:</span>
                            <span className="text-foreground">${parseFloat(amount).toFixed(2)}</span>
                          </div>
                          
                          <div className="flex justify-between text-sm">
                            <span className="text-muted-foreground">{t('fee_amount')}:</span>
                            <span className="text-red-400">-${(parseFloat(amount) * 0.12).toFixed(2)}</span>
                          </div>
                          
                          <div className="border-t border-gray-700 pt-2">
                            <div className="flex justify-between text-sm font-medium">
                              <span className="text-green-400">{t('net_amount')}:</span>
                              <span className="text-green-400">${(parseFloat(amount) * 0.88).toFixed(2)}</span>
                            </div>
                          </div>
                        </div>
                        
                        <div className="text-xs text-yellow-400 bg-yellow-900/20 p-2 rounded border border-yellow-700">
                          {t('fee_note')}
                        </div>
                      </div>
                    )}

                    <div className="space-y-2">
                      <Label htmlFor="walletAddress" className="text-foreground">{t('usdt_wallet_address')}</Label>
                      <Input
                        id="walletAddress"
                        type="text"
                        placeholder={t('usdt_wallet_address')}
                        value={walletAddress}
                        onChange={(e) => setWalletAddress(e.target.value)}
                        className="bg-gradient-to-r from-custom-2e2e2e to-gray-900 border-gray-700 text-white placeholder:text-gray-400"
                        required
                      />
                    </div>

                    <Button 
                      type="submit" 
                      disabled={submitting}
                      className="w-full bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 hover:from-gray-700 hover:to-custom-2e2e2e text-white"
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
                )}
              </CardContent>
            </Card>

            {/* Wallet Management */}
            <Card className="bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
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
                    <div className="p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
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
                    className="bg-gradient-to-r from-custom-2e2e2e to-gray-900 border-gray-700 text-white placeholder:text-gray-400"
                  />
                </div>

                <Button 
                  onClick={handleSaveWallet}
                  className="w-full bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 hover:from-gray-700 hover:to-custom-2e2e2e text-white" 
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
            <Card className="lg:col-span-2 bg-gradient-to-br from-custom-2e2e2e to-gray-900 border-gray-700 shadow-lg">
              <CardHeader>
                <CardTitle className="text-foreground">{t('important_notes')}</CardTitle>
              </CardHeader>
              <CardContent>
                <Alert className="bg-gradient-to-r from-custom-2e2e2e to-gray-900 border-gray-700 text-white">
                  <AlertTriangle className="h-4 w-4 text-yellow-400" />
                  <AlertDescription className="text-white">
                    <ul className="space-y-2">
                      <li>• {t('only_usdt_accepted')}</li>
                      <li>• {t('verify_wallet_address')}</li>
                    </ul>
                  </AlertDescription>
                </Alert>
              </CardContent>
            </Card>
          </div>
        </div>
        
        
      </div>
    </ProtectedRoute>
  )
}
