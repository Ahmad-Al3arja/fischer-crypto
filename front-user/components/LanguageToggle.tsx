"use client"

import { useLanguage } from "@/contexts/LanguageContext"
import { Button } from "@/components/ui/button"
import { Languages } from "lucide-react"

export function LanguageToggle() {
  const { language, toggleLanguage } = useLanguage()

  return (
    <Button
      variant="outline"
      size="icon"
      onClick={toggleLanguage}
      className="h-9 w-9 rounded-md border border-border bg-card hover:bg-muted"
    >
      <Languages className="h-4 w-4 text-foreground" />
      <span className="sr-only">Toggle language</span>
    </Button>
  )
} 
