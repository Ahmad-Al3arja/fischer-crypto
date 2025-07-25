export interface User {
  id: number
  username: string
  role: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  userId: number
  username: string
  role: string
}

export interface RegisterData {
  fullName: string
  username: string
  phoneNumber: string
  password: string
  confirmPassword: string
  referralCode: string
}

export interface Plan {
  id: number
  name: string
  price: number
  monthlyProfit: number
  dailyProfitMin: number
  dailyProfitMax: number
  planLevel: number
}

export interface DashboardData {
  fullName: string
  username: string
  phoneNumber: string
  currentPlanName: string
  totalBalance: number
  totalProfits: number
  dailyProfit: number
  counterStatus: {
    isActive: boolean
    isCompleted: boolean
    remainingSeconds: number
    needsReset: boolean
  }
  activationPending: false
  activationMessage?: string
}

export interface BalanceData {
  totalBalance: number
  frozenBalance: number
  withdrawableBalance: number
  referralEarnings: number
}

export interface WithdrawalItem {
  id: number
  amount: number
  fee: number
  netAmount: number
  walletAddress: string
  status: string
  createdAt: string
  processedAt?: string
  rejectionNote?: string
}
