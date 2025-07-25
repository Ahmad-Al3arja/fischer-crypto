"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Banknote, Wallet, AlertTriangle } from "lucide-react"
import Link from "next/link"

interface BalanceData {
  totalBalance: number
  frozenBalance: number
  withdrawableBalance: number
  referralEarnings: number
}

interface WalletData {
  usdtAddress: string
  isLocked: boolean
}

export default function WithdrawPage() {
  const [balance, setBalance] = useState<BalanceData | null>(null)
  const [wallet, setWallet] = useState<WalletData | null>(null)
  const [amount, setAmount] = useState("")
  const [walletAddress, setWalletAddress] = useState("")
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState("")
  const [success, setSuccess] = useState("")

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const [balanceData, walletData] = await Promise.all([
        apiService.getBalance(),
        apiService
          .getWallet()
          .catch(() => null), // Wallet might not exist
      ])
      setBalance(balanceData)
      setWallet(walletData)
      if (walletData?.usdtAddress) {
        setWalletAddress(walletData.usdtAddress)
      }
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setSuccess("")

    if (!amount || Number.parseFloat(amount) <= 0) {
      setError("Please enter a valid amount")
      return
    }

    if (Number.parseFloat(amount) < 10) {
      setError("Minimum withdrawal amount is $10")
      return
    }

    if (balance && Number.parseFloat(amount) > balance.withdrawableBalance) {
      setError(`Insufficient balance. Available: $${balance.withdrawableBalance.toFixed(2)}`)
      return
    }

    if (!walletAddress) {
      setError("Please enter a USDT TRC20 wallet address")
      return
    }

    // Basic TRC20 address validation
    if (!walletAddress.startsWith("T") || walletAddress.length !== 34) {
      setError("Invalid USDT TRC20 wallet address format")
      return
    }

    setSubmitting(true)

    try {
      const response = await apiService.createWithdrawal({
        amount: Number.parseFloat(amount),
        walletAddress: walletAddress,
      })

      setSuccess("Withdrawal request submitted successfully! It will be reviewed by admin.")
      setAmount("")

      // Refresh balance
      await fetchData()
    } catch (err: any) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const calculateFee = (amount: number) => {
    return amount * 0.12 // 12% fee as shown in the image
  }

  const calculateNetAmount = (amount: number) => {
    return amount - calculateFee(amount)
  }

  if (loading) {
    return (
      <ProtectedRoute>
        <div className="min-h-screen flex items-center justify-center bg-background">
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
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-foreground">سحب الأرباح</h1>
            <p className="text-muted-foreground">اسحب أرباحك إلى محفظتك الخاصة</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {success && (
            <Alert className="mb-6 border-green-200 bg-green-900/20">
              <AlertDescription className="text-green-400">{success}</AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Balance Overview */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <Banknote className="h-5 w-5" />
                  <span>Available Balance</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">Your current withdrawable profits</CardDescription>
              </CardHeader>
              <CardContent>
                {balance && (
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div className="p-4 bg-primary/10 rounded-lg border border-primary/20">
                        <p className="text-sm text-primary">Total Balance</p>
                        <p className="text-2xl font-bold text-foreground">${balance.totalBalance.toFixed(2)}</p>
                      </div>
                      <div className="p-4 bg-muted rounded-lg border border-border">
                        <p className="text-sm text-muted-foreground">Frozen Balance</p>
                        <p className="text-2xl font-bold text-foreground">${balance.frozenBalance.toFixed(2)}</p>
                      </div>
                    </div>

                    <div className="p-4 bg-green-900/20 rounded-lg border-2 border-green-500/20">
                      <p className="text-sm text-green-400">Available for Withdrawal</p>
                      <p className="text-3xl font-bold text-green-400">${balance.withdrawableBalance.toFixed(2)}</p>
                    </div>

                    <div className="p-4 bg-yellow-900/20 rounded-lg border border-yellow-500/20">
                      <p className="text-sm text-yellow-400">Referral Earnings</p>
                      <p className="text-2xl font-bold text-yellow-400">${balance.referralEarnings.toFixed(2)}</p>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Withdrawal Form */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-foreground">
                  <Wallet className="h-5 w-5" />
                  <span>Withdrawal Request</span>
                </CardTitle>
                <CardDescription className="text-muted-foreground">Enter withdrawal details</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="amount" className="text-foreground">Withdrawal Amount ($)</Label>
                    <Input
                      id="amount"
                      type="number"
                      step="0.01"
                      min="10"
                      max={balance?.withdrawableBalance || 0}
                      placeholder="Enter amount (min $10)"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      className="bg-input border-border text-foreground placeholder:text-muted-foreground focus-ring"
                      required
                    />
                    {amount && Number.parseFloat(amount) >= 10 && (
                      <div className="text-sm text-muted-foreground space-y-1">
                        <p>Fee (12%): ${calculateFee(Number.parseFloat(amount)).toFixed(2)}</p>
                        <p className="font-medium text-foreground">
                          Net Amount: ${calculateNetAmount(Number.parseFloat(amount)).toFixed(2)}
                        </p>
                      </div>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="walletAddress" className="text-foreground">USDT TRC20 Wallet Address</Label>
                    <Input
                      id="walletAddress"
                      type="text"
                      placeholder="Enter your USDT TRC20 wallet address"
                      value={walletAddress}
                      onChange={(e) => setWalletAddress(e.target.value)}
                      className="bg-input border-border text-foreground placeholder:text-muted-foreground focus-ring"
                      required
                    />
                    {wallet?.isLocked && (
                      <p className="text-sm text-orange-400">Your saved wallet is locked. Only admin can modify it.</p>
                    )}
                    {!wallet && (
                      <p className="text-sm text-primary">
                        <Link href="/wallet" className="underline">
                          Save your wallet address
                        </Link>{" "}
                        for faster withdrawals
                      </p>
                    )}
                  </div>

                  <Alert className="bg-card border-border">
                    <AlertTriangle className="h-4 w-4" />
                    <AlertDescription className="text-foreground">
                      <strong>Important:</strong>
                      <ul className="mt-1 space-y-1 text-sm text-muted-foreground">
                        <li>• Minimum withdrawal: $10</li>
                        <li>• Withdrawal fee: 12%</li>
                        <li>• Processing time: 1-24 hours</li>
                        <li>• Only USDT TRC20 addresses accepted</li>
                      </ul>
                    </AlertDescription>
                  </Alert>

                  <Button type="submit" className="w-full bg-primary hover:bg-primary/90 text-primary-foreground btn-animate" disabled={submitting || !balance?.withdrawableBalance}>
                    {submitting ? (
                      <div className="flex items-center space-x-2">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                        <span>Processing...</span>
                      </div>
                    ) : (
                      "Submit Withdrawal Request"
                    )}
                  </Button>
                </form>

                <div className="mt-4 text-center">
                  <Link href="/withdrawal-history">
                    <Button variant="outline" size="sm" className="bg-card border-border hover:bg-accent">
                      View Withdrawal History
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}
