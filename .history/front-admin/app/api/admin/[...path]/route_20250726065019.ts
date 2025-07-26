import { NextRequest, NextResponse } from 'next/server'

export async function GET(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  return handleRequest(request, params.path, 'GET')
}

export async function POST(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  return handleRequest(request, params.path, 'POST')
}

export async function PUT(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  return handleRequest(request, params.path, 'PUT')
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  return handleRequest(request, params.path, 'DELETE')
}

async function handleRequest(
  request: NextRequest,
  pathSegments: string[],
  method: string
) {
  try {
    const path = pathSegments.join('/')
    const url = new URL(request.url)
    const backendUrl = `http://localhost:8080/api/admin/${path}${url.search}`

    console.log(`Proxying ${method} request to: ${backendUrl}`)

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    }

    // Forward authorization header if present
    const authHeader = request.headers.get('authorization')
    if (authHeader) {
      headers['Authorization'] = authHeader
      console.log('Authorization header present:', authHeader.substring(0, 20) + '...')
    } else {
      console.log('No authorization header found')
    }

    const requestOptions: RequestInit = {
      method,
      headers,
    }

    // Add body for POST/PUT requests
    if (method === 'POST' || method === 'PUT') {
      const body = await request.json()
      requestOptions.body = JSON.stringify(body)
      console.log('Request body:', body)
    }

    console.log('Making request with options:', { method, headers: Object.keys(headers) })
    const response = await fetch(backendUrl, requestOptions)
    
    console.log('Backend response status:', response.status)
    console.log('Backend response headers:', Object.fromEntries(response.headers.entries()))

    let data
    try {
      const responseText = await response.text()
      console.log('Backend response text:', responseText)
      
      if (responseText) {
        data = JSON.parse(responseText)
        console.log('Backend response data:', data)
      } else {
        data = {}
        console.log('Backend response is empty')
      }
    } catch (parseError) {
      console.error('Failed to parse backend response:', parseError)
      return NextResponse.json(
        { message: `Backend returned invalid JSON: ${parseError.message}` },
        { status: 500 }
      )
    }

    if (!response.ok) {
      console.error('Backend error response:', data)
      return NextResponse.json(
        { message: data.message || data.error || `Backend error: ${response.status}` },
        { status: response.status }
      )
    }

    return NextResponse.json(data)
  } catch (error) {
    console.error(`API error for ${method} /api/admin/${pathSegments.join('/')}:`, error)
    return NextResponse.json(
      { message: error instanceof Error ? error.message : 'Internal server error' },
      { status: 500 }
    )
  }
} 