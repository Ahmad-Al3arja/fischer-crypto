"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"

export default function DebugPage() {
  const [results, setResults] = useState<any[]>([])
  const [loading, setLoading] = useState(false)

  const addResult = (title: string, data: any, error?: boolean) => {
    setResults(prev => [...prev, { title, data, error, timestamp: new Date().toISOString() }])
  }

  const testLogin = async () => {
    setLoading(true)
    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phoneNumber: "1234567890", password: "admin123" })
      })
      
      const data = await response.json()
      addResult("Login Test", { status: response.status, data })
    } catch (error) {
      addResult("Login Test", { error: error.message }, true)
    } finally {
      setLoading(false)
    }
  }

  const testPromoCodes = async () => {
    setLoading(true)
    try {
      const response = await fetch("/api/admin/promo-codes")
      const data = await response.json()
      addResult("Promo Codes Test", { status: response.status, data })
    } catch (error) {
      addResult("Promo Codes Test", { error: error.message }, true)
    } finally {
      setLoading(false)
    }
  }

  const testTogglePromoCode = async () => {
    setLoading(true)
    try {
      // First get promo codes to find an ID
      const response = await fetch("/api/admin/promo-codes")
      const data = await response.json()
      
      if (data.promoCodes && data.promoCodes.length > 0) {
        const firstPromoCode = data.promoCodes[0]
        addResult("Found Promo Code", { id: firstPromoCode.id, code: firstPromoCode.code })
        
        // Try to toggle it
        const toggleResponse = await fetch(`/api/admin/promo-codes/${firstPromoCode.id}/toggle`, {
          method: "POST"
        })
        const toggleData = await toggleResponse.json()
        addResult("Toggle Promo Code Test", { status: toggleResponse.status, data: toggleData })
      } else {
        addResult("Toggle Promo Code Test", { error: "No promo codes found" }, true)
      }
    } catch (error) {
      addResult("Toggle Promo Code Test", { error: error.message }, true)
    } finally {
      setLoading(false)
    }
  }

  const testBackendDirect = async () => {
    setLoading(true)
    try {
      const response = await fetch("http://localhost:8080/api/auth/test")
      const data = await response.json()
      addResult("Backend Direct Test", { status: response.status, data })
    } catch (error) {
      addResult("Backend Direct Test", { error: error.message }, true)
    } finally {
      setLoading(false)
    }
  }

  const clearResults = () => {
    setResults([])
  }

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-2xl font-bold">API Debug Page</h1>
      
      <div className="flex space-x-4">
        <Button onClick={testLogin} disabled={loading}>
          Test Login
        </Button>
        <Button onClick={testPromoCodes} disabled={loading}>
          Test Promo Codes
        </Button>
        <Button onClick={testTogglePromoCode} disabled={loading}>
          Test Toggle Promo Code
        </Button>
        <Button onClick={testBackendDirect} disabled={loading}>
          Test Backend Direct
        </Button>
        <Button onClick={clearResults} variant="outline">
          Clear Results
        </Button>
      </div>

      <div className="space-y-4">
        {results.map((result, index) => (
          <Card key={index}>
            <CardHeader>
              <CardTitle className={result.error ? "text-red-600" : "text-green-600"}>
                {result.title}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <pre className="bg-gray-100 p-4 rounded text-sm overflow-auto">
                {JSON.stringify(result.data, null, 2)}
              </pre>
              <p className="text-xs text-gray-500 mt-2">
                {new Date(result.timestamp).toLocaleTimeString()}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
} 