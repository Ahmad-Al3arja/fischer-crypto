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
      const walletData = await apiService.getWallet().catch(err => {
        console.error("Error fetching wallet:", err)
        return defaultWalletInfo
      })

      // Use default balance info since the endpoint doesn't exist
      setBalanceInfo(defaultBalanceInfo)
      
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
        title: "Success!",
        description: "Withdrawal request submitted successfully",
      })
      
      // Reset form
      setAmount("")
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
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Dashboard
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

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Withdrawal Form */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <DollarSign className="h-5 w-5" />
                  <span>Withdraw Funds</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">Withdraw your earnings to your USDT wallet</CardDescription>
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
                        placeholder="Enter withdrawal amount"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        className="pl-10 bg-muted border-border text-foreground placeholder:text-muted-foreground"
                        required
                      />
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Min: $10 | Max: $50,000
                    </p>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="walletAddress" className="text-foreground">USDT Wallet Address</Label>
                    <Input
                      id="walletAddress"
                      type="text"
                      placeholder="Enter your USDT wallet address"
                      value={walletAddress}
                      onChange={(e) => setWalletAddress(e.target.value)}
                      className="bg-muted border-border text-foreground placeholder:text-muted-foreground"
                      required
                    />
                  </div>

                  <Button 
                    type="submit" 
                    className="w-full bg-primary hover:bg-primary/90 text-primary-foreground" 
                    disabled={submitting}
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

            {/* Wallet Management */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <Wallet className="h-5 w-5" />
                  <span>Wallet Management</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">Save your USDT wallet address for future withdrawals</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {walletInfo.isSet ? (
                  <div className="space-y-2">
                    <Label className="text-foreground">Current Wallet Address</Label>
                    <div className="p-3 bg-muted rounded-lg">
                      <p className="text-sm text-muted-foreground break-all">{walletInfo.usdtAddress}</p>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Your wallet address is saved. You can update it below.
                    </p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    <Label className="text-foreground">No Wallet Address Set</Label>
                    <p className="text-sm text-muted-foreground">
                      Please save your USDT wallet address to enable withdrawals.
                    </p>
                  </div>
                )}

                <div className="space-y-2">
                  <Label htmlFor="newWalletAddress" className="text-foreground">New Wallet Address</Label>
                  <Input
                    id="newWalletAddress"
                    type="text"
                    placeholder="Enter new USDT wallet address"
                    value={walletAddress}
                    onChange={(e) => setWalletAddress(e.target.value)}
                    className="bg-muted border-border text-foreground placeholder:text-muted-foreground"
                  />
                </div>

                <Button 
                  onClick={handleSaveWallet}
                  className="w-full bg-card border-border hover:bg-muted text-foreground" 
                  disabled={submitting}
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
              </CardContent>
            </Card>

            {/* Important Notes */}
            <Card className="lg:col-span-2 bg-card border-border">
              <CardHeader>
                <CardTitle className="text-foreground">Important Notes</CardTitle>
              </CardHeader>
              <CardContent>
                <Alert className="bg-blue-50 border-blue-200">
                  <AlertTriangle className="h-4 w-4 text-blue-600" />
                  <AlertDescription className="text-blue-800">
                    <ul className="space-y-2">
                      <li>• Minimum withdrawal amount: $10</li>
                      <li>• Maximum withdrawal amount: $50,000</li>
                      <li>• Withdrawal processing time: 24-48 hours</li>
                      <li>• Only USDT (TRC20) wallet addresses are accepted</li>
                      <li>• Make sure your wallet address is correct before submitting</li>
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