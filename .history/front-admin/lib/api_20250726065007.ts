class ApiClient {
  private getAuthHeaders() {
    const token = localStorage.getItem("admin_token")
    return {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
    }
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const response = await fetch(endpoint, {
      ...options,
      headers: {
        ...this.getAuthHeaders(),
        ...options.headers,
      },
    })

    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}`
      try {
        const errorData = await response.json()
        errorMessage = errorData.message || errorData.error || errorMessage
      } catch (parseError) {
        // If JSON parsing fails, try to get text content
        try {
          const textContent = await response.text()
          if (textContent) {
            errorMessage = textContent
          }
        } catch (textError) {
          // If all else fails, use default message
          errorMessage = `HTTP ${response.status}: ${response.statusText || 'Unknown error'}`
        }
      }
      throw new Error(errorMessage)
    }

    try {
      return await response.json()
    } catch (parseError) {
      throw new Error(`Failed to parse response: ${parseError.message}`)
    }
  }

  // Auth endpoints
  async login(phoneNumber: string, password: string) {
    return this.request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ phoneNumber, password }),
    })
  }

  // Dashboard stats
  async getDashboardStats() {
    return this.request("/api/admin/stats/overview")
  }

  // User management
  async getUsers(planId?: number) {
    const params = planId ? `?planId=${planId}` : ""
    return this.request(`/api/admin/users${params}`)
  }

  async getUserDetails(userId: number) {
    return this.request(`/api/admin/users/${userId}`)
  }

  async activateUser(userId: number) {
    return this.request(`/api/admin/users/${userId}/activate`, { method: "POST" })
  }

  async suspendUser(userId: number) {
    return this.request(`/api/admin/users/${userId}/suspend`, { method: "POST" })
  }

  async updateUserBalance(userId: number, amount: number, reason: string) {
    return this.request(`/api/admin/users/${userId}/balance`, {
      method: "POST",
      body: JSON.stringify({ amount, reason }),
    })
  }

  async updateUserReferralLimit(userId: number, limit: number) {
    return this.request(`/api/admin/users/${userId}/referral-limit`, {
      method: "POST",
      body: JSON.stringify({ limit }),
    })
  }

  async getUserReferralUsage(userId: number) {
    return this.request(`/api/admin/users/${userId}/referral-usage`)
  }

  // Deposit management
  async getDeposits(status?: string) {
    const params = status ? `?status=${status}` : ""
    return this.request(`/api/admin/deposits${params}`)
  }

  async approveDeposit(depositId: number) {
    return this.request(`/api/admin/deposits/${depositId}/approve`, { method: "POST" })
  }

  async rejectDeposit(depositId: number) {
    return this.request(`/api/admin/deposits/${depositId}/reject`, { method: "POST" })
  }

  // Withdrawal management
  async getWithdrawals(status?: string) {
    const params = status ? `?status=${status}` : ""
    return this.request(`/api/admin/withdrawals${params}`)
  }

  async approveWithdrawal(withdrawalId: number) {
    return this.request(`/api/admin/withdrawals/${withdrawalId}/approve`, { method: "POST" })
  }

  async rejectWithdrawal(withdrawalId: number, reason: string) {
    return this.request(`/api/admin/withdrawals/${withdrawalId}/reject`, {
      method: "POST",
      body: JSON.stringify({ reason }),
    })
  }

  // Plan management
  async getPlans() {
    return this.request("/api/admin/plans")
  }

  async createPlan(plan: any) {
    return this.request("/api/admin/plans", {
      method: "POST",
      body: JSON.stringify(plan),
    })
  }

  async updatePlan(planId: number, plan: any) {
    return this.request(`/api/admin/plans/${planId}`, {
      method: "PUT",
      body: JSON.stringify(plan),
    })
  }

  async deletePlan(planId: number) {
    return this.request(`/api/admin/plans/${planId}`, { method: "DELETE" })
  }

  // Promo code management
  async getPromoCodes() {
    return this.request("/api/admin/promo-codes")
  }

  async createPromoCode(promoCode: any) {
    return this.request("/api/admin/promo-codes", {
      method: "POST",
      body: JSON.stringify(promoCode),
    })
  }

  async togglePromoCode(promoCodeId: number) {
    return this.request(`/api/admin/promo-codes/${promoCodeId}/toggle`, { method: "POST" })
  }

  // Wallet change requests
  async getWalletChangeRequests() {
    return this.request("/api/admin/wallet-change-requests")
  }

  async getPendingWalletChangeRequests() {
    return this.request("/api/admin/wallet-change-requests/pending")
  }

  async approveWalletChangeRequest(requestId: number, adminNotes: string) {
    return this.request(`/api/admin/wallet-change-requests/${requestId}/approve`, {
      method: "POST",
      body: JSON.stringify({ adminNotes }),
    })
  }

  async rejectWalletChangeRequest(requestId: number, adminNotes: string) {
    return this.request(`/api/admin/wallet-change-requests/${requestId}/reject`, {
      method: "POST",
      body: JSON.stringify({ adminNotes }),
    })
  }

  // Settings
  async getSettings() {
    return this.request("/api/admin/settings")
  }

  async toggleMaintenanceMode(enabled: boolean) {
    return this.request("/api/admin/settings/maintenance", {
      method: "POST",
      body: JSON.stringify({ enabled }),
    })
  }

  async updateAboutContent(content: string) {
    return this.request("/api/admin/settings/about", {
      method: "POST",
      body: JSON.stringify({ content }),
    })
  }

  async getDefaultUsageLimit() {
    return this.request("/api/admin/settings/default-usage-limit")
  }

  async updateDefaultUsageLimit(usageLimit: number) {
    return this.request("/api/admin/settings/default-usage-limit", {
      method: "POST",
      body: JSON.stringify({ usageLimit }),
    })
  }

  // Daily counter management
  async activateUserCounter(userId: number) {
    return this.request(`/api/admin/users/${userId}/counter/activate`, { method: "POST" })
  }

  async deactivateUserCounter(userId: number) {
    return this.request(`/api/admin/users/${userId}/counter/deactivate`, { method: "POST" })
  }
}

export const apiClient = new ApiClient()
