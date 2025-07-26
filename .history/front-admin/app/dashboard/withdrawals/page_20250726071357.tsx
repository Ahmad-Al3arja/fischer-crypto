"use client"

import { useEffect, useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { DataTable } from "@/components/data-table"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { apiClient } from "@/lib/api"
import type { ColumnDef } from "@tanstack/react-table"
import { Check, X, MoreHorizontal } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { useToast } from "@/hooks/use-toast"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"

interface Withdrawal {
  id: number
  userName: string
  userPhone: string
  amount: string
  walletAddress: string
  status: string
  createdAt: string
  rejectionNote?: string
}

export default function WithdrawalsPage() {
  const [withdrawals, setWithdrawals] = useState<Withdrawal[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [actionLoading, setActionLoading] = useState(false)
  const [showRejectDialog, setShowRejectDialog] = useState(false)
  const [selectedWithdrawal, setSelectedWithdrawal] = useState<Withdrawal | null>(null)
  const [rejectionReason, setRejectionReason] = useState("")
  const { toast } = useToast()

  useEffect(() => {
    loadWithdrawals()
  }, [statusFilter])

  const loadWithdrawals = async () => {
    try {
      setLoading(true)
      const status = statusFilter === "all" ? undefined : statusFilter
      const data = await apiClient.getWithdrawals(status)
      setWithdrawals(data.withdrawals || [])
    } catch (err: any) {
      setError(err.message || "Failed to load withdrawals")
    } finally {
      setLoading(false)
    }
  }

  const handleApproveWithdrawal = async (withdrawalId: number) => {
    try {
      setActionLoading(true)
      await apiClient.approveWithdrawal(withdrawalId)
      toast({
        title: "Success",
        description: "Withdrawal approved successfully",
      })
      loadWithdrawals()
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to approve withdrawal",
        variant: "destructive",
      })
    } finally {
      setActionLoading(false)
    }
  }

  const handleRejectWithdrawal = async () => {
    if (!selectedWithdrawal || !rejectionReason) return

    try {
      setActionLoading(true)
      await apiClient.rejectWithdrawal(selectedWithdrawal.id, rejectionReason)
      toast({
        title: "Success",
        description: "Withdrawal rejected successfully",
      })
      setShowRejectDialog(false)
      setRejectionReason("")
      setSelectedWithdrawal(null)
      loadWithdrawals()
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to reject withdrawal",
        variant: "destructive",
      })
    } finally {
      setActionLoading(false)
    }
  }

  const getStatusBadge = (status: string) => {
    const statusMap = {
      PENDING: { label: "Pending", variant: "secondary" as const },
      APPROVED: { label: "Approved", variant: "default" as const },
      REJECTED: { label: "Rejected", variant: "destructive" as const },
    }

    const statusInfo = statusMap[status as keyof typeof statusMap] || { label: status, variant: "secondary" as const }

    return <Badge variant={statusInfo.variant}>{statusInfo.label}</Badge>
  }

  const columns: ColumnDef<Withdrawal>[] = [
    {
      accessorKey: "userName",
      header: "User",
      cell: ({ row }) => {
        const value = row.getValue("userName") as string
        return <div className="font-medium truncate max-w-[100px] sm:max-w-none">{value}</div>
      },
    },
    {
      accessorKey: "userPhone",
      header: "Phone",
      cell: ({ row }) => {
        const value = row.getValue("userPhone") as string
        return <div className="truncate max-w-[100px] sm:max-w-none">{value}</div>
      },
    },
    {
      accessorKey: "amount",
      header: "Amount",
      cell: ({ row }) => {
        const amount = Number.parseFloat(row.getValue("amount"))
        return <div className="font-medium">${amount.toLocaleString()}</div>
      },
    },
    {
      accessorKey: "walletAddress",
      header: "Wallet Address",
      cell: ({ row }) => {
        const address = row.getValue("walletAddress") as string
        return <div className="font-mono text-xs sm:text-sm truncate max-w-[120px] sm:max-w-none">
          {address.slice(0, 8)}...{address.slice(-8)}
        </div>
      },
    },
    {
      accessorKey: "status",
      header: "Status",
      cell: ({ row }) => getStatusBadge(row.getValue("status")),
    },
    {
      accessorKey: "createdAt",
      header: "Created",
      cell: ({ row }) => {
        const date = new Date(row.getValue("createdAt"))
        return <div className="text-xs sm:text-sm">{date.toLocaleDateString()}</div>
      },
    },
    {
      id: "actions",
      cell: ({ row }) => {
        const withdrawal = row.original

        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="h-8 w-8 p-0">
                <span className="sr-only">Open menu</span>
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-48 sm:w-56">
              <DropdownMenuLabel>Actions</DropdownMenuLabel>
              <DropdownMenuItem onClick={() => navigator.clipboard.writeText(withdrawal.walletAddress)}>
                Copy wallet address
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              {withdrawal.status === "PENDING" && (
                <>
                  <DropdownMenuItem onClick={() => handleApproveWithdrawal(withdrawal.id)} className="text-green-600">
                    <Check className="mr-2 h-4 w-4" />
                    Approve
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => {
                      setSelectedWithdrawal(withdrawal)
                      setShowRejectDialog(true)
                    }}
                    className="text-red-600"
                  >
                    <X className="mr-2 h-4 w-4" />
                    Reject
                  </DropdownMenuItem>
                </>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
    },
  ]

  if (loading) {
    return (
      <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
        <DashboardHeader title="Withdrawals" description="Manage user withdrawals" />
        <div className="space-y-3 sm:space-y-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-16 w-full" />
          ))}
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
        <DashboardHeader title="Withdrawals" description="Manage user withdrawals" />
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
      <DashboardHeader title="Withdrawals" description="Manage user withdrawals" />

      <div className="flex flex-col sm:flex-row sm:items-center gap-4">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-full sm:w-48">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Withdrawals</SelectItem>
            <SelectItem value="PENDING">Pending</SelectItem>
            <SelectItem value="APPROVED">Approved</SelectItem>
            <SelectItem value="REJECTED">Rejected</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <DataTable columns={columns} data={withdrawals} searchKey="userName" searchPlaceholder="Search withdrawals..." />

      {/* Reject Dialog */}
      <Dialog open={showRejectDialog} onOpenChange={setShowRejectDialog}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Reject Withdrawal</DialogTitle>
            <DialogDescription>Provide a reason for rejecting this withdrawal request.</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="reason">Rejection Reason</Label>
              <Textarea
                id="reason"
                placeholder="Enter reason for rejection"
                value={rejectionReason}
                onChange={(e) => setRejectionReason(e.target.value)}
                rows={3}
              />
            </div>
          </div>
          <DialogFooter className="flex-col sm:flex-row gap-2">
            <Button variant="outline" onClick={() => setShowRejectDialog(false)} className="w-full sm:w-auto">
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleRejectWithdrawal} disabled={!rejectionReason || actionLoading} className="w-full sm:w-auto">
              {actionLoading ? "Rejecting..." : "Reject Withdrawal"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
