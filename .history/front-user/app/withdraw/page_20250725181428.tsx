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
import { Banknote, DollarSign, Wallet, AlertCircle } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"

interface WalletInfo {
  usdtAddress: string
  isSet: boolean
}

interface BalanceInfo {
  availableBalance: number
  totalBalance: number
  pendingWithdrawals: number
}

// Default data in case API fails
const defaultWalletInfo: WalletInfo = {
  usdtAddress: "",
  isSet: false
}

const defaultBalanceInfo: BalanceInfo = {
  availableBalance: 0,
  totalBalance: 0,
  pendingWithdrawals: 0
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

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      setApiError("")
      const [walletData, balanceData] = await Promise.all([
        apiService.getWallet().catch(err => {
          console.error("Error fetching wallet:", err)
          return defaultWalletInfo
        }),
        apiService.getBalance().catch(err => {
          console.error("Error fetching balance:", err)
          return defaultBalanceInfo
        })
      ])
      
      // Ensure we have valid data
      const validWalletInfo = {
        usdtAddress: walletData?.usdtAddress || "",
        isSet: walletData?.isSet || false
      }
      
      const validBalanceInfo = {
        availableBalance: balanceData?.availableBalance || 0,
        totalBalance: balanceData?.totalBalance || 0,
        pendingWithdrawals: balanceData?.pendingWithdrawals || 0
      }
      
      setWalletInfo(validWalletInfo)
      setBalanceInfo(validBalanceInfo)
      
      if (validWalletInfo.usdtAddress) {
        setWalletAddress(validWalletInfo.usdtAddress)
      }
    } catch (err: any) {
      console.error("Error in fetchData:", err)
      setApiError("Failed to load data. Please try again later.")
      // Keep default data on error
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

    if (balanceInfo && numAmount > balanceInfo.availableBalance) {
      setError("Insufficient available balance")
      return
    }

    if (!walletInfo?.isSet && !walletAddress.trim()) {
      setError("Please set your wallet address first")
      return
    }

    setSubmitting(true)
    setError("")

    try {
      await apiService.createWithdrawal({
        amount: numAmount,
        walletAddress: walletAddress || undefined
      })
      
      toast({
        title: "Success!",
        description: "Withdrawal request submitted successfully",
      })
      
      // Reset form and refresh data
      setAmount("")
      await fetchData()
    } catch (err: any) {
      console.error("Error creating withdrawal:", err)
      setError(err.message || "Failed to submit withdrawal request")
    } finally {
      setSubmitting(false)
    }
  }

  const handleSaveWallet = async () => {
    if (!walletAddress.trim()) {
      setError("Please enter a valid wallet address")
      return
    }

    setSubmitting(true)
    setError("")

    try {
      await apiService.saveWallet({ usdtAddress: walletAddress })
      await fetchData()
      toast({
        title: "Success!",
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
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-3xl font-bold text-foreground">Withdraw</h1>
            <Link href="/dashboard">
              <Button variant="outline" className="bg-card border-border hover:bg-muted">
                Back to Dashboard
              </Button>
            </Link>
          </div>

          {apiError && (
            <Alert variant="destructive" className="mb-6">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{apiError}</AlertDescription>
            </Alert>
          )}

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Withdrawal Form */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <Banknote className="h-5 w-5" />
                  <span>Withdraw Funds</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">Withdraw your profits to your wallet</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="amount" className="text-foreground">Amount (USD)</Label>
                    <div className="relative">
                      <DollarSign className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                      <Input
                        id="amount"
                        type="number"
                        placeholder="Enter amount to withdraw"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        className="pl-10 bg-muted border-border text-foreground placeholder:text-muted-foreground"
                        required
                      />
                    </div>
                    {balanceInfo && (
                      <p className="text-xs text-muted-foreground">
                        Available: ${balanceInfo.availableBalance.toFixed(2)}
                      </p>
                    )}
                  </div>

                  <Button 
                    type="submit" 
                    className="w-full bg-primary hover:bg-primary/90 text-primary-foreground" 
                    disabled={submitting || !walletInfo?.isSet}
                  >
                    {submitting ? (
                      <div className="flex items-center space-x-2">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                        <span>Processing...</span>
                      </div>
                    ) : (
                      "Submit Withdrawal"
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Wallet Setup */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <Wallet className="h-5 w-5" />
                  <span>Wallet Address</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">Set your USDT wallet address for withdrawals</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {walletInfo?.isSet ? (
                  <div className="space-y-2">
                    <Label className="text-foreground">Current Wallet Address</Label>
                    <div className="p-3 bg-muted rounded-lg">
                      <p className="font-mono text-sm break-all text-foreground">{walletInfo.usdtAddress}</p>
                    </div>
                    <Button 
                      variant="outline" 
                      onClick={() => setWalletInfo({ ...walletInfo, isSet: false })}
                      className="w-full bg-card border-border hover:bg-muted"
                    >
                      Change Address
                    </Button>
                  </div>
                ) : (
                  <div className="space-y-2">
                    <Label htmlFor="walletAddress" className="text-foreground">USDT Wallet Address</Label>
                    <Input
                      id="walletAddress"
                      type="text"
                      placeholder="Enter your USDT wallet address"
                      value={walletAddress}
                      onChange={(e) => setWalletAddress(e.target.value)}
                      className="bg-muted border-border text-foreground placeholder:text-muted-foreground"
                    />
                    <Button 
                      onClick={handleSaveWallet} 
                      disabled={submitting || !walletAddress.trim()}
                      className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
                    >
                      {submitting ? (
                        <div className="flex items-center space-x-2">
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                          <span>Saving...</span>
                        </div>
                      ) : (
                        "Save Wallet Address"
                      )}
                    </Button>
                  </div>
                )}

                {!walletInfo?.isSet && (
                  <Alert className="bg-blue-50 border-blue-200">
                    <AlertCircle className="h-4 w-4 text-blue-600" />
                    <AlertDescription className="text-blue-800">
                      You need to set your wallet address before making withdrawals.
                    </AlertDescription>
                  </Alert>
                )}
              </CardContent>
            </Card>

            {/* Balance Information */}
            <Card className="lg:col-span-2 bg-card border-border">
              <CardHeader>
                <CardTitle className="text-foreground">Balance Information</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Total Balance</p>
                    <p className="text-2xl font-bold text-foreground">${balanceInfo.totalBalance.toFixed(2)}</p>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Available for Withdrawal</p>
                    <p className="text-2xl font-bold text-primary">${balanceInfo.availableBalance.toFixed(2)}</p>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Pending Withdrawals</p>
                    <p className="text-2xl font-bold text-orange-600">${balanceInfo.pendingWithdrawals.toFixed(2)}</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Withdrawal Information */}
            <Card className="lg:col-span-2 bg-card border-border">
              <CardHeader>
                <CardTitle className="text-foreground">Withdrawal Information</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground">Minimum Withdrawal</p>
                    <p className="font-semibold text-foreground">$10.00</p>
                  </div>
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground">Processing Time</p>
                    <p className="font-semibold text-foreground">24-48 hours</p>
                  </div>
                  <div className="text-center p-3 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground">Network Fee</p>
                    <p className="font-semibold text-foreground">$1.00</p>
                  </div>
                </div>
                
                <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                  <h4 className="font-semibold text-blue-900 mb-2">Important Notes:</h4>
                  <ul className="text-sm text-blue-800 space-y-1">
                    <li>• Withdrawals are processed within 24-48 hours</li>
                    <li>• A network fee of $1.00 applies to each withdrawal</li>
                    <li>• Minimum withdrawal amount is $10.00</li>
                    <li>• Only USDT (TRC20) network is supported</li>
                  </ul>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
} 