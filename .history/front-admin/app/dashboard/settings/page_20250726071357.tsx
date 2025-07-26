"use client"

import { useEffect, useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Switch } from "@/components/ui/switch"
import { apiClient } from "@/lib/api"
import { useToast } from "@/hooks/use-toast"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"
import { Save, SettingsIcon } from "lucide-react"

interface Settings {
  maintenanceMode: boolean
  aboutContent: string
  usdtWalletAddress: string
  defaultUsageLimit: number
}

export default function SettingsPage() {
  const [settings, setSettings] = useState<Settings | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState("")
  const { toast } = useToast()

  useEffect(() => {
    loadSettings()
  }, [])

  const loadSettings = async () => {
    try {
      setLoading(true)
      const data = await apiClient.getSettings()
      setSettings(data)
    } catch (err: any) {
      setError(err.message || "Failed to load settings")
    } finally {
      setLoading(false)
    }
  }

  const handleToggleMaintenanceMode = async (enabled: boolean) => {
    try {
      setSaving(true)
      await apiClient.toggleMaintenanceMode(enabled)
      setSettings((prev) => (prev ? { ...prev, maintenanceMode: enabled } : null))
      toast({
        title: "Success",
        description: `Maintenance mode ${enabled ? "enabled" : "disabled"}`,
      })
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to update maintenance mode",
        variant: "destructive",
      })
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateAboutContent = async () => {
    if (!settings) return

    try {
      setSaving(true)
      await apiClient.updateAboutContent(settings.aboutContent)
      toast({
        title: "Success",
        description: "About content updated successfully",
      })
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to update about content",
        variant: "destructive",
      })
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateDefaultUsageLimit = async () => {
    if (!settings) return

    try {
      setSaving(true)
      await apiClient.updateDefaultUsageLimit(settings.defaultUsageLimit)
      toast({
        title: "Success",
        description: "Default usage limit updated successfully",
      })
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to update usage limit",
        variant: "destructive",
      })
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
        <DashboardHeader title="Settings" description="Manage platform settings" />
        <div className="grid gap-4 sm:gap-6">
          {Array.from({ length: 3 }).map((_, i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-6 w-48" />
                <Skeleton className="h-4 w-64" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-20 w-full" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
        <DashboardHeader title="Settings" description="Manage platform settings" />
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 p-4 sm:p-6">
      <DashboardHeader title="Settings" description="Manage platform settings" />

      <div className="grid gap-4 sm:gap-6">
        {/* Maintenance Mode */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2 text-lg sm:text-xl">
              <SettingsIcon className="h-5 w-5" />
              <span>Maintenance Mode</span>
            </CardTitle>
            <CardDescription>Enable maintenance mode to temporarily disable platform access</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex items-center space-x-2">
              <Switch
                checked={settings?.maintenanceMode || false}
                onCheckedChange={handleToggleMaintenanceMode}
                disabled={saving}
              />
              <Label className="text-sm sm:text-base">
                {settings?.maintenanceMode ? "Maintenance mode is ON" : "Maintenance mode is OFF"}
              </Label>
            </div>
          </CardContent>
        </Card>

        {/* About Content */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg sm:text-xl">About Content</CardTitle>
            <CardDescription>Update the about content displayed to users</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="aboutContent">About Content</Label>
              <Textarea
                id="aboutContent"
                value={settings?.aboutContent || ""}
                onChange={(e) => setSettings((prev) => (prev ? { ...prev, aboutContent: e.target.value } : null))}
                placeholder="Enter about content..."
                rows={6}
                className="min-h-[120px]"
              />
            </div>
            <Button onClick={handleUpdateAboutContent} disabled={saving} className="w-full sm:w-auto">
              <Save className="mr-2 h-4 w-4" />
              {saving ? "Saving..." : "Save About Content"}
            </Button>
          </CardContent>
        </Card>

        {/* Platform Wallet */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg sm:text-xl">Platform Wallet</CardTitle>
            <CardDescription>USDT wallet address for receiving deposits</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <Label htmlFor="walletAddress">USDT Wallet Address</Label>
              <Input 
                id="walletAddress" 
                value={settings?.usdtWalletAddress || ""} 
                readOnly 
                className="font-mono text-xs sm:text-sm" 
              />
              <p className="text-xs sm:text-sm text-muted-foreground">
                This is the platform's USDT wallet address. Contact system administrator to change.
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Default Usage Limit */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg sm:text-xl">Referral Settings</CardTitle>
            <CardDescription>Configure default referral usage limits for new users</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="usageLimit">Default Usage Limit</Label>
              <Input
                id="usageLimit"
                type="number"
                value={settings?.defaultUsageLimit || 0}
                onChange={(e) =>
                  setSettings((prev) =>
                    prev ? { ...prev, defaultUsageLimit: Number.parseInt(e.target.value) || 0 } : null,
                  )
                }
                placeholder="100"
              />
              <p className="text-xs sm:text-sm text-muted-foreground">Default number of referrals a new user can make</p>
            </div>
            <Button onClick={handleUpdateDefaultUsageLimit} disabled={saving} className="w-full sm:w-auto">
              <Save className="mr-2 h-4 w-4" />
              {saving ? "Saving..." : "Save Usage Limit"}
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
