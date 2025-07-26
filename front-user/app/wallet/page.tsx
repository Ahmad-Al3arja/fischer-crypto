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
import { Badge } from "@/components/ui/badge"
import { Wallet, ArrowLeft, Copy, Edit, Save, X, AlertCircle } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { useLanguage } from "@/contexts/LanguageContext"


interface WalletInfo {
  usdtAddress: string
  isSet: boolean
  lastUpdated?: string
}

export default function WalletPage() {
  const [walletInfo, setWalletInfo] = useState<WalletInfo | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [editing, setEditing] = useState(false)
  const [walletAddress, setWalletAddress] = useState("")
  const [saving, setSaving] = useState(false)
  const { toast } = useToast()
  const { t } = useLanguage()

  useEffect(() => {
    fetchWalletInfo()
  }, [])

  const fetchWalletInfo = async () => {
    try {
      const data = await apiService.getWallet()
      setWalletInfo(data)
      setWalletAddress(data.usdtAddress || "")
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = () => {
    setEditing(true)
  }

  const handleCancel = () => {
    setEditing(false)
    setWalletAddress(walletInfo?.usdtAddress || "")
  }

  const handleSave = async () => {
    if (!walletAddress.trim()) {
      setError("Please enter a valid wallet address")
      return
    }

    setSaving(true)
    setError("")

    try {
      await apiService.saveWallet({ usdtAddress: walletAddress })
      await fetchWalletInfo()
      setEditing(false)
      
      toast({
        title: "Success!",
        description: "Wallet address updated successfully",
      })
    } catch (err: any) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const copyAddress = () => {
    if (walletInfo?.usdtAddress) {
      navigator.clipboard.writeText(walletInfo.usdtAddress)
      toast({
        title: "Copied!",
        description: "Wallet address copied to clipboard",
      })
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const formatWalletAddress = (address: string) => {
    if (address.length <= 20) return address
    return `${address.substring(0, 10)}...${address.substring(address.length - 10)}`
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
            <div className="flex items-center space-x-2">
              <Wallet className="h-6 w-6" />
              <h1 className="text-3xl font-bold text-foreground">{t('wallet_settings')}</h1>
            </div>
            <Link href="/dashboard">
              <Button variant="outline" className="flex items-center space-x-2">
                <ArrowLeft className="h-4 w-4" />
                <span>{t('back_to_dashboard')}</span>
              </Button>
            </Link>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Wallet Information */}
            <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>{t('usdt_wallet_address')}</CardTitle>
                    <CardDescription>{t('your_wallet_address')}</CardDescription>
                  </div>
                  {!editing && walletInfo?.isSet && (
                    <Button variant="outline" size="sm" onClick={handleEdit}>
                      <Edit className="h-4 w-4 mr-2" />
                      {t('edit')}
                    </Button>
                  )}
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {!walletInfo?.isSet ? (
                  <div className="space-y-4">
                    <Alert>
                      <AlertCircle className="h-4 w-4" />
                      <AlertDescription>
                        {t('need_set_wallet')}
                      </AlertDescription>
                    </Alert>
                    
                    <div className="space-y-2">
                      <Label htmlFor="walletAddress">{t('usdt_wallet_address_trc20')}</Label>
                      <Input
                        id="walletAddress"
                        placeholder={t('usdt_wallet_address')}
                        value={walletAddress}
                        onChange={(e) => setWalletAddress(e.target.value)}
                      />
                    </div>
                    
                    <Button onClick={handleSave} disabled={saving || !walletAddress.trim()}>
                      {saving ? (
                        <div className="flex items-center space-x-2">
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                          <span>{t('save')}</span>
                        </div>
                      ) : (
                        <>
                          <Save className="h-4 w-4 mr-2" />
                          {t('save_wallet_address_btn')}
                        </>
                      )}
                    </Button>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {editing ? (
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <Label htmlFor="editWalletAddress">{t('usdt_wallet_address_trc20')}</Label>
                          <Input
                            id="editWalletAddress"
                            placeholder={t('usdt_wallet_address')}
                            value={walletAddress}
                            onChange={(e) => setWalletAddress(e.target.value)}
                          />
                        </div>
                        
                        <div className="flex space-x-2">
                          <Button onClick={handleSave} disabled={saving || !walletAddress.trim()}>
                            {saving ? (
                              <div className="flex items-center space-x-2">
                                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                <span>{t('save')}</span>
                              </div>
                            ) : (
                              <>
                                <Save className="h-4 w-4 mr-2" />
                                {t('save_changes')}
                              </>
                            )}
                          </Button>
                          <Button variant="outline" onClick={handleCancel}>
                            <X className="h-4 w-4 mr-2" />
                            {t('cancel')}
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        <div className="p-4 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                          <div className="flex items-center justify-between">
                            <div className="flex-1">
                              <p className="font-mono text-sm break-all">{walletInfo.usdtAddress}</p>
                            </div>
                            <Button size="sm" variant="ghost" onClick={copyAddress} className="ml-2">
                              <Copy className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                        
                        <div className="flex items-center space-x-2">
                          <Badge className="bg-green-100 text-green-800">{t('active')}</Badge>
                          <span className="text-sm text-muted-foreground">
                            Ready for withdrawals
                          </span>
                        </div>
                        
                        {walletInfo.lastUpdated && (
                          <div className="text-sm text-muted-foreground">
                            Last updated: {formatDate(walletInfo.lastUpdated)}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Wallet Information */}
            <div className="space-y-6">
              {/* Network Information */}
              <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
                <CardHeader>
                  <CardTitle>{t('network_information')}</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="text-center p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                      <p className="text-sm text-muted-foreground mb-1">{t('network')}</p>
                      <p className="font-semibold">TRC20</p>
                    </div>
                    <div className="text-center p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                      <p className="text-sm text-muted-foreground mb-1">{t('token')}</p>
                      <p className="font-semibold">USDT</p>
                    </div>
                  </div>
                  
                  <div className="p-4 bg-gradient-to-r from-custom-2e2e2e to-gray-900 border border-gray-700 rounded-lg">
                    <h4 className="font-semibold text-foreground mb-2">{t('network_notes')}</h4>
                    <ul className="text-sm text-gray-300 space-y-1">
                      <li>• {t('only_trc20_supported')}</li>
                      <li>• {t('ensure_correct_network')}</li>
                      <li>• {t('double_check_address')}</li>
                      <li>• {t('withdrawals_sent_to')}</li>
                    </ul>
                  </div>
                </CardContent>
              </Card>

              {/* Quick Actions */}
              <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
                <CardHeader>
                  <CardTitle>{t('quick_actions')}</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <Link href="/withdraw">
                    <Button className="w-full" variant="outline">
                      {t('make_withdrawal')}
                    </Button>
                  </Link>
                  <Link href="/withdrawal-history">
                    <Button className="w-full" variant="outline">
                      {t('view_withdrawal_history')}
                    </Button>
                  </Link>
                  <Link href="/dashboard">
                    <Button className="w-full" variant="outline">
                      {t('back_to_dashboard')}
                    </Button>
                  </Link>
                </CardContent>
              </Card>

              {/* Security Tips */}
              <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
                <CardHeader>
                  <CardTitle>{t('security_tips')}</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="flex items-start space-x-2">
                    <div className="w-2 h-2 bg-green-500 rounded-full mt-2 flex-shrink-0"></div>
                    <p className="text-sm text-muted-foreground">
                      {t('verify_address_before_saving')}
                    </p>
                  </div>
                  <div className="flex items-start space-x-2">
                    <div className="w-2 h-2 bg-green-500 rounded-full mt-2 flex-shrink-0"></div>
                    <p className="text-sm text-muted-foreground">
                      {t('use_own_wallet')}
                    </p>
                  </div>
                  <div className="flex items-start space-x-2">
                    <div className="w-2 h-2 bg-green-500 rounded-full mt-2 flex-shrink-0"></div>
                    <p className="text-sm text-muted-foreground">
                      {t('keep_credentials_secure')}
                    </p>
                  </div>
                  <div className="flex items-start space-x-2">
                    <div className="w-2 h-2 bg-green-500 rounded-full mt-2 flex-shrink-0"></div>
                    <p className="text-sm text-muted-foreground">
                      {t('never_share_private_keys')}
                    </p>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
        
        
      </div>
    </ProtectedRoute>
  )
}
