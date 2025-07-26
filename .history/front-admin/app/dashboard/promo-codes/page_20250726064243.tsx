"use client"

import { useEffect, useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { apiClient } from "@/lib/api"
import { Plus, Edit, Trash2, Copy, CheckCircle, XCircle, Gift } from "lucide-react"

interface PromoCode {
  id: number
  code: string
  bonusValue: number
  usageLimit: number
  usedCount: number
  isActive: boolean
  createdAt: string
  expiresAt?: string
}

export default function PromoCodesPage() {
  const [promoCodes, setPromoCodes] = useState<PromoCode[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [newPromoCode, setNewPromoCode] = useState({
    code: "",
    bonusValue: 10,
    usageLimit: 100,
    expiresAt: ""
  })

  useEffect(() => {
    loadPromoCodes()
  }, [])

  const loadPromoCodes = async () => {
    try {
      setLoading(true)
      const data = await apiClient.getPromoCodes()
      setPromoCodes(data.promoCodes || [])
    } catch (err: any) {
      setError(err.message || "Failed to load promo codes")
    } finally {
      setLoading(false)
    }
  }

  const handleCreatePromoCode = async () => {
    try {
      await apiClient.createPromoCode(newPromoCode)
      setIsCreateDialogOpen(false)
      setNewPromoCode({ code: "", bonusValue: 10, usageLimit: 100, expiresAt: "" })
      loadPromoCodes()
    } catch (err: any) {
      setError(err.message || "Failed to create promo code")
    }
  }

  const handleTogglePromoCode = async (promoCodeId: number) => {
    try {
      await apiClient.togglePromoCode(promoCodeId)
      loadPromoCodes()
    } catch (err: any) {
      setError(err.message || "Failed to toggle promo code")
    }
  }

  const copyToClipboard = (code: string) => {
    navigator.clipboard.writeText(code)
  }

  if (loading) {
    return (
      <div className="space-y-6 p-6">
        <DashboardHeader title="Promo Codes" description="Manage promotional codes and discounts" />
        <div className="grid gap-4">
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
    <div className="space-y-6 p-6">
      <DashboardHeader title="Promo Codes" description="Manage promotional codes and discounts" />

      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Create Promo Code Button */}
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-lg font-semibold">Promotional Codes</h2>
          <p className="text-sm text-muted-foreground">Create and manage discount codes for users</p>
        </div>
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Create Promo Code
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Promo Code</DialogTitle>
              <DialogDescription>Add a new promotional code with discount settings</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="code">Promo Code</Label>
                <Input
                  id="code"
                  placeholder="SUMMER2024"
                  value={newPromoCode.code}
                  onChange={(e) => setNewPromoCode({ ...newPromoCode, code: e.target.value.toUpperCase() })}
                />
              </div>
              <div>
                <Label htmlFor="bonusValue">Bonus Value ($)</Label>
                <Input
                  id="bonusValue"
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={newPromoCode.bonusValue}
                  onChange={(e) => setNewPromoCode({ ...newPromoCode, bonusValue: parseFloat(e.target.value) })}
                />
              </div>
              <div>
                <Label htmlFor="maxUsage">Maximum Usage</Label>
                <Input
                  id="maxUsage"
                  type="number"
                  min="1"
                  value={newPromoCode.maxUsage}
                  onChange={(e) => setNewPromoCode({ ...newPromoCode, maxUsage: parseInt(e.target.value) })}
                />
              </div>
              <div>
                <Label htmlFor="expiresAt">Expiry Date (Optional)</Label>
                <Input
                  id="expiresAt"
                  type="datetime-local"
                  value={newPromoCode.expiresAt}
                  onChange={(e) => setNewPromoCode({ ...newPromoCode, expiresAt: e.target.value })}
                />
              </div>
              <Button onClick={handleCreatePromoCode} className="w-full">
                Create Promo Code
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {/* Promo Codes List */}
      <div className="grid gap-4">
        {promoCodes.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-8">
              <Gift className="h-12 w-12 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">No Promo Codes</h3>
              <p className="text-muted-foreground text-center mb-4">
                Create your first promotional code to start offering discounts to users
              </p>
              <Button onClick={() => setIsCreateDialogOpen(true)}>
                <Plus className="mr-2 h-4 w-4" />
                Create First Promo Code
              </Button>
            </CardContent>
          </Card>
        ) : (
          promoCodes.map((promoCode) => (
            <Card key={promoCode.id}>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-lg font-bold">{promoCode.code}</span>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => copyToClipboard(promoCode.code)}
                        className="h-6 w-6 p-0"
                      >
                        <Copy className="h-3 w-3" />
                      </Button>
                    </div>
                    <Badge variant={promoCode.isActive ? "default" : "secondary"}>
                      {promoCode.isActive ? "Active" : "Inactive"}
                    </Badge>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleTogglePromoCode(promoCode.id)}
                    >
                      {promoCode.isActive ? (
                        <XCircle className="h-4 w-4 text-red-500" />
                      ) : (
                        <CheckCircle className="h-4 w-4 text-green-500" />
                      )}
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                  <div>
                    <p className="text-muted-foreground">Discount</p>
                    <p className="font-semibold">{promoCode.discountPercentage}%</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Usage</p>
                    <p className="font-semibold">{promoCode.currentUsage} / {promoCode.maxUsage}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Created</p>
                    <p className="font-semibold">{new Date(promoCode.createdAt).toLocaleDateString()}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Expires</p>
                    <p className="font-semibold">
                      {promoCode.expiresAt ? new Date(promoCode.expiresAt).toLocaleDateString() : "Never"}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  )
} 