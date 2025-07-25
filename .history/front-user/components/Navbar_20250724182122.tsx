// front-user/components/Navbar.tsx
"use client"

import { useAuth } from "@/contexts/AuthContext"
import { Button } from "@/components/ui/button"
import { LogOut, User, Home } from "lucide-react"
import Link from "next/link"

export default function Navbar() {
  const { user, logout } = useAuth()

  return (
    <nav className="bg-card border-b border-border shadow-lg sticky top-0 z-50 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link href="/dashboard" className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                <Home className="h-5 w-5 text-primary-foreground font-bold" />
              </div>
              <span className="font-bold text-2xl text-foreground tracking-wider">
                FISCHER
              </span>
            </Link>
          </div>

          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-3 bg-secondary px-4 py-2 rounded-lg">
              <div className="w-6 h-6 bg-primary rounded-full flex items-center justify-center">
                <User className="h-3 w-3 text-primary-foreground" />
              </div>
              <span className="text-sm text-foreground font-medium">{user?.username}</span>
            </div>
            <Button 
              variant="outline" 
              size="sm" 
              onClick={logout} 
              className="bg-destructive/10 border-destructive/20 text-destructive hover:bg-destructive hover:text-destructive-foreground transition-all duration-200"
            >
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </Button>
          </div>
        </div>
      </div>
    </nav>
  )
}