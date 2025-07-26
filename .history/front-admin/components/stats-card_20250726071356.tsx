import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import type { LucideIcon } from "lucide-react"
import { cn } from "@/lib/utils"

interface StatsCardProps {
  title: string
  value: string | number
  description?: string
  icon: LucideIcon
  trend?: {
    value: number
    isPositive: boolean
  }
  className?: string
}

export function StatsCard({ title, value, description, icon: Icon, trend, className }: StatsCardProps) {
  return (
    <Card className={cn("", className)}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-xs sm:text-sm font-medium truncate">{title}</CardTitle>
        <Icon className="h-4 w-4 text-muted-foreground flex-shrink-0" />
      </CardHeader>
      <CardContent>
        <div className="text-xl sm:text-2xl font-bold truncate">{value}</div>
        {description && <p className="text-xs text-muted-foreground truncate">{description}</p>}
        {trend && (
          <div className="flex items-center pt-1">
            <span className={cn("text-xs font-medium", trend.isPositive ? "text-green-600" : "text-red-600")}>
              {trend.isPositive ? "+" : ""}
              {trend.value}%
            </span>
            <span className="text-xs text-muted-foreground ml-1 hidden sm:inline">from last month</span>
            <span className="text-xs text-muted-foreground ml-1 sm:hidden">vs last month</span>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
