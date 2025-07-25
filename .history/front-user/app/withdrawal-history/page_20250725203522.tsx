"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { History, ArrowLeft, Clock, CheckCircle, XCircle, AlertCircle } from "lucide-react"
import Link from "next/link"

interface WithdrawalHistory {
  id: number
  amount: number
  status: string
  walletAddress: string
  createdAt: string
  processedAt?: string
  transactionHash?: string
}

// Default data in case API fails
const defaultWithdrawals: WithdrawalHistory[] = [
  {
    id: 1,
    amount: 150,
    status: "approved",
    walletAddress: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    createdAt: "2024-01-15T10:30:00Z",
    processedAt: "2024-01-15T11:30:00Z",
    transactionHash: "0x1234567890abcdef1234567890abcdef12345678"
  },
  {
    id: 2,
    amount: 300,
    status: "pending",
    walletAddress: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    createdAt: "2024-01-20T14:45:00Z"
  }
]

export default function WithdrawalHistoryPage() {
  const [withdrawals, setWithdrawals] = useState<WithdrawalHistory[]>(defaultWithdrawals)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [apiError, setApiError] = useState("")

  useEffect(() => {
    fetchWithdrawalHistory()
  }, [])

  const fetchWithdrawalHistory = async () => {
    try {
      setApiError("")
      const data = await apiService.getWithdrawalHistory().catch(err => {
        console.error("Error fetching withdrawal history:", err)
        return null
      })
      
      if (data && Array.isArray(data.withdrawals)) {
        // Convert backend WithdrawalResponse to frontend WithdrawalHistory
        const validWithdrawals = data.withdrawals.map(w => ({
          id: w.id || 0,
          amount: w.amount || 0,
          status: w.status || "pending",
          walletAddress: w.walletAddress || "",
          createdAt: w.createdAt || new Date().toISOString(),
          processedAt: w.processedAt,
          transactionHash: undefined // Backend doesn't provide transaction hash
        }))
        
        setWithdrawals(validWithdrawals)
        setApiError("")
      } else {
        // Use default data if API fails or returns invalid data
        setWithdrawals(defaultWithdrawals)
        setApiError("Failed to load withdrawal history from database. Showing demo data.")
      }
    } catch (err: any) {
      console.error("Error in fetchWithdrawalHistory:", err)
      setApiError("Failed to load withdrawal history. Please try again later.")
      // Keep default data on error
      setWithdrawals(defaultWithdrawals)
    } finally {
      setLoading(false)
    }
  }

  const getStatusBadge = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending':
        return <Badge variant="secondary" className="bg-yellow-100 text-yellow-800">Pending</Badge>
      case 'approved':
        return <Badge variant="default" className="bg-green-100 text-green-800">Approved</Badge>
      case 'rejected':
        return <Badge variant="destructive">Rejected</Badge>
      case 'processing':
        return <Badge variant="secondary" className="bg-blue-100 text-blue-800">Processing</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending':
        return <Clock className="h-4 w-4 text-yellow-600" />
      case 'approved':
        return <CheckCircle className="h-4 w-4 text-green-600" />
      case 'rejected':
        return <XCircle className="h-4 w-4 text-red-600" />
      case 'processing':
        return <AlertCircle className="h-4 w-4 text-blue-600" />
      default:
        return <Clock className="h-4 w-4 text-gray-600" />
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
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

        <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-2">
              <History className="h-6 w-6" />
              <h1 className="text-3xl font-bold text-foreground">Withdrawal History</h1>
            </div>
            <Link href="/dashboard">
              <Button variant="outline" className="flex items-center space-x-2">
                <ArrowLeft className="h-4 w-4" />
                <span>Back to Dashboard</span>
              </Button>
            </Link>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {apiError && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{apiError}</AlertDescription>
            </Alert>
          )}

          <Card>
            <CardHeader>
              <CardTitle>Transaction History</CardTitle>
              <CardDescription>
                View all your withdrawal requests and their current status
              </CardDescription>
            </CardHeader>
            <CardContent>
              {withdrawals.length === 0 ? (
                <div className="text-center py-8">
                  <History className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                  <h3 className="text-lg font-semibold text-muted-foreground mb-2">No Withdrawals Yet</h3>
                  <p className="text-muted-foreground mb-4">
                    You haven't made any withdrawal requests yet.
                  </p>
                  <Link href="/withdraw">
                    <Button>Make Your First Withdrawal</Button>
                  </Link>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>ID</TableHead>
                        <TableHead>Amount</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Wallet Address</TableHead>
                        <TableHead>Created</TableHead>
                        <TableHead>Processed</TableHead>
                        <TableHead>Transaction</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {withdrawals.map((withdrawal) => (
                        <TableRow key={withdrawal.id}>
                          <TableCell className="font-mono">#{withdrawal.id}</TableCell>
                          <TableCell className="font-semibold">
                            ${withdrawal.amount.toFixed(2)}
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center space-x-2">
                              {getStatusIcon(withdrawal.status)}
                              {getStatusBadge(withdrawal.status)}
                            </div>
                          </TableCell>
                          <TableCell className="font-mono text-sm">
                            {formatWalletAddress(withdrawal.walletAddress)}
                          </TableCell>
                          <TableCell className="text-sm text-muted-foreground">
                            {formatDate(withdrawal.createdAt)}
                          </TableCell>
                          <TableCell className="text-sm text-muted-foreground">
                            {withdrawal.processedAt ? formatDate(withdrawal.processedAt) : '-'}
                          </TableCell>
                          <TableCell>
                            {withdrawal.transactionHash ? (
                              <a
                                href={`https://tronscan.org/#/transaction/${withdrawal.transactionHash}`}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="text-primary hover:underline font-mono text-sm"
                              >
                                {formatWalletAddress(withdrawal.transactionHash)}
                              </a>
                            ) : (
                              <span className="text-muted-foreground text-sm">-</span>
                            )}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Statistics Card */}
          {withdrawals.length > 0 && (
            <Card className="mt-6">
              <CardHeader>
                <CardTitle>Statistics</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Total Withdrawals</p>
                    <p className="text-2xl font-bold text-foreground">{withdrawals.length}</p>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Total Amount</p>
                    <p className="text-2xl font-bold text-primary">
                      ${withdrawals.reduce((sum, w) => sum + w.amount, 0).toFixed(2)}
                    </p>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Pending</p>
                    <p className="text-2xl font-bold text-yellow-600">
                      {withdrawals.filter(w => w.status.toLowerCase() === 'pending').length}
                    </p>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">Completed</p>
                    <p className="text-2xl font-bold text-green-600">
                      {withdrawals.filter(w => w.status.toLowerCase() === 'approved').length}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}
