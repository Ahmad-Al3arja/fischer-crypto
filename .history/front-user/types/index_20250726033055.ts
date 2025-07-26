export interface User {
  id: number
  fullName: string
  username: string
  phoneNumber: string
  status: string
  totalDeposits: number
  totalWithdrawals: number
  totalProfits: number
  createdAt: string
}

export interface DashboardData {
  totalBalance: number
  availableBalance: number
  totalDeposits: number
  totalWithdrawals: number
  totalProfits: number
  activeDeposits: number
  pendingWithdrawals: number
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

// Frontend Plan interface (converted from backend)
export interface FrontendPlan {
  id: number
  name: string
  description: string
  price: number
  monthlyProfit: number
  dailyProfitMin: number
  dailyProfitMax: number
  planLevel: number
  minAmount: number
  maxAmount: number
  duration: number
  dailyProfit: number
  totalProfit: number
  features: string[]
  isPopular?: boolean
}

// Backend API response structure
export interface PlansApiResponse {
  plans: Plan[]
}

// Function to convert backend plan to frontend plan
export const convertBackendPlanToFrontend = (backendPlan: Plan): FrontendPlan => {
  // Calculate average daily profit from min and max (these are dollar amounts, not percentages)
  const avgDailyProfit = (backendPlan.dailyProfitMin + backendPlan.dailyProfitMax) / 2
  const duration = 30 // Default duration in days
  
  return {
    id: backendPlan.id,
    name: backendPlan.name,
    description: `Level ${backendPlan.planLevel} Investment Plan`,
    price: backendPlan.price,
    monthlyProfit: backendPlan.monthlyProfit,
    dailyProfitMin: backendPlan.dailyProfitMin,
    dailyProfitMax: backendPlan.dailyProfitMax,
    planLevel: backendPlan.planLevel,
    minAmount: backendPlan.price,
    maxAmount: backendPlan.price * 10, // 10x the base price
    duration: duration,
    dailyProfit: avgDailyProfit,
    totalProfit: backendPlan.monthlyProfit, // Use monthly profit from database
    features: [], // No extra features, keep it simple
    isPopular: backendPlan.planLevel === 2 // Level 2 is most popular
  }
}

export interface ReferralStats {
  totalReferrals: number
  activeReferrals: number
  totalEarnings: number
  referralCode: string
  referralLink: string
}

export interface ReferralUser {
  id: number
  username: string
  fullName: string
  phoneNumber: string
  status: string
  totalDeposits: number
  totalProfits: number
  joinedAt: string
}

export interface DepositInfo {
  minDeposit: number
  maxDeposit: number
  processingTime: string
}

export interface WalletInfo {
  usdtAddress: string
  isSet: boolean
}

export interface BalanceInfo {
  totalBalance: number
  frozenBalance: number
  withdrawableBalance: number
  referralEarnings: number
  availableBalance?: number // Keep for backward compatibility
  pendingWithdrawals?: number // Keep for backward compatibility
}

export interface Transaction {
  id: number
  type: 'deposit' | 'withdrawal'
  amount: number
  status: string
  createdAt: string
  planName?: string
  walletAddress?: string
}
