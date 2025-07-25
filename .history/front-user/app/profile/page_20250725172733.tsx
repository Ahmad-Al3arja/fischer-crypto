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

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [editing, setEditing] = useState(false)
  const [editForm, setEditForm] = useState({
    fullName: "",
    phoneNumber: "",
  })
  const [saving, setSaving] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    try {
      const data = await apiService.getProfile()
      setProfile(data)
      setEditForm({
        fullName: data.fullName,
        phoneNumber: data.phoneNumber,
      })
    } catch (err: any) {
      setError(err.message)
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

  const getStatusBadge = (status: string) => {
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

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
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
              <h1 className="text-3xl font-bold text-foreground">Profile</h1>
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

          {profile && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Profile Information */}
              <div className="lg:col-span-2 space-y-6">
                <Card>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle>Personal Information</CardTitle>
                        <CardDescription>Your account details and preferences</CardDescription>
                      </div>
                      {!editing ? (
                        <Button variant="outline" size="sm" onClick={handleEdit}>
                          <Edit className="h-4 w-4 mr-2" />
                          Edit
                        </Button>
                      ) : (
                        <div className="flex space-x-2">
                          <Button size="sm" onClick={handleSave} disabled={saving}>
                            {saving ? (
                              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
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
                        <Label htmlFor="fullName">Full Name</Label>
                        {editing ? (
                          <Input
                            id="fullName"
                            value={editForm.fullName}
                            onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
                          />
                        ) : (
                          <div className="p-3 bg-muted rounded-lg">
                            <p className="font-medium">{profile.fullName}</p>
                          </div>
                        )}
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="username">Username</Label>
                        <div className="p-3 bg-muted rounded-lg">
                          <p className="font-mono font-medium">@{profile.username}</p>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="phoneNumber">Phone Number</Label>
                        {editing ? (
                          <Input
                            id="phoneNumber"
                            value={editForm.phoneNumber}
                            onChange={(e) => setEditForm({ ...editForm, phoneNumber: e.target.value })}
                          />
                        ) : (
                          <div className="p-3 bg-muted rounded-lg">
                            <p className="font-medium">{profile.phoneNumber}</p>
                          </div>
                        )}
                      </div>

                      <div className="space-y-2">
                        <Label>Account Status</Label>
                        <div className="p-3 bg-muted rounded-lg">
                          {getStatusBadge(profile.status)}
                        </div>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label>Member Since</Label>
                        <div className="p-3 bg-muted rounded-lg">
                          <div className="flex items-center space-x-2">
                            <Calendar className="h-4 w-4 text-muted-foreground" />
                            <p className="text-sm">{formatDate(profile.createdAt)}</p>
                          </div>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <Label>Last Login</Label>
                        <div className="p-3 bg-muted rounded-lg">
                          <div className="flex items-center space-x-2">
                            <Shield className="h-4 w-4 text-muted-foreground" />
                            <p className="text-sm">
                              {profile.lastLoginAt ? formatDate(profile.lastLoginAt) : 'Never'}
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Account Statistics */}
                <Card>
                  <CardHeader>
                    <CardTitle>Account Statistics</CardTitle>
                    <CardDescription>Your investment activity summary</CardDescription>
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
                {/* Account Security */}
                <Card>
                  <CardHeader>
                    <CardTitle>Security</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <Label>Password</Label>
                      <Button variant="outline" className="w-full">
                        Change Password
                      </Button>
                    </div>
                    <div className="space-y-2">
                      <Label>Two-Factor Authentication</Label>
                      <Button variant="outline" className="w-full">
                        Enable 2FA
                      </Button>
                    </div>
                  </CardContent>
                </Card>

                {/* Quick Actions */}
                <Card>
                  <CardHeader>
                    <CardTitle>Quick Actions</CardTitle>
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

                {/* Support */}
                <Card>
                  <CardHeader>
                    <CardTitle>Support</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <Button variant="outline" className="w-full">
                      Contact Support
                    </Button>
                    <Button variant="outline" className="w-full">
                      FAQ
                    </Button>
                    <Button variant="outline" className="w-full">
                      Terms of Service
                    </Button>
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
