import { NextResponse } from 'next/server'

export async function GET() {
  try {
    console.log('Health check: Testing backend connectivity...')
    
    // Test basic connectivity
    const testResponse = await fetch('http://localhost:8080/api/auth/test', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    })
    
    console.log('Health check: Backend test response status:', testResponse.status)
    
    if (testResponse.ok) {
      const testData = await testResponse.json()
      console.log('Health check: Backend test data:', testData)
      
      return NextResponse.json({
        status: 'healthy',
        backend: 'connected',
        testData
      })
    } else {
      return NextResponse.json({
        status: 'unhealthy',
        backend: 'error',
        error: `Backend returned ${testResponse.status}`
      }, { status: 503 })
    }
  } catch (error) {
    console.error('Health check: Backend connection failed:', error)
    return NextResponse.json({
      status: 'unhealthy',
      backend: 'unreachable',
      error: error instanceof Error ? error.message : 'Unknown error'
    }, { status: 503 })
  }
} 