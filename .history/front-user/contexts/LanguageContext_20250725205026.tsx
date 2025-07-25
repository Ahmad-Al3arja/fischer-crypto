"use client"

import React, { createContext, useContext, useEffect, useState } from 'react'

type Language = 'en' | 'ar'

interface LanguageContextType {
  language: Language
  toggleLanguage: () => void
  t: (key: string) => string
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined)

// Translation dictionary
const translations = {
  en: {
    // Dashboard
    'dashboard': 'Dashboard',
    'welcome': 'Welcome',
    'total_balance': 'Total Balance',
    'daily_profit': 'Daily Profit',
    'activate_timer': 'Activate Timer',
    'complete_timer': 'Complete Timer',
    'choose_plan': 'Choose a Plan',
    'timer_active': 'Timer Active',
    'timer_completed': 'Timer Completed',
    'start_new_timer': 'Start New Timer',
    'daily_timer': 'Daily Timer',
    'current_plan': 'Current Plan',
    'no_active_plan': 'No active plan',
    'claim_profit': 'Claim Profit',
    'claiming': 'Claiming...',
    'activating': 'Activating...',
    'timer_active_status': 'Timer Active',
    'come_back_later': 'Come back when the timer completes!',
    'ready_to_claim': 'Ready to Claim!',
    'countdown': 'Countdown',
    
    // Navigation
    'profile': 'Profile',
    'deposit': 'Deposit',
    'withdraw': 'Withdraw',
    'referrals': 'Referrals',
    'plans': 'Plans',
    'wallet': 'Wallet',
    'logout': 'Logout',
    'back_to_dashboard': 'Back to Dashboard',
    
    // Plans
    'investment_plans': 'Investment Plans',
    'choose_your_plan': 'Choose Your Investment Plan',
    'select_perfect_plan': 'Select the perfect investment plan that matches your goals.',
    'investment_range': 'Investment Range',
    'duration': 'Duration',
    'daily_profit': 'Daily Profit',
    'total_profit': 'Total Profit',
    'invest_now': 'Invest Now',
    'most_popular': 'Most Popular',
    'level': 'Level',
    'investment_amount': 'Investment Amount',
    'daily_profit_range': 'Daily Profit Range',
    'avg': 'Avg',
    'monthly_profit': 'Monthly Profit',
    'return': 'Return',
    'plan_level': 'Plan Level',
    'investment': 'Investment',
    'demo_mode': 'Demo Mode',
    'no_plans_available': 'No plans available at the moment.',
    'how_investment_works': 'How Investment Works',
    'understanding_investment_process': 'Understanding the investment process',
    'choose_your_plan_step': 'Choose Your Plan',
    'choose_plan_description': 'Select an investment plan that matches your goals and budget',
    'make_deposit_step': 'Make Your Deposit',
    'make_deposit_description': 'Deposit your investment amount using secure payment methods',
    'earn_daily_profits_step': 'Earn Daily Profits',
    'earn_daily_profits_description': 'Receive daily profit distributions directly to your account',
    'withdraw_anytime_step': 'Withdraw Anytime',
    'withdraw_anytime_description': 'Withdraw your profits or reinvest for compound growth',
    'investment_benefits': 'Investment Benefits',
    'why_choose_platform': 'Why choose our investment platform',
    'high_returns': 'High Returns',
    'high_returns_description': 'Competitive daily profit rates with transparent calculations',
    'daily_payouts': 'Daily Payouts',
    'daily_payouts_description': 'Receive your profits daily, not monthly or yearly',
    'flexible_terms': 'Flexible Terms',
    'flexible_terms_description': 'Choose from various investment amounts and profit levels',
    'ready_to_start': 'Ready to Start Investing?',
    'join_thousands': 'Join thousands of investors who are already earning daily profits. Start your investment journey today with our secure and transparent platform.',
    'start_investing_now': 'Start Investing Now',
    'refresh': 'Refresh',
    'new_deposit': 'New Deposit',
    'choose_plan_deposit': 'Choose a plan and enter deposit amount',
    'investment_plan': 'Investment Plan',
    'amount_usd': 'Amount (USD)',
    'promo_code': 'Promo Code (Optional)',
    'enter_promo_code': 'Enter promo code',
    'submit_deposit': 'Submit Deposit',
    'plan_details': 'Plan Details',
    'plan_features': 'Plan Features',
    'daily_profit_distribution': 'Daily profit distribution',
    'support_24_7': '24/7 support',
    'secure_transactions': 'Secure transactions',
    'instant_activation': 'Instant activation',
    'deposit_information': 'Deposit Information',
    'minimum_deposit': 'Minimum Deposit',
    'maximum_deposit': 'Maximum Deposit',
    'processing_time': 'Processing Time',
    'instant': 'Instant',
    'withdraw_funds': 'Withdraw Funds',
    'withdraw_earnings': 'Withdraw your earnings to your USDT wallet',
    'enter_withdrawal_amount': 'Enter withdrawal amount',
    'usdt_wallet_address': 'USDT Wallet Address',
    'submit_withdrawal': 'Submit Withdrawal',
    'wallet_management': 'Wallet Management',
    'save_wallet_address': 'Save your USDT wallet address for future withdrawals',
    'no_wallet_address': 'No Wallet Address Set',
    'please_save_wallet': 'Please save your USDT wallet address to enable withdrawals.',
    'new_wallet_address': 'New Wallet Address',
    'save_wallet_address_btn': 'Save Wallet Address',
    'important_notes': 'Important Notes',
    'min_withdrawal': 'Minimum withdrawal amount: $10',
    'max_withdrawal': 'Maximum withdrawal amount: $50,000',
    'withdrawal_processing_time': 'Withdrawal processing time: 24-48 hours',
    'only_usdt_accepted': 'Only USDT (TRC20) wallet addresses are accepted',
    'verify_wallet_address': 'Make sure your wallet address is correct before submitting',
    'withdrawal_history': 'Withdrawal History',
    'transaction_history': 'Transaction History',
    'view_withdrawal_requests': 'View all your withdrawal requests and their current status',
    'no_withdrawals_yet': 'No Withdrawals Yet',
    'no_withdrawal_requests': 'You haven\'t made any withdrawal requests yet.',
    'make_first_withdrawal': 'Make Your First Withdrawal',
    'referral_code': 'Referral Code',
    'bonus_profit': 'Bonus Profit',
    'home': 'Home',
    'investment': 'Investment',
    'wallet': 'Wallet',
    'wallet_settings': 'Wallet Settings',
    'your_wallet_address': 'Your wallet address for withdrawals',
    'need_set_wallet': 'You need to set your USDT wallet address to receive withdrawals.',
    'usdt_wallet_address_trc20': 'USDT Wallet Address (TRC20)',
    'network_information': 'Network Information',
    'network': 'Network',
    'token': 'Token',
    'network_notes': 'Important Notes:',
    'only_trc20_supported': 'Only USDT (TRC20) network is supported',
    'ensure_correct_network': 'Ensure you\'re using the correct network',
    'double_check_address': 'Double-check the address before saving',
    'withdrawals_sent_to': 'Withdrawals will be sent to this address',
    'quick_actions': 'Quick Actions',
    'make_withdrawal': 'Make Withdrawal',
    'view_withdrawal_history': 'View Withdrawal History',
    'security_tips': 'Security Tips',
    'verify_address_before_saving': 'Always verify the wallet address before saving',
    'use_own_wallet': 'Use only your own wallet address',
    'keep_credentials_secure': 'Keep your wallet credentials secure',
    'never_share_private_keys': 'Never share your private keys',
    'personal_information': 'Personal Information',
    'account_details': 'Your account details and preferences',
    'full_name': 'Full Name',
    'username_label': 'Username',
    'phone_number': 'Phone Number',
    'account_status': 'Account Status',
    'member_since': 'Member Since',
    'last_login': 'Last Login',
    'investment_activity': 'Your investment activity summary',
    'min_max': 'Min: $60 | Max: $600',
    'min_max_withdrawal': 'Min: $10 | Max: $50,000',
    
    // Profile
    'account_information': 'Account Information',
    'edit_profile': 'Edit Profile',
    'save_changes': 'Save Changes',
    'cancel': 'Cancel',
    'account_statistics': 'Account Statistics',
    'total_deposits': 'Total Deposits',
    'total_withdrawals': 'Total Withdrawals',
    'total_profits': 'Total Profits',
    'quick_actions': 'Quick Actions',
    'make_deposit': 'Make Deposit',
    'withdraw_funds': 'Withdraw Funds',
    'view_referrals': 'View Referrals',
    
    // Common
    'loading': 'Loading...',
    'error': 'Error',
    'success': 'Success',
    'save': 'Save',
    'edit': 'Edit',
    'delete': 'Delete',
    'confirm': 'Confirm',
    'cancel': 'Cancel',
    'close': 'Close',
    'next': 'Next',
    'previous': 'Previous',
    'submit': 'Submit',
    'reset': 'Reset',
    'search': 'Search',
    'filter': 'Filter',
    'sort': 'Sort',
    'refresh': 'Refresh',
    'copy': 'Copy',
    'copied': 'Copied!',
    
    // Status
    'active': 'Active',
    'pending': 'Pending',
    'completed': 'Completed',
    'cancelled': 'Cancelled',
    'approved': 'Approved',
    'rejected': 'Rejected',
    'processing': 'Processing',
    
    // Time
    'days': 'days',
    'hours': 'hours',
    'minutes': 'minutes',
    'seconds': 'seconds',
    
    // Currency
    'currency': '$',
    
    // Messages
    'no_data_available': 'No data available',
    'try_again_later': 'Please try again later',
    'operation_successful': 'Operation completed successfully',
    'operation_failed': 'Operation failed',
    'network_error': 'Network error occurred',
    'unauthorized': 'Unauthorized access',
    'forbidden': 'Access forbidden',
    'not_found': 'Resource not found',
    'server_error': 'Server error occurred',
  },
  ar: {
    // Dashboard
    'dashboard': 'لوحة التحكم',
    'welcome': 'مرحباً',
    'total_balance': 'الرصيد الإجمالي',
    'daily_profit': 'الربح اليومي',
    'activate_timer': 'تفعيل المؤقت',
    'complete_timer': 'إكمال المؤقت',
    'choose_plan': 'اختر خطة',
    'timer_active': 'المؤقت نشط',
    'timer_completed': 'تم إكمال المؤقت',
    'start_new_timer': 'بدء مؤقت جديد',
    'daily_timer': 'المؤقت اليومي',
    'current_plan': 'الخطة الحالية',
    'no_active_plan': 'لا توجد خطة نشطة',
    'claim_profit': 'استلام الربح',
    'claiming': 'جاري الاستلام...',
    'activating': 'جاري التفعيل...',
    'timer_active_status': 'المؤقت نشط',
    'come_back_later': 'عد عندما يكتمل المؤقت!',
    'ready_to_claim': 'جاهز للاستلام!',
    'countdown': 'العد التنازلي',
    
    // Navigation
    'profile': 'الملف الشخصي',
    'deposit': 'إيداع',
    'withdraw': 'سحب',
    'referrals': 'الإحالات',
    'plans': 'الخطط',
    'wallet': 'المحفظة',
    'logout': 'تسجيل الخروج',
    'back_to_dashboard': 'العودة للوحة التحكم',
    
    // Plans
    'investment_plans': 'خطط الاستثمار',
    'choose_your_plan': 'اختر خطة الاستثمار الخاصة بك',
    'select_perfect_plan': 'اختر خطة الاستثمار المثالية التي تناسب أهدافك.',
    'investment_range': 'نطاق الاستثمار',
    'duration': 'المدة',
    'daily_profit': 'الربح اليومي',
    'total_profit': 'الربح الإجمالي',
    'invest_now': 'استثمر الآن',
    'most_popular': 'الأكثر شعبية',
    'level': 'المستوى',
    'investment_amount': 'مبلغ الاستثمار',
    'daily_profit_range': 'نطاق الربح اليومي',
    'avg': 'متوسط',
    'monthly_profit': 'الربح الشهري',
    'return': 'العائد',
    'plan_level': 'مستوى الخطة',
    'investment': 'الاستثمار',
    'demo_mode': 'وضع العرض',
    'no_plans_available': 'لا توجد خطط متاحة في الوقت الحالي.',
    'how_investment_works': 'كيف يعمل الاستثمار',
    'understanding_investment_process': 'فهم عملية الاستثمار',
    
    // Profile
    'account_information': 'معلومات الحساب',
    'edit_profile': 'تعديل الملف الشخصي',
    'save_changes': 'حفظ التغييرات',
    'cancel': 'إلغاء',
    'account_statistics': 'إحصائيات الحساب',
    'total_deposits': 'إجمالي الإيداعات',
    'total_withdrawals': 'إجمالي السحوبات',
    'total_profits': 'إجمالي الأرباح',
    'quick_actions': 'إجراءات سريعة',
    'make_deposit': 'إجراء إيداع',
    'withdraw_funds': 'سحب الأموال',
    'view_referrals': 'عرض الإحالات',
    
    // Common
    'loading': 'جاري التحميل...',
    'error': 'خطأ',
    'success': 'نجح',
    'save': 'حفظ',
    'edit': 'تعديل',
    'delete': 'حذف',
    'confirm': 'تأكيد',
    'close': 'إغلاق',
    'next': 'التالي',
    'previous': 'السابق',
    'submit': 'إرسال',
    'reset': 'إعادة تعيين',
    'search': 'بحث',
    'filter': 'تصفية',
    'sort': 'ترتيب',
    'refresh': 'تحديث',
    'copy': 'نسخ',
    'copied': 'تم النسخ!',
    
    // Status
    'active': 'نشط',
    'pending': 'قيد الانتظار',
    'completed': 'مكتمل',
    'cancelled': 'ملغي',
    'approved': 'موافق عليه',
    'rejected': 'مرفوض',
    'processing': 'قيد المعالجة',
    
    // Time
    'days': 'أيام',
    'hours': 'ساعات',
    'minutes': 'دقائق',
    'seconds': 'ثواني',
    
    // Currency
    'currency': '$',
    
    // Messages
    'no_data_available': 'لا توجد بيانات متاحة',
    'try_again_later': 'يرجى المحاولة مرة أخرى لاحقاً',
    'operation_successful': 'تم إكمال العملية بنجاح',
    'operation_failed': 'فشلت العملية',
    'network_error': 'حدث خطأ في الشبكة',
    'unauthorized': 'وصول غير مصرح',
    'forbidden': 'الوصول ممنوع',
    'not_found': 'المورد غير موجود',
    'server_error': 'حدث خطأ في الخادم',
  }
}

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguage] = useState<Language>('en')

  useEffect(() => {
    // Get language from localStorage or default to English
    const savedLanguage = localStorage.getItem('language') as Language
    if (savedLanguage) {
      setLanguage(savedLanguage)
      document.documentElement.dir = savedLanguage === 'ar' ? 'rtl' : 'ltr'
      document.documentElement.lang = savedLanguage
    } else {
      // Default to English
      setLanguage('en')
      document.documentElement.dir = 'ltr'
      document.documentElement.lang = 'en'
    }
  }, [])

  const toggleLanguage = () => {
    const newLanguage = language === 'en' ? 'ar' : 'en'
    setLanguage(newLanguage)
    localStorage.setItem('language', newLanguage)
    document.documentElement.dir = newLanguage === 'ar' ? 'rtl' : 'ltr'
    document.documentElement.lang = newLanguage
  }

  const t = (key: string): string => {
    return translations[language][key] || key
  }

  return (
    <LanguageContext.Provider value={{ language, toggleLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  )
}

export function useLanguage() {
  const context = useContext(LanguageContext)
  if (context === undefined) {
    throw new Error('useLanguage must be used within a LanguageProvider')
  }
  return context
} 