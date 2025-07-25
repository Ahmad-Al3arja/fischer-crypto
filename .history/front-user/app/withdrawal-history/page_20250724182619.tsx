// front-user/app/withdrawal-history/page.tsx
"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { History, DollarSign, Calendar, Wallet, AlertCircle, Clock, CheckCircle, XCircle } from "lucide-react"

interface WithdrawalItem {
  id: number
  amount: number
  fee: number
  netAmount: number
  walletAddress: string
  status: string
  createdAt: string
  processedAt?: string
  rejectionNote?: string
}

interface WithdrawalHistoryData {
  withdrawals: WithdrawalItem[]
  availableBalance: number
}

export default function WithdrawalHistoryPage() {
  const [historyData, setHistoryData] = useState<WithdrawalHistoryData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    fetchWithdrawalHistory()
  }, [])

  const fetchWithdrawalHistory = async () => {
    try {
      const data = await apiService.getWithdrawalHistory()
      setHistoryData(data)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const getStatusBadge = (status: string) => {
    switch (status.toLowerCase()) {
      case "pending":
        return (
          <Badge className="bg-yellow-500/10 text-yellow-600 border-yellow-500/20 hover:bg-yellow-500/20">
            <Clock className="w-3 h-3 mr-1" />
            قيد المراجعة
          </Badge>
        )
      case "approved":
        return (
          <Badge className="bg-primary/10 text-primary border-primary/20 hover:bg-primary/20">
            <CheckCircle className="w-3 h-3 mr-1" />
            مكتمل
          </Badge>
        )
      case "rejected":
        return (
          <Badge variant="destructive" className="bg-destructive/10 border-destructive/20">
            <XCircle className="w-3 h-3 mr-1" />
            مرفوض
          </Badge>
        )
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ar-EG', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const truncateAddress = (address: string) => {
    if (address.length <= 20) return address
    return `${address.substring(0, 10)}...${address.substring(address.length - 10)}`
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
      <div className="min-h-screen bg-background fischer-gradient-bg">
        <Navbar />

        <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-foreground">سجل السحوبات</h1>
            <p className="text-muted-foreground mt-2">تتبع طلبات السحب وحالتها</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6 bg-destructive/10 border-destructive/20">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {historyData && (
            <>
              {/* Available Balance Card */}
              <Card className="fischer-card mb-6">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2 space-x-reverse">
                    <DollarSign className="h-5 w-5" />
                    <span>الرصيد المتاح الحالي</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-center">
                    <div className="text-4xl font-bold text-primary mb-2">
                      ${historyData.availableBalance.toFixed(2)}
                    </div>
                    <p className="text-sm text-muted-foreground">متاح للسحب</p>
                  </div>
                </CardContent>
              </Card>

              {/* Withdrawal History */}
              <Card className="fischer-card">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2 space-x-reverse">
                    <History className="h-5 w-5" />
                    <span>طلبات السحب</span>
                  </CardTitle>
                  <CardDescription>جميع طلبات السحب وحالتها الحالية</CardDescription>
                </CardHeader>
                <CardContent>
                  {historyData.withdrawals.length === 0 ? (
                    <div className="text-center py-12">
                      <div className="w-24 h-24 bg-muted rounded-full flex items-center justify-center mx-auto mb-4">
                        <History className="h-12 w-12 text-muted-foreground" />
                      </div>
                      <h3 className="text-lg font-medium text-foreground mb-2">لا توجد طلبات سحب</h3>
                      <p className="text-muted-foreground">سيظهر سجل السحوبات هنا</p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {historyData.withdrawals.map((withdrawal) => (
                        <div key={withdrawal.id} className="border border-border rounded-xl p-6 hover:bg-secondary/30 transition-all duration-200">
                          <div className="flex items-start justify-between mb-4">
                            <div className="flex items-center space-x-3 space-x-reverse">
                              <div className="p-3 bg-primary/10 rounded-xl">
                                <Wallet className="h-6 w-6 text-primary" />
                              </div>
                              <div>
                                <p className="font-semibold text-foreground text-lg">طلب سحب #{withdrawal.id}</p>
                                <p className="text-sm text-muted-foreground flex items-center space-x-1 space-x-reverse">
                                  <Calendar className="h-4 w-4" />
                                  <span>{formatDate(withdrawal.createdAt)}</span>
                                </p>
                              </div>
                            </div>
                            {getStatusBadge(withdrawal.status)}
                          </div>

                          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                            <div className="bg-secondary/50 rounded-lg p-4 text-center">
                              <p className="text-sm text-muted-foreground mb-1">المبلغ المطلوب</p>
                              <p className="text-xl font-bold text-foreground">${withdrawal.amount.toFixed(2)}</p>
                            </div>
                            <div className="bg-destructive/10 rounded-lg p-4 text-center">
                              <p className="text-sm text-muted-foreground mb-1">الرسوم (2%)</p>
                              <p className="text-xl font-bold text-destructive">-${withdrawal.fee.toFixed(2)}</p>
                            </div>
                            <div className="bg-primary/10 rounded-lg p-4 text-center">
                              <p className="text-sm text-muted-foreground mb-1">صافي المبلغ</p>
                              <p className="text-xl font-bold text-primary">${withdrawal.netAmount.toFixed(2)}</p>
                            </div>
                          </div>

                          <div className="space-y-3">
                            <div>
                              <p className="text-sm text-muted-foreground mb-1">عنوان المحفظة</p>
                              <div className="bg-input rounded-lg p-3 border border-border">
                                <p className="font-mono text-sm text-foreground break-all">
                                  {withdrawal.walletAddress}
                                </p>
                              </div>
                            </div>

                            {withdrawal.processedAt && (
                              <div>
                                <p className="text-sm text-muted-foreground mb-1">
                                  {withdrawal.status === "APPROVED" ? "تاريخ الموافقة" : "تاريخ المعالجة"}
                                </p>
                                <div className="flex items-center space-x-2 space-x-reverse">
                                  <Calendar className="h-4 w-4 text-muted-foreground" />
                                  <p className="text-sm text-foreground">{formatDate(withdrawal.processedAt)}</p>
                                </div>
                              </div>
                            )}

                            {withdrawal.rejectionNote && (
                              <Alert variant="destructive" className="bg-destructive/10 border-destructive/20">
                                <AlertCircle className="h-4 w-4" />
                                <AlertDescription>
                                  <strong>سبب الرفض:</strong> {withdrawal.rejectionNote}
                                </AlertDescription>
                              </Alert>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* Status Legend */}
              {historyData.withdrawals.length > 0 && (
                <Card className="fischer-card mt-6">
                  <CardHeader>
                    <CardTitle className="text-foreground text-center">معاني الحالات</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-center">
                      <div className="space-y-2">
                        <Badge className="bg-yellow-500/10 text-yellow-600 border-yellow-500/20">
                          <Clock className="w-3 h-3 mr-1" />
                          قيد المراجعة
                        </Badge>
                        <p className="text-xs text-muted-foreground">في انتظار موافقة الإدارة</p>
                      </div>
                      <div className="space-y-2">
                        <Badge className="bg-primary/10 text-primary border-primary/20">
                          <CheckCircle className="w-3 h-3 mr-1" />
                          مكتمل
                        </Badge>
                        <p className="text-xs text-muted-foreground">تم السحب بنجاح</p>
                      </div>
                      <div className="space-y-2">
                        <Badge variant="destructive" className="bg-destructive/10 border-destructive/20">
                          <XCircle className="w-3 h-3 mr-1" />
                          مرفوض
                        </Badge>
                        <p className="text-xs text-muted-foreground">تم رفض الطلب</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}
            </>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}