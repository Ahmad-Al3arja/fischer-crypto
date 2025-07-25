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