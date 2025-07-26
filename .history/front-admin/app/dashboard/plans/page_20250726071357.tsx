"use client"

import { useEffect, useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { DataTable } from "@/components/data-table"
import { Button } from "@/components/ui/button"
import { apiClient } from "@/lib/api"
import type { ColumnDef } from "@tanstack/react-table"
import { Plus, Edit, Trash2, MoreHorizontal } from "lucide-react"
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
import { useToast } from "@/hooks/use-toast"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"

interface Plan {
  id: number
  name: string
  price: string
  monthlyProfit: string
  dailyProfitMin: string
  dailyProfitMax: string
  planLevel: number
}

interface PlanFormData {
  name: string
  price: string
  monthlyProfit: string
  dailyProfitMin: string
  dailyProfitMax: string
  planLevel: string
}

export default function PlansPage() {
  const [plans, setPlans] = useState<Plan[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [showDialog, setShowDialog] = useState(false)
  const [editingPlan, setEditingPlan] = useState<Plan | null>(null)
  const [actionLoading, setActionLoading] = useState(false)
  const [formData, setFormData] = useState<PlanFormData>({
    name: "",
    price: "",
    monthlyProfit: "",
    dailyProfitMin: "",
    dailyProfitMax: "",
    planLevel: "",
  })
  const { toast } = useToast()

  useEffect(() => {
    loadPlans()
  }, [])

  const loadPlans = async () => {
    try {
      setLoading(true)
      const data = await apiClient.getPlans()
      setPlans(data.plans || [])
    } catch (err: any) {
      setError(err.message || "Failed to load plans")
    } finally {
      setLoading(false)
    }
  }

  const handleCreatePlan = () => {
    setEditingPlan(null)
    setFormData({
      name: "",
      price: "",
      monthlyProfit: "",
      dailyProfitMin: "",
      dailyProfitMax: "",
      planLevel: "",
    })
    setShowDialog(true)
  }

  const handleEditPlan = (plan: Plan) => {
    setEditingPlan(plan)
    setFormData({
      name: plan.name,
      price: plan.price,
      monthlyProfit: plan.monthlyProfit,
      dailyProfitMin: plan.dailyProfitMin,
      dailyProfitMax: plan.dailyProfitMax,
      planLevel: plan.planLevel.toString(),
    })
    setShowDialog(true)
  }

  const handleSubmit = async () => {
    try {
      setActionLoading(true)

      const planData = {
        name: formData.name,
        price: Number.parseFloat(formData.price),
        monthlyProfit: Number.parseFloat(formData.monthlyProfit),
        dailyProfitMin: Number.parseFloat(formData.dailyProfitMin),
        dailyProfitMax: Number.parseFloat(formData.dailyProfitMax),
        planLevel: Number.parseInt(formData.planLevel),
      }

      if (editingPlan) {
        await apiClient.updatePlan(editingPlan.id, planData)
        toast({
          title: "Success",
          description: "Plan updated successfully",
        })
      } else {
        await apiClient.createPlan(planData)
        toast({
          title: "Success",
          description: "Plan created successfully",
        })
      }

      setShowDialog(false)
      loadPlans()
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to save plan",
        variant: "destructive",
      })
    } finally {
      setActionLoading(false)
    }
  }

  const handleDeletePlan = async (planId: number) => {
    if (!confirm("Are you sure you want to delete this plan?")) return

    try {
      setActionLoading(true)
      await apiClient.deletePlan(planId)
      toast({
        title: "Success",
        description: "Plan deleted successfully",
      })
      loadPlans()
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to delete plan",
        variant: "destructive",
      })
    } finally {
      setActionLoading(false)
    }
  }

  const columns: ColumnDef<Plan>[] = [
    {
      accessorKey: "name",
      header: "Plan Name",
      cell: ({ row }) => {
        const value = row.getValue("name") as string
        return <div className="font-medium truncate max-w-[120px] sm:max-w-none">{value}</div>
      },
    },
    {
      accessorKey: "planLevel",
      header: "Level",
      cell: ({ row }) => {
        const value = row.getValue("planLevel") as number
        return <div className="font-medium">{value}</div>
      },
    },
    {
      accessorKey: "price",
      header: "Price",
      cell: ({ row }) => {
        const price = Number.parseFloat(row.getValue("price"))
        return <div className="font-medium">${price.toLocaleString()}</div>
      },
    },
    {
      accessorKey: "monthlyProfit",
      header: "Monthly Profit",
      cell: ({ row }) => {
        const profit = Number.parseFloat(row.getValue("monthlyProfit"))
        return <div className="font-medium">${profit.toLocaleString()}</div>
      },
    },
    {
      accessorKey: "dailyProfitMin",
      header: "Daily Min",
      cell: ({ row }) => {
        const min = Number.parseFloat(row.getValue("dailyProfitMin"))
        return <div className="font-medium">${min.toFixed(2)}</div>
      },
    },
    {
      accessorKey: "dailyProfitMax",
      header: "Daily Max",
      cell: ({ row }) => {
        const max = Number.parseFloat(row.getValue("dailyProfitMax"))
        return <div className="font-medium">${max.toFixed(2)}</div>
      },
    },
    {
      id: "actions",
      cell: ({ row }) => {
        const plan = row.original

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
              <DropdownMenuItem onClick={() => navigator.clipboard.writeText(plan.id.toString())}>
                Copy plan ID
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={() => handleEditPlan(plan)}>
                <Edit className="mr-2 h-4 w-4" />
                Edit plan
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleDeletePlan(plan.id)} className="text-red-600">
                <Trash2 className="mr-2 h-4 w-4" />
                Delete plan
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
    },
  ]

  if (loading) {
    return (
      <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
        <DashboardHeader title="Plans" description="Manage investment plans" />
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
        <DashboardHeader title="Plans" description="Manage investment plans" />
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <DashboardHeader title="Plans" description="Manage investment plans" />
        <Button onClick={handleCreatePlan} className="w-full sm:w-auto">
          <Plus className="mr-2 h-4 w-4" />
          Create Plan
        </Button>
      </div>

      <DataTable columns={columns} data={plans} searchKey="name" searchPlaceholder="Search plans..." />

      {/* Create/Edit Dialog */}
      <Dialog open={showDialog} onOpenChange={setShowDialog}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>{editingPlan ? "Edit Plan" : "Create Plan"}</DialogTitle>
            <DialogDescription>
              {editingPlan ? "Update plan details" : "Create a new investment plan"}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Plan Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter plan name"
              />
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="planLevel">Level</Label>
                <Input
                  id="planLevel"
                  type="number"
                  value={formData.planLevel}
                  onChange={(e) => setFormData({ ...formData, planLevel: e.target.value })}
                  placeholder="1"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="price">Price ($)</Label>
                <Input
                  id="price"
                  type="number"
                  step="0.01"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                  placeholder="100.00"
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="monthlyProfit">Monthly Profit ($)</Label>
              <Input
                id="monthlyProfit"
                type="number"
                step="0.01"
                value={formData.monthlyProfit}
                onChange={(e) => setFormData({ ...formData, monthlyProfit: e.target.value })}
                placeholder="10.00"
              />
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="dailyProfitMin">Daily Min ($)</Label>
                <Input
                  id="dailyProfitMin"
                  type="number"
                  step="0.01"
                  value={formData.dailyProfitMin}
                  onChange={(e) => setFormData({ ...formData, dailyProfitMin: e.target.value })}
                  placeholder="0.30"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="dailyProfitMax">Daily Max ($)</Label>
                <Input
                  id="dailyProfitMax"
                  type="number"
                  step="0.01"
                  value={formData.dailyProfitMax}
                  onChange={(e) => setFormData({ ...formData, dailyProfitMax: e.target.value })}
                  placeholder="0.40"
                />
              </div>
            </div>
          </div>
          <DialogFooter className="flex-col sm:flex-row gap-2">
            <Button variant="outline" onClick={() => setShowDialog(false)} className="w-full sm:w-auto">
              Cancel
            </Button>
            <Button onClick={handleSubmit} disabled={actionLoading} className="w-full sm:w-auto">
              {actionLoading ? "Saving..." : editingPlan ? "Update Plan" : "Create Plan"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
