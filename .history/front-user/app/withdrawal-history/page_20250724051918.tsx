"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { History, DollarSign, Calendar, Wallet, AlertCircle } from "lucide-react"

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
        return <Badge variant="secondary">⏳ Pending</Badge>
      case "approved":
        return <Badge className="bg-green-600">✅ Approved</Badge>
      case "rejected":
        return <Badge variant="destructive">❌ Rejected</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString()
  }

  const truncateAddress = (address: string) => {
    if (address.length <= 20) return address
    return `${address.substring(0, 10)}...${address.substring(address.length - 10)}`
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

        <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">📜 Withdrawal History</h1>
            <p className="text-gray-600">Track your withdrawal requests and status</p>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {historyData && (
            <>
              {/* Available Balance Card */}
              <Card className="mb-6">
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <DollarSign className="h-5 w-5" />
                    <span>Current Available Balance</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-bold text-green-600">${historyData.availableBalance.toFixed(2)}</div>
                  <p className="text-sm text-gray-600 mt-1">Available for withdrawal</p>
                </CardContent>
              </Card>

              {/* Withdrawal History */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <History className="h-5 w-5" />
                    <span>Withdrawal Requests</span>
                  </CardTitle>
                  <CardDescription>All your withdrawal requests and their current status</CardDescription>
                </CardHeader>
                <CardContent>
                  {historyData.withdrawals.length === 0 ? (
                    <div className="text-center py-8">
                      <History className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                      <p className="text-gray-500">No withdrawal requests found</p>
                      <p className="text-sm text-gray-400">Your withdrawal history will appear here</p>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {historyData.withdrawals.map((withdrawal) => (
                        <div key={withdrawal.id} className="border rounded-lg p-4 hover:bg-gray-50 transition-colors">
                          <div className="flex items-start justify-between mb-3">
                            <div className="flex items-center space-x-3">
                              <div className="p-2 bg-blue-100 rounded-lg">
                                <Wallet className="h-5 w-5 text-blue-600" />
                              </div>
                              <div>
                                <p className="font-medium text-gray-900">Withdrawal Request #{withdrawal.id}</p>
                                <p className="text-sm text-gray-500">{formatDate(withdrawal.createdAt)}</p>
                              </div>
                            </div>
                            {getStatusBadge(withdrawal.status)}
                          </div>

                          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-3">
                            <div>
                              <p className="text-sm text-gray-500">Amount Requested</p>
                              <p className="font-medium">${withdrawal.amount.toFixed(2)}</p>
                            </div>
                            <div>
                              <p className="text-sm text-gray-500">Fee (2%)</p>
                              <p className="font-medium text-red-600">-${withdrawal.fee.toFixed(2)}</p>
                            </div>
                            <div>
                              <p className="text-sm text-gray-500">Net Amount</p>
                              <p className="font-medium text-green-600">${withdrawal.netAmount.toFixed(2)}</p>
                            </div>
                          </div>

                          <div className="mb-3">
                            <p className="text-sm text-gray-500">Wallet Address</p>
                            <p className="font-mono text-sm bg-gray-100 p-2 rounded">
                              {truncateAddress(withdrawal.walletAddress)}
                            </p>
                          </div>

                          {withdrawal.processedAt && (
                            <div className="mb-3">
                              <p className="text-sm text-gray-500">
                                {withdrawal.status === "APPROVED" ? "Approved At" : "Processed At"}
                              </p>
                              <div className="flex items-center space-x-2">
                                <Calendar className="h-4 w-4 text-gray-400" />
                                <p className="text-sm">{formatDate(withdrawal.processedAt)}</p>
                              </div>
                            </div>
                          )}

                          {withdrawal.rejectionNote && (
                            <Alert variant="destructive">
                              <AlertCircle className="h-4 w-4" />
                              <AlertDescription>
                                <strong>Rejection Reason:</strong> {withdrawal.rejectionNote}
                              </AlertDescription>
                            </Alert>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}
