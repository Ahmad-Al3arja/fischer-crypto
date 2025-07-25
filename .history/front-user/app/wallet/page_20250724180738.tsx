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
import { Wallet, Lock, Save, AlertTriangle } from "lucide-react"

interface WalletData {
  usdtAddress: string
  isLocked: boolean
  createdAt: string
  updatedAt: string
}

export default function WalletPage() {
  const [wallet, setWallet] = useState<WalletData | null>(null)
  const [walletAddress, setWalletAddress] = useState("")
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState("")
  const [success, setSuccess] = useState("")

  useEffect(() => {
    fetchWallet()
  }, [])

  const fetchWallet = async () => {
    try {
      const data = await apiService.getWallet()
      setWallet(data)
      if (data?.usdtAddress) {
        setWalletAddress(data.usdtAddress)
      }
    } catch (err: any) {
      // Wallet might not exist yet, which is fine
      if (!err.message.includes("not found")) {
        setError(err.message)
      }
    } finally {
      setLoading(false)
    }
  }

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setSuccess("")

    if (!walletAddress) {
      setError("Please enter a wallet address")
      return
    }

    // Basic TRC20 address validation
    if (!walletAddress.startsWith("T") || walletAddress.length !== 34) {
      setError("Invalid USDT TRC20 wallet address format")
      return
    }

    if (!/^T[A-Za-z0-9]{33}$/.test(walletAddress)) {
      setError("Invalid USDT TRC20 wallet address format")
      return
    }

    setSaving(true)

    try {
      await apiService.saveWallet({ usdtAddress: walletAddress })
      setSuccess("Wallet address saved successfully!")
      await fetchWallet()
    } catch (err: any) {
      setError(err.message)
    } finally {
      setSaving(false)
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

        <div className="max-w-2xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">🔒 Wallet Setup</h1>
            <p className="text-gray-600">Manage your USDT TRC20 wallet address</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {success && (
            <Alert className="mb-6 border-green-200 bg-green-50">
              <AlertDescription className="text-green-800">{success}</AlertDescription>
            </Alert>
          )}

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <Wallet className="h-5 w-5" />
                <span>USDT TRC20 Wallet Address</span>
              </CardTitle>
              <CardDescription>
                {wallet ? "Your wallet address is set and locked" : "Set up your wallet address for withdrawals (one-time only)"}
              </CardDescription>
            </CardHeader>
            <CardContent>
              {wallet?.isLocked ? (
                <div className="space-y-4">
                  <Alert>
                    <Lock className="h-4 w-4" />
                    <AlertDescription>
                      <strong>Wallet Locked:</strong> Your wallet address is locked by admin. Only administrators can
                      modify this address for security reasons.
                    </AlertDescription>
                  </Alert>

                  <div className="p-4 bg-gray-50 rounded-lg">
                    <Label className="text-sm font-medium text-gray-700">Current Wallet Address</Label>
                    <p className="mt-1 font-mono text-sm bg-white p-3 rounded border break-all">{wallet.usdtAddress}</p>
                  </div>

                  <div className="text-sm text-gray-600">
                    <p>
                      <strong>Created:</strong> {new Date(wallet.createdAt).toLocaleString()}
                    </p>
                    <p>
                      <strong>Last Updated:</strong> {new Date(wallet.updatedAt).toLocaleString()}
                    </p>
                  </div>
                </div>
              ) : (
                <form onSubmit={handleSave} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="walletAddress">USDT TRC20 Wallet Address</Label>
                    <Input
                      id="walletAddress"
                      type="text"
                      placeholder="Enter your USDT TRC20 wallet address (starts with T)"
                      value={walletAddress}
                      onChange={(e) => setWalletAddress(e.target.value)}
                      className="font-mono"
                      required
                    />
                    <p className="text-sm text-gray-500">Example: TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE</p>
                  </div>

                  <Alert>
                    <AlertTriangle className="h-4 w-4" />
                    <AlertDescription>
                      <strong>Important:</strong>
                      <ul className="mt-2 space-y-1 text-sm">
                        <li>• Only USDT TRC20 addresses are supported</li>
                        <li>• Double-check your address before saving</li>
                        <li>• Wrong addresses may result in lost funds</li>
                        <li>• Address starts with 'T' and is 34 characters long</li>
                      </ul>
                    </AlertDescription>
                  </Alert>

                  <Button type="submit" className="w-full" disabled={saving}>
                    {saving ? (
                      <div className="flex items-center space-x-2">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                        <span>Saving...</span>
                      </div>
                    ) : (
                      <div className="flex items-center space-x-2">
                        <Save className="h-4 w-4" />
                        <span>{wallet ? "Update Wallet Address" : "Save Wallet Address"}</span>
                      </div>
                    )}
                  </Button>
                </form>
              )}

              {wallet && !wallet.isLocked && (
                <div className="mt-4 text-sm text-gray-600">
                  <p>
                    <strong>Created:</strong> {new Date(wallet.createdAt).toLocaleString()}
                  </p>
                  <p>
                    <strong>Last Updated:</strong> {new Date(wallet.updatedAt).toLocaleString()}
                  </p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="mt-6">
            <CardHeader>
              <CardTitle>💡 Wallet Setup Tips</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3 text-sm text-gray-600">
                <div>
                  <h4 className="font-medium text-gray-900">What is USDT TRC20?</h4>
                  <p>
                    USDT TRC20 is a version of Tether (USDT) that runs on the TRON blockchain. It offers fast and
                    low-cost transactions.
                  </p>
                </div>
                <div>
                  <h4 className="font-medium text-gray-900">How to get a TRC20 wallet?</h4>
                  <p>
                    You can create a TRC20 wallet using popular wallets like TronLink, Trust Wallet, or exchanges like
                    Binance.
                  </p>
                </div>
                <div>
                  <h4 className="font-medium text-gray-900">Security Tips</h4>
                  <ul className="list-disc list-inside space-y-1 ml-4">
                    <li>Always double-check your wallet address</li>
                    <li>Test with a small amount first</li>
                    <li>Keep your private keys secure</li>
                    <li>Never share your private keys with anyone</li>
                  </ul>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </ProtectedRoute>
  )
}
