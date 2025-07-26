"use client"

import { useAuth } from "@/contexts/AuthContext"
import { useLanguage } from "@/contexts/LanguageContext"
import { Button } from "@/components/ui/button"
import { LogOut, User, Menu } from "lucide-react"
import Link from "next/link"
import { useState } from "react"
import { LanguageToggle } from "./LanguageToggle"

export default function Navbar() {
  const { user, logout } = useAuth()
  const { t } = useLanguage()
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  return (
         <nav className="bg-black border-b border-gray-800 shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link href="/dashboard" className="flex items-center space-x-2">
              <span className="font-bold text-xl text-foreground">FISCHER</span>
            </Link>
          </div>

          <div className="flex items-center space-x-2">
            {/* Desktop view */}
                         <div className="hidden md:flex items-center space-x-2">
               <div className="flex items-center space-x-2 px-3 py-2 text-sm text-white">
                 <User className="h-4 w-4 text-gray-300" />
                 <span>{user?.username}</span>
               </div>
              
              <LanguageToggle />
              
                             <Button 
                 variant="ghost" 
                 size="sm" 
                 onClick={logout} 
                 className="flex items-center space-x-2 text-gray-300 hover:text-white"
               >
                <LogOut className="h-4 w-4" />
                <span className="hidden sm:inline">{t('logout')}</span>
              </Button>
            </div>

            {/* Mobile menu button */}
            <Button 
              variant="ghost" 
              size="sm" 
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="md:hidden"
            >
              <Menu className="h-4 w-4" />
            </Button>
          </div>
        </div>

        {/* Mobile menu */}
        {isMenuOpen && (
          <div className="md:hidden border-t border-border">
            <div className="px-2 pt-2 pb-3 space-y-1">
                             <div className="flex items-center space-x-2 px-3 py-2 text-sm text-white">
                 <User className="h-4 w-4 text-gray-300" />
                 <span>{user?.username}</span>
               </div>
              
              <div className="flex items-center space-x-2 px-3 py-2">
                <LanguageToggle />
              </div>
              
                             <Button 
                 variant="ghost" 
                 size="sm" 
                 onClick={logout} 
                 className="w-full justify-start text-gray-300 hover:text-white"
               >
                <LogOut className="h-4 w-4 mr-2" />
                {t('logout')}
              </Button>
            </div>
          </div>
        )}
      </div>
    </nav>
  )
} 
