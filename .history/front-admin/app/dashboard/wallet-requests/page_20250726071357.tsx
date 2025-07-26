"use client"

import { useEffect, useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { apiClient } from "@/lib/api"
import { Wallet, CheckCircle, XCircle, Clock, User } from "lucide-react"

interface WalletChangeRequest {
  id: number
  userName: string
  userPhone: string
  currentAddress: string
  newAddress: string
  reason: string
  status: string
  createdAt: string
  adminNotes?: string
}

export default function WalletRequestsPage() {
  const [requests, setRequests] = useState<WalletChangeRequest[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [selectedRequest, setSelectedRequest] = useState<WalletChangeRequest | null>(null)
  const [isActionDialogOpen, setIsActionDialogOpen] = useState(false)
  const [actionType, setActionType] = useState<"approve" | "reject" | null>(null)
  const [adminNotes, setAdminNotes] = useState("")

  useEffect(() => {
    loadWalletRequests()
  }, [])

  const loadWalletRequests = async () => {
    try {
      setLoading(true)
      const data = await apiClient.getWalletChangeRequests()
      setRequests(data.requests || [])
    } catch (err: any) {
      setError(err.message || "Failed to load wallet requests")
    } finally {
      setLoading(false)
    }
  }

  const handleAction = async () => {
    if (!selectedRequest || !actionType) return

    try {
      if (actionType === "approve") {
        await apiClient.approveWalletChangeRequest(selectedRequest.id, adminNotes)
      } else {
        await apiClient.rejectWalletChangeRequest(selectedRequest.id, adminNotes)
      }
      setIsActionDialogOpen(false)
      setSelectedRequest(null)
      setActionType(null)
      setAdminNotes("")
      loadWalletRequests()
    } catch (err: any) {
      setError(err.message || `Failed to ${actionType} request`)
    }
  }

  const openActionDialog = (request: WalletChangeRequest, type: "approve" | "reject") => {
    setSelectedRequest(request)
    setActionType(type)
    setIsActionDialogOpen(true)
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "PENDING":
        return <Badge variant="secondary"><Clock className="w-3 h-3 mr-1" />Pending</Badge>
      case "APPROVED":
        return <Badge variant="default"><CheckCircle className="w-3 h-3 mr-1" />Approved</Badge>
      case "REJECTED":
        return <Badge variant="destructive"><XCircle className="w-3 h-3 mr-1" />Rejected</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  if (loading) {
    return (
      <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
        <DashboardHeader title="Wallet Requests" description="Manage wallet address change requests" />
        <div className="grid gap-3 sm:gap-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-4 w-32" />
                <Skeleton className="h-3 w-48" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-6 w-24 mb-2" />
                <Skeleton className="h-4 w-32" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
      <DashboardHeader title="Wallet Requests" description="Manage wallet address change requests" />

      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Stats */}
      <div className="grid gap-3 sm:gap-4 grid-cols-1 sm:grid-cols-3">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center space-x-2">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium">Pending</span>
            </div>
            <p className="text-xl sm:text-2xl font-bold">
              {requests.filter(r => r.status === "PENDING").length}
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center space-x-2">
              <CheckCircle className="h-4 w-4 text-green-500" />
              <span className="text-sm font-medium">Approved</span>
            </div>
            <p className="text-xl sm:text-2xl font-bold">
              {requests.filter(r => r.status === "APPROVED").length}
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center space-x-2">
              <XCircle className="h-4 w-4 text-red-500" />
              <span className="text-sm font-medium">Rejected</span>
            </div>
            <p className="text-xl sm:text-2xl font-bold">
              {requests.filter(r => r.status === "REJECTED").length}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Requests List */}
      <div className="grid gap-3 sm:gap-4">
        {requests.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-8 px-4">
              <Wallet className="h-12 w-12 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2 text-center">No Wallet Requests</h3>
              <p className="text-muted-foreground text-center">
                No wallet address change requests have been submitted yet
              </p>
            </CardContent>
          </Card>
        ) : (
          requests.map((request) => (
            <Card key={request.id}>
              <CardHeader>
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                  <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-3">
                    <div className="flex items-center space-x-2">
                      <User className="h-4 w-4 text-muted-foreground" />
                      <span className="font-medium text-sm sm:text-base">{request.userName}</span>
                    </div>
                    <span className="text-xs sm:text-sm text-muted-foreground">{request.userPhone}</span>
                  </div>
                  {getStatusBadge(request.status)}
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-3 sm:space-y-4">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                    <div>
                      <p className="text-xs sm:text-sm font-medium text-muted-foreground">Current Address</p>
                      <p className="font-mono text-xs sm:text-sm break-all">{request.currentAddress}</p>
                    </div>
                    <div>
                      <p className="text-xs sm:text-sm font-medium text-muted-foreground">New Address</p>
                      <p className="font-mono text-xs sm:text-sm break-all">{request.newAddress}</p>
                    </div>
                  </div>
                  <div>
                    <p className="text-xs sm:text-sm font-medium text-muted-foreground">Reason</p>
                    <p className="text-xs sm:text-sm">{request.reason}</p>
                  </div>
                  {request.adminNotes && (
                    <div>
                      <p className="text-xs sm:text-sm font-medium text-muted-foreground">Admin Notes</p>
                      <p className="text-xs sm:text-sm">{request.adminNotes}</p>
                    </div>
                  )}
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                    <p className="text-xs text-muted-foreground">
                      Requested on {new Date(request.createdAt).toLocaleDateString()}
                    </p>
                    {request.status === "PENDING" && (
                      <div className="flex flex-col sm:flex-row gap-2">
                        <Button
                          size="sm"
                          onClick={() => openActionDialog(request, "approve")}
                          className="bg-green-600 hover:bg-green-700 w-full sm:w-auto"
                        >
                          <CheckCircle className="w-4 h-4 mr-1" />
                          Approve
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => openActionDialog(request, "reject")}
                          className="w-full sm:w-auto"
                        >
                          <XCircle className="w-4 h-4 mr-1" />
                          Reject
                        </Button>
                      </div>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>

      {/* Action Dialog */}
      <Dialog open={isActionDialogOpen} onOpenChange={setIsActionDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {actionType === "approve" ? "Approve" : "Reject"} Wallet Change Request
            </DialogTitle>
            <DialogDescription>
              {actionType === "approve" 
                ? "This will approve the wallet address change request."
                : "This will reject the wallet address change request."
              }
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="adminNotes">Admin Notes (Optional)</Label>
              <Textarea
                id="adminNotes"
                placeholder="Add any notes about this decision..."
                value={adminNotes}
                onChange={(e) => setAdminNotes(e.target.value)}
                rows={3}
              />
            </div>
            <div className="flex flex-col sm:flex-row gap-2">
              <Button
                onClick={handleAction}
                className={`${actionType === "approve" ? "bg-green-600 hover:bg-green-700" : ""} w-full sm:w-auto`}
              >
                {actionType === "approve" ? "Approve Request" : "Reject Request"}
              </Button>
              <Button variant="outline" onClick={() => setIsActionDialogOpen(false)} className="w-full sm:w-auto">
                Cancel
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
} 