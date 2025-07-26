"use client"

import { useEffect, useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { DataTable } from "@/components/data-table"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { apiClient } from "@/lib/api"
import type { ColumnDef } from "@tanstack/react-table"
import { MoreHorizontal, Eye, UserCheck, UserX, DollarSign } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { useToast } from "@/hooks/use-toast"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"

interface User {
  id: number
  fullName: string
  username: string
  phoneNumber: string
  planName: string
  totalBalance: string
  status: string
  createdAt: string
}

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const [showBalanceDialog, setShowBalanceDialog] = useState(false)
  const [balanceAmount, setBalanceAmount] = useState("")
  const [balanceReason, setBalanceReason] = useState("")
  const [actionLoading, setActionLoading] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    try {
      setLoading(true)
      const data = await apiClient.getUsers()
      setUsers(data.users || [])
    } catch (err: any) {
      setError(err.message || "Failed to load users")
    } finally {
      setLoading(false)
    }
  }

  const handleActivateUser = async (userId: number) => {
    try {
      setActionLoading(true)
      await apiClient.activateUser(userId)
      toast({
        title: "Success",
        description: "User activated successfully",
      })
      loadUsers()
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to activate user",
        variant: "destructive",
      })
    } finally {
      setActionLoading(false)
    }
  }

  const handleSuspendUser = async (userId: number) => {
    try {
      setActionLoading(true)
      await apiClient.suspendUser(userId)
      toast({
        title: "Success",
        description: "User suspended successfully",
      })
      loadUsers()
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to suspend user",
        variant: "destructive",
      })
    } finally {
      setActionLoading(false)
    }
  }

  const handleUpdateBalance = async () => {
    if (!selectedUser || !balanceAmount) return

    try {
      setActionLoading(true)
      await apiClient.updateUserBalance(selectedUser.id, Number.parseFloat(balanceAmount), balanceReason)
      toast({
        title: "Success",
        description: "User balance updated successfully",
      })
      setShowBalanceDialog(false)
      setBalanceAmount("")
      setBalanceReason("")
      setSelectedUser(null)
      loadUsers()
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to update balance",
        variant: "destructive",
      })
    } finally {
      setActionLoading(false)
    }
  }

  const getStatusBadge = (status: string) => {
    const statusMap = {
      ACTIVE: { label: "Active", variant: "default" as const },
      INACTIVE: { label: "Inactive", variant: "secondary" as const },
      SUSPENDED: { label: "Suspended", variant: "destructive" as const },
    }

    const statusInfo = statusMap[status as keyof typeof statusMap] || { label: status, variant: "secondary" as const }

    return <Badge variant={statusInfo.variant}>{statusInfo.label}</Badge>
  }

  const columns: ColumnDef<User>[] = [
    {
      accessorKey: "fullName",
      header: "Full Name",
      cell: ({ row }) => {
        const value = row.getValue("fullName") as string
        return <div className="font-medium truncate max-w-[120px] sm:max-w-none">{value}</div>
      },
    },
    {
      accessorKey: "username",
      header: "Username",
      cell: ({ row }) => {
        const value = row.getValue("username") as string
        return <div className="truncate max-w-[100px] sm:max-w-none">{value}</div>
      },
    },
    {
      accessorKey: "phoneNumber",
      header: "Phone",
      cell: ({ row }) => {
        const value = row.getValue("phoneNumber") as string
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
      accessorKey: "totalBalance",
      header: "Balance",
      cell: ({ row }) => {
        const balance = Number.parseFloat(row.getValue("totalBalance"))
        return <div className="font-medium">${balance.toLocaleString()}</div>
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
        const user = row.original

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
              <DropdownMenuItem onClick={() => navigator.clipboard.writeText(user.id.toString())}>
                Copy user ID
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem>
                <Eye className="mr-2 h-4 w-4" />
                View details
              </DropdownMenuItem>
              <DropdownMenuItem
                onClick={() => {
                  setSelectedUser(user)
                  setShowBalanceDialog(true)
                }}
              >
                <DollarSign className="mr-2 h-4 w-4" />
                Update balance
              </DropdownMenuItem>
              {user.status === "ACTIVE" ? (
                <DropdownMenuItem onClick={() => handleSuspendUser(user.id)} className="text-red-600">
                  <UserX className="mr-2 h-4 w-4" />
                  Suspend user
                </DropdownMenuItem>
              ) : (
                <DropdownMenuItem onClick={() => handleActivateUser(user.id)} className="text-green-600">
                  <UserCheck className="mr-2 h-4 w-4" />
                  Activate user
                </DropdownMenuItem>
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
        <DashboardHeader title="Users" description="Manage platform users" />
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
        <DashboardHeader title="Users" description="Manage platform users" />
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
      <DashboardHeader title="Users" description="Manage platform users" />

      <DataTable columns={columns} data={users} searchKey="fullName" searchPlaceholder="Search users..." />

      {/* Balance Update Dialog */}
      <Dialog open={showBalanceDialog} onOpenChange={setShowBalanceDialog}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Update User Balance</DialogTitle>
            <DialogDescription>Update balance for {selectedUser?.fullName}</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="amount">Amount</Label>
              <Input
                id="amount"
                type="number"
                step="0.01"
                placeholder="Enter amount (positive to add, negative to subtract)"
                value={balanceAmount}
                onChange={(e) => setBalanceAmount(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="reason">Reason</Label>
              <Textarea
                id="reason"
                placeholder="Enter reason for balance update"
                value={balanceReason}
                onChange={(e) => setBalanceReason(e.target.value)}
                rows={3}
              />
            </div>
          </div>
          <DialogFooter className="flex-col sm:flex-row gap-2">
            <Button variant="outline" onClick={() => setShowBalanceDialog(false)} className="w-full sm:w-auto">
              Cancel
            </Button>
            <Button onClick={handleUpdateBalance} disabled={!balanceAmount || actionLoading} className="w-full sm:w-auto">
              {actionLoading ? "Updating..." : "Update Balance"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
