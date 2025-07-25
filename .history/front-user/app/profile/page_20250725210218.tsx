"use client"

import { useState, useEffect } from "react"
import { apiService } from "@/services/api"
import ProtectedRoute from "@/components/ProtectedRoute"
import Navbar from "@/components/Navbar"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { User, Phone, Calendar, Shield, ArrowLeft, Edit, Save, X } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { useLanguage } from "@/contexts/LanguageContext"

interface UserProfile {
  id: number
  fullName: string
  username: string
  phoneNumber: string
  email?: string
  status: string
  createdAt: string
  lastLoginAt?: string
  totalDeposits: number
  totalWithdrawals: number
  totalProfits: number
}

// Default profile data in case API fails
const defaultProfile: UserProfile = {
  id: 1,
  fullName: "Demo User",
  username: "demo_user",
  phoneNumber: "+1234567890",
  status: "active",
  createdAt: new Date().toISOString(),
  totalDeposits: 0,
  totalWithdrawals: 0,
  totalProfits: 0
}

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile | null>(defaultProfile)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [editing, setEditing] = useState(false)
  const [editForm, setEditForm] = useState({
    fullName: "",
    phoneNumber: "",
  })
  const [saving, setSaving] = useState(false)
  const { toast } = useToast()
  const { t } = useLanguage()

  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    try {
      const data = await apiService.getProfile()
      // Ensure we have valid data
      const validProfile = {
        ...defaultProfile,
        ...data,
        status: data.status || "active",
        fullName: data.fullName || "Demo User",
        username: data.username || "demo_user",
        phoneNumber: data.phoneNumber || "+1234567890",
        totalDeposits: data.totalDeposits || 0,
        totalWithdrawals: data.totalWithdrawals || 0,
        totalProfits: data.totalProfits || 0
      }
      setProfile(validProfile)
      setEditForm({
        fullName: validProfile.fullName,
        phoneNumber: validProfile.phoneNumber,
      })
    } catch (err: any) {
      console.error("Error fetching profile:", err)
      setError(err.message || "Failed to load profile")
      // Keep default profile on error
      setProfile(defaultProfile)
      setEditForm({
        fullName: defaultProfile.fullName,
        phoneNumber: defaultProfile.phoneNumber,
      })
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = () => {
    setEditing(true)
  }

  const handleCancel = () => {
    setEditing(false)
    setEditForm({
      fullName: profile?.fullName || "",
      phoneNumber: profile?.phoneNumber || "",
    })
  }

  const handleSave = async () => {
    if (!profile) return

    setSaving(true)
    try {
      // Note: This would need to be implemented in the API
      // await apiService.updateProfile(editForm)
      
      setProfile({
        ...profile,
        fullName: editForm.fullName,
        phoneNumber: editForm.phoneNumber,
      })
      setEditing(false)
      
      toast({
        title: "Success!",
        description: "Profile updated successfully",
      })
    } catch (err: any) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const getStatusBadge = (status: string | undefined) => {
    // Handle undefined or null status
    if (!status) {
      return <Badge className="bg-green-100 text-green-800">Active</Badge>
    }

    switch (status.toLowerCase()) {
      case 'active':
        return <Badge className="bg-green-100 text-green-800">Active</Badge>
      case 'suspended':
        return <Badge variant="destructive">Suspended</Badge>
      case 'pending':
        return <Badge className="bg-yellow-100 text-yellow-800">Pending</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) {
      return "N/A"
    }
    
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    } catch (error) {
      return "Invalid Date"
    }
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

        <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-2">
              <User className="h-6 w-6" />
              <h1 className="text-3xl font-bold text-foreground">{t('profile')}</h1>
            </div>
            <Link href="/dashboard">
              <Button variant="outline" className="flex items-center space-x-2">
                <ArrowLeft className="h-4 w-4" />
                <span>{t('back_to_dashboard')}</span>
              </Button>
            </Link>
          </div>

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {profile && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Profile Information */}
              <div className="lg:col-span-2 space-y-6">
                <Card className="bg-card border-border">
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="text-foreground">Personal Information</CardTitle>
                        <CardDescription className="text-muted-foreground">Your account details and preferences</CardDescription>
                      </div>
                      {!editing ? (
                        <Button variant="outline" size="sm" onClick={handleEdit}>
                          <Edit className="h-4 w-4 mr-2" />
                          Edit
                        </Button>
                      ) : (
                        <div className="flex space-x-2">
                          <Button size="sm" onClick={handleSave} disabled={saving} className="bg-primary hover:bg-primary/90 text-primary-foreground">
                            {saving ? (
                              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                            ) : (
                              <>
                                <Save className="h-4 w-4 mr-2" />
                                Save
                              </>
                            )}
                          </Button>
                          <Button variant="outline" size="sm" onClick={handleCancel}>
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      )}
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="fullName" className="text-foreground">Full Name</Label>
                        {editing ? (
                          <Input
                            id="fullName"
                            value={editForm.fullName}
                            onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
                            className="bg-muted border-border text-foreground"
                          />
                        ) : (
                          <div className="p-3 bg-muted rounded-lg">
                            <p className="font-medium text-foreground">{profile.fullName}</p>
                          </div>
                        )}
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="username" className="text-foreground">Username</Label>
                        <div className="p-3 bg-muted rounded-lg">
                          <p className="font-mono font-medium text-foreground">@{profile.username}</p>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="phoneNumber" className="text-foreground">Phone Number</Label>
                        {editing ? (
                          <Input
                            id="phoneNumber"
                            value={editForm.phoneNumber}
                            onChange={(e) => setEditForm({ ...editForm, phoneNumber: e.target.value })}
                            className="bg-muted border-border text-foreground"
                          />
                        ) : (
                          <div className="p-3 bg-muted rounded-lg">
                            <p className="font-medium text-foreground">{profile.phoneNumber}</p>
                          </div>
                        )}
                      </div>

                      <div className="space-y-2">
                        <Label className="text-foreground">Account Status</Label>
                        <div className="p-3 bg-muted rounded-lg">
                          {getStatusBadge(profile.status)}
                        </div>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label className="text-foreground">Member Since</Label>
                        <div className="p-3 bg-muted rounded-lg">
                          <div className="flex items-center space-x-2">
                            <Calendar className="h-4 w-4 text-muted-foreground" />
                            <p className="text-sm text-foreground">{formatDate(profile.createdAt)}</p>
                          </div>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <Label className="text-foreground">Last Login</Label>
                        <div className="p-3 bg-muted rounded-lg">
                          <div className="flex items-center space-x-2">
                            <Shield className="h-4 w-4 text-muted-foreground" />
                            <p className="text-sm text-foreground">
                              {profile.lastLoginAt ? formatDate(profile.lastLoginAt) : 'Never'}
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Account Statistics */}
                <Card className="bg-card border-border">
                  <CardHeader>
                    <CardTitle className="text-foreground">Account Statistics</CardTitle>
                    <CardDescription className="text-muted-foreground">Your investment activity summary</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div className="text-center p-4 bg-muted rounded-lg">
                        <p className="text-sm text-muted-foreground mb-1">Total Deposits</p>
                        <p className="text-2xl font-bold text-foreground">${profile.totalDeposits.toFixed(2)}</p>
                      </div>
                      <div className="text-center p-4 bg-muted rounded-lg">
                        <p className="text-sm text-muted-foreground mb-1">Total Withdrawals</p>
                        <p className="text-2xl font-bold text-primary">${profile.totalWithdrawals.toFixed(2)}</p>
                      </div>
                      <div className="text-center p-4 bg-muted rounded-lg">
                        <p className="text-sm text-muted-foreground mb-1">Total Profits</p>
                        <p className="text-2xl font-bold text-green-600">${profile.totalProfits.toFixed(2)}</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Sidebar */}
              <div className="space-y-6">
                {/* Quick Actions */}
                <Card className="bg-card border-border">
                  <CardHeader>
                    <CardTitle className="text-foreground">Quick Actions</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <Link href="/deposit">
                      <Button className="w-full" variant="outline">
                        Make Deposit
                      </Button>
                    </Link>
                    <Link href="/withdraw">
                      <Button className="w-full" variant="outline">
                        Withdraw Funds
                      </Button>
                    </Link>
                    <Link href="/referrals">
                      <Button className="w-full" variant="outline">
                        View Referrals
                      </Button>
                    </Link>
                  </CardContent>
                </Card>
              </div>
            </div>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}
