"use client"

import { useEffect, useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuth } from "@/contexts/auth-context"
import { User, Shield, Phone, Calendar } from "lucide-react"

interface AdminProfile {
  id: number
  username: string
  phoneNumber: string
  role: string
  createdAt: string
  lastLogin?: string
}

export default function ProfilePage() {
  const { user } = useAuth()
  const [profile, setProfile] = useState<AdminProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const [isEditing, setIsEditing] = useState(false)
  const [editForm, setEditForm] = useState({
    username: "",
    phoneNumber: ""
  })

  useEffect(() => {
    loadProfile()
  }, [])

  const loadProfile = async () => {
    try {
      setLoading(true)
      // For now, we'll use the user data from auth context
      // In a real app, you might want to fetch additional profile data from the backend
      if (user) {
        const profileData: AdminProfile = {
          id: user.id,
          username: user.username,
          phoneNumber: "1234567890", // This would come from backend
          role: user.role,
          createdAt: new Date().toISOString(), // This would come from backend
          lastLogin: new Date().toISOString() // This would come from backend
        }
        setProfile(profileData)
        setEditForm({
          username: profileData.username,
          phoneNumber: profileData.phoneNumber
        })
      }
    } catch (err: any) {
      console.error("Error loading profile:", err)
      setError(err.message || "Failed to load profile")
    } finally {
      setLoading(false)
    }
  }

  const handleSaveProfile = async () => {
    try {
      // Here you would typically make an API call to update the profile
      console.log("Saving profile:", editForm)
      
      // For now, just update the local state
      if (profile) {
        setProfile({
          ...profile,
          username: editForm.username,
          phoneNumber: editForm.phoneNumber
        })
      }
      
      setIsEditing(false)
      setError("")
    } catch (err: any) {
      console.error("Error saving profile:", err)
      setError(err.message || "Failed to save profile")
    }
  }

  const handleCancelEdit = () => {
    if (profile) {
      setEditForm({
        username: profile.username,
        phoneNumber: profile.phoneNumber
      })
    }
    setIsEditing(false)
    setError("")
  }

  if (loading) {
    return (
      <div className="space-y-6 p-6">
        <DashboardHeader title="Profile" description="Manage your admin profile" />
        <div className="grid gap-6">
          <Card>
            <CardHeader>
              <Skeleton className="h-6 w-32" />
            </CardHeader>
            <CardContent className="space-y-4">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-3/4" />
              <Skeleton className="h-4 w-1/2" />
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="space-y-6 p-6">
        <DashboardHeader title="Profile" description="Manage your admin profile" />
        <Alert variant="destructive">
          <AlertDescription>Profile not found</AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-6 p-6">
      <DashboardHeader title="Profile" description="Manage your admin profile" />

      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-6">
        {/* Profile Information */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="h-5 w-5" />
              Profile Information
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="username">Username</Label>
                {isEditing ? (
                  <Input
                    id="username"
                    value={editForm.username}
                    onChange={(e) => setEditForm({ ...editForm, username: e.target.value })}
                  />
                ) : (
                  <div className="text-sm font-medium py-2">{profile.username}</div>
                )}
              </div>
              <div>
                <Label htmlFor="phoneNumber">Phone Number</Label>
                {isEditing ? (
                  <Input
                    id="phoneNumber"
                    value={editForm.phoneNumber}
                    onChange={(e) => setEditForm({ ...editForm, phoneNumber: e.target.value })}
                  />
                ) : (
                  <div className="text-sm font-medium py-2 flex items-center gap-2">
                    <Phone className="h-4 w-4" />
                    {profile.phoneNumber}
                  </div>
                )}
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label>Role</Label>
                <div className="text-sm font-medium py-2 flex items-center gap-2">
                  <Shield className="h-4 w-4" />
                  {profile.role}
                </div>
              </div>
              <div>
                <Label>Member Since</Label>
                <div className="text-sm font-medium py-2 flex items-center gap-2">
                  <Calendar className="h-4 w-4" />
                  {new Date(profile.createdAt).toLocaleDateString()}
                </div>
              </div>
            </div>

            {profile.lastLogin && (
              <div>
                <Label>Last Login</Label>
                <div className="text-sm font-medium py-2">
                  {new Date(profile.lastLogin).toLocaleString()}
                </div>
              </div>
            )}

            <div className="flex gap-2 pt-4">
              {isEditing ? (
                <>
                  <Button onClick={handleSaveProfile}>Save Changes</Button>
                  <Button variant="outline" onClick={handleCancelEdit}>
                    Cancel
                  </Button>
                </>
              ) : (
                <Button onClick={() => setIsEditing(true)}>Edit Profile</Button>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Security Information */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="h-5 w-5" />
              Security Information
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label>Account Status</Label>
                <div className="text-sm font-medium py-2 text-green-600">Active</div>
              </div>
              <div>
                <Label>Two-Factor Authentication</Label>
                <div className="text-sm font-medium py-2 text-gray-600">Not enabled</div>
              </div>
            </div>
            <Button variant="outline">Change Password</Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 