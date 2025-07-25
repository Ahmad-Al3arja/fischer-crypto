const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api"

class ApiService {
  private authToken: string | null = null

  setAuthToken(token: string | null) {
    this.authToken = token
  }

  private async request(endpoint: string, options: RequestInit = {}) {
    const url = `${API_BASE_URL}${endpoint}`
    const headers: HeadersInit = {
      "Content-Type": "application/json",
      ...options.headers,
    }

    if (this.authToken) {
      headers.Authorization = `Bearer ${this.authToken}`
    }

    const response = await fetch(url, {
      ...options,
      headers,
    })

    if (!response.ok) {
      let errorMessage = `HTTP error! status: ${response.status}`
      
      try {
        const errorData = await response.json()
        errorMessage = errorData.message || errorData.error || errorMessage
      } catch (e) {
        // If we can't parse JSON, use the status text
        errorMessage = response.statusText || errorMessage
      }
      
      // Add status code to the error message for debugging
      if (response.status === 401) {
        errorMessage = `Authentication failed (401): ${errorMessage}`
      } else if (response.status === 403) {
        errorMessage = `Access denied (403): ${errorMessage}`
      }
      
      throw new Error(errorMessage)
    }

    return response.json()
  }

  // Auth endpoints
  async login(data: { phoneNumber: string; password: string }) {
    return this.request("/auth/login", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async register(data: {
    fullName: string
    username: string
    phoneNumber: string
    password: string
    confirmPassword: string
    referralCode: string
  }) {
    return this.request("/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  // User endpoints
  async getDashboard() {
    return this.request("/user/dashboard")
  }

  async getProfile() {
    return this.request("/user/profile")
  }

  async updateProfile(data: { fullName: string; username: string }) {
    return this.request("/user/profile", {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async getReferralStats() {
    return this.request("/user/team-stats")
  }

  async getDailyCounterStatus() {
    return this.request("/user/daily-counter/status")
  }

  async activateCounter() {
    return this.request("/user/daily-counter/activate", { method: "POST" })
  }

  async completeCounter() {
    return this.request("/user/daily-counter/complete", { method: "POST" })
  }

  // Transaction endpoints
  async createDeposit(data: { amount: number; planId: number; promoCode?: string }) {
    return this.request("/transactions/deposits", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async getDepositHistory() {
    return this.request("/transactions/deposits")
  }

  async createWithdrawal(data: { amount: number; walletAddress?: string }) {
    return this.request("/transactions/withdrawals", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async getWithdrawalHistory() {
    return this.request("/transactions/withdrawals")
  }

  async saveWallet(data: { usdtAddress: string }) {
    return this.request("/transactions/wallet/address", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async getWallet() {
    return this.request("/transactions/wallet")
  }

  // Plans endpoints
  async getPlans() {
    return this.request("/plans")
  }

  async getPlan(id: number) {
    return this.request(`/plans/${id}`)
  }
}

export const apiService = new ApiService()
