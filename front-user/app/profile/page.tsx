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
    username: "",
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
        username: validProfile.username,
      })
    } catch (err: any) {
      console.error("Error fetching profile:", err)
      setError(err.message || "Failed to load profile")
      // Keep default profile on error
      setProfile(defaultProfile)
      setEditForm({
        fullName: defaultProfile.fullName,
        username: defaultProfile.username,
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
      username: profile?.username || "",
    })
  }

  const handleSave = async () => {
    if (!profile) return

    // Validate required fields
    if (!editForm.fullName.trim()) {
      setError("Full name is required")
      return
    }

    if (!editForm.username.trim()) {
      setError("Username is required")
      return
    }

    setSaving(true)
    setError("")
    
    try {
      // Call the API to update the profile
      const updatedProfile = await apiService.updateProfile({
        fullName: editForm.fullName.trim(),
        username: editForm.username.trim(),
      })
      
      // Update the local state with the response from the server
      setProfile({
        ...profile,
        fullName: updatedProfile.fullName || editForm.fullName,
        username: updatedProfile.username || editForm.username,
      })
      setEditing(false)
      
      toast({
        title: "Success!",
        description: "Profile updated successfully",
      })
    } catch (err: any) {
      
      // Handle specific error cases
      if (err.message?.includes("Unauthorized") || err.message?.includes("401")) {
        setError("Authentication failed. Please log in again.")
        // Optionally redirect to login
        // router.push("/login")
      } else if (err.message?.includes("Username already taken")) {
        setError("Username is already taken. Please choose a different username.")
      } else {
        setError(err.message || "Failed to update profile")
      }
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
        <div className="min-h-screen bg-black flex items-center justify-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-400"></div>
        </div>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-black">
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
                <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="text-foreground">{t('personal_information')}</CardTitle>
                        <CardDescription className="text-muted-foreground">{t('account_details')}</CardDescription>
                      </div>
                      {!editing ? (
                        <Button variant="outline" size="sm" onClick={handleEdit}>
                          <Edit className="h-4 w-4 mr-2" />
                          {t('edit')}
                        </Button>
                      ) : (
                        <div className="flex space-x-2">
                          <Button size="sm" onClick={handleSave} disabled={saving} className="bg-gradient-to-r from-gray-700 to-custom-2e2e2e hover:from-gray-600 hover:to-gray-700 text-foreground">
                            {saving ? (
                              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                            ) : (
                              <>
                                <Save className="h-4 w-4 mr-2" />
                                {t('save')}
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
                        <Label htmlFor="fullName" className="text-foreground">{t('full_name')}</Label>
                        {editing ? (
                          <Input
                            id="fullName"
                            value={editForm.fullName}
                            onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
                            className="bg-gradient-to-r bg-card border-border text-foreground"
                          />
                        ) : (
                          <div className="p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                            <p className="font-medium text-foreground">{profile.fullName}</p>
                          </div>
                        )}
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="username" className="text-foreground">{t('username_label')}</Label>
                        {editing ? (
                          <Input
                            id="username"
                            value={editForm.username}
                            onChange={(e) => setEditForm({ ...editForm, username: e.target.value })}
                            className="bg-gradient-to-r bg-card border-border text-foreground"
                          />
                        ) : (
                          <div className="p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                            <p className="font-mono font-medium text-foreground">@{profile.username}</p>
                          </div>
                        )}
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="phoneNumber" className="text-foreground">{t('phone_number')}</Label>
                        <div className="p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                          <p className="font-medium text-foreground">{profile.phoneNumber}</p>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <Label className="text-foreground">{t('account_status')}</Label>
                        <div className="p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                          {getStatusBadge(profile.status)}
                        </div>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label className="text-foreground">{t('member_since')}</Label>
                        <div className="p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                          <div className="flex items-center space-x-2">
                            <Calendar className="h-4 w-4 text-muted-foreground" />
                            <p className="text-sm text-foreground">{formatDate(profile.createdAt)}</p>
                          </div>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <Label className="text-foreground">{t('last_login')}</Label>
                        <div className="p-3 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
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
                <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
                  <CardHeader>
                    <CardTitle className="text-foreground">{t('account_statistics')}</CardTitle>
                    <CardDescription className="text-muted-foreground">{t('investment_activity')}</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div className="text-center p-4 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                        <p className="text-sm text-muted-foreground mb-1">{t('total_deposits')}</p>
                        <p className="text-2xl font-bold text-foreground">${profile.totalDeposits.toFixed(2)}</p>
                      </div>
                      <div className="text-center p-4 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                        <p className="text-sm text-muted-foreground mb-1">{t('total_withdrawals')}</p>
                        <p className="text-2xl font-bold text-green-400">${profile.totalWithdrawals.toFixed(2)}</p>
                      </div>
                      <div className="text-center p-4 bg-gradient-to-r from-custom-2e2e2e to-gray-900 rounded-lg">
                        <p className="text-sm text-muted-foreground mb-1">{t('total_profits')}</p>
                        <p className="text-2xl font-bold text-green-600">${profile.totalProfits.toFixed(2)}</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Sidebar */}
              <div className="space-y-6">
                {/* Quick Actions */}
                <Card className="bg-gradient-to-br bg-card border-border shadow-lg">
                  <CardHeader>
                    <CardTitle className="text-foreground">{t('quick_actions')}</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <Link href="/deposit">
                      <Button className="w-full" variant="outline">
                        {t('make_deposit')}
                      </Button>
                    </Link>
                    <Link href="/withdraw">
                      <Button className="w-full" variant="outline">
                        {t('withdraw_funds')}
                      </Button>
                    </Link>
                    <Link href="/referrals">
                      <Button className="w-full" variant="outline">
                        {t('view_referrals')}
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
