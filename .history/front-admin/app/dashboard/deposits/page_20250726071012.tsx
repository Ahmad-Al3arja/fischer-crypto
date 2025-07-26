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
import { useToast } from "@/hooks/use-toast"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"

interface Deposit {
  id: number
  userName: string
  userPhone: string
  planName: string
  amount: string
  bonusAmount: string
  status: string
  createdAt: string
}

export default function DepositsPage() {
  const [deposits, setDeposits] = useState<Deposit[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [actionLoading, setActionLoading] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    loadDeposits()
  }, [statusFilter])

  const loadDeposits = async () => {
    try {
      setLoading(true)
      const status = statusFilter === "all" ? undefined : statusFilter
      const data = await apiClient.getDeposits(status)
      setDeposits(data.deposits || [])
    } catch (err: any) {
      setError(err.message || "Failed to load deposits")
    } finally {
      setLoading(false)
    }
  }

  const handleApproveDeposit = async (depositId: number) => {
    try {
      setActionLoading(true)
      await apiClient.approveDeposit(depositId)
      toast({
        title: "Success",
        description: "Deposit approved successfully",
      })
      loadDeposits()
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to approve deposit",
        variant: "destructive",
      })
    } finally {
      setActionLoading(false)
    }
  }

  const handleRejectDeposit = async (depositId: number) => {
    try {
      setActionLoading(true)
      await apiClient.rejectDeposit(depositId)
      toast({
        title: "Success",
        description: "Deposit rejected successfully",
      })
      loadDeposits()
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to reject deposit",
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

  const columns: ColumnDef<Deposit>[] = [
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
      accessorKey: "planName",
      header: "Plan",
      cell: ({ row }) => {
        const value = row.getValue("planName") as string
        return <div className="truncate max-w-[80px] sm:max-w-none">{value}</div>
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
      accessorKey: "bonusAmount",
      header: "Bonus",
      cell: ({ row }) => {
        const bonus = Number.parseFloat(row.getValue("bonusAmount"))
        return bonus > 0 ? <div className="font-medium">${bonus.toLocaleString()}</div> : <div>-</div>
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
        const deposit = row.original

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
              <DropdownMenuItem onClick={() => navigator.clipboard.writeText(deposit.id.toString())}>
                Copy deposit ID
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              {deposit.status === "PENDING" && (
                <>
                  <DropdownMenuItem onClick={() => handleApproveDeposit(deposit.id)} className="text-green-600">
                    <Check className="mr-2 h-4 w-4" />
                    Approve
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleRejectDeposit(deposit.id)} className="text-red-600">
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
        <DashboardHeader title="Deposits" description="Manage user deposits" />
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
        <DashboardHeader title="Deposits" description="Manage user deposits" />
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
      <DashboardHeader title="Deposits" description="Manage user deposits" />

      <div className="flex flex-col sm:flex-row sm:items-center gap-4">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-full sm:w-48">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Deposits</SelectItem>
            <SelectItem value="PENDING">Pending</SelectItem>
            <SelectItem value="APPROVED">Approved</SelectItem>
            <SelectItem value="REJECTED">Rejected</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <DataTable columns={columns} data={deposits} searchKey="userName" searchPlaceholder="Search deposits..." />
    </div>
  )
}
