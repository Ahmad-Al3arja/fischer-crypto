import { ExternalLink, MessageCircle, Heart } from 'lucide-react'
import { useLanguage } from '@/contexts/LanguageContext'

interface DeveloperSectionProps {
  compact?: boolean
}

export default function DeveloperSection({ compact = false }: DeveloperSectionProps) {
  const { t } = useLanguage()

  const handleWhatsAppClick = () => {
    const phoneNumber = "970599123456" // Replace with actual WhatsApp number
    const message = "Hello! I'm interested in your web development services."
    const whatsappUrl = `https://wa.me/${phoneNumber}?text=${encodeURIComponent(message)}`
    window.open(whatsappUrl, '_blank')
  }

  if (compact) {
    return (
      <div className="w-full text-center py-4">
        <button
          onClick={handleWhatsAppClick}
          className="text-green-400 hover:text-green-300 text-sm font-medium transition-colors duration-200 flex items-center justify-center gap-1 mx-auto"
        >
          <MessageCircle className="h-3 w-3" />
          <span>{t('developer_name')}</span>
          <ExternalLink className="h-3 w-3" />
        </button>
      </div>
    )
  }

  return (
    <div className="w-full bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 border-t border-gray-600 py-8 px-4 mb-16">
      <div className="max-w-lg mx-auto">
        <div className="bg-gradient-to-br from-gray-800 via-gray-700 to-gray-800 border border-gray-600 rounded-2xl shadow-xl p-8">
          {/* Header with text only - Enhanced for better attention */}
          <div className="flex flex-col items-center text-center mb-8">
            {/* Text content - Enhanced styling for better attention */}
            <div className="space-y-3">
              <h3 className="text-white font-bold text-2xl leading-tight tracking-wide drop-shadow-lg">
                {t('developer_name')}
              </h3>
              <p className="text-green-400 text-lg font-semibold tracking-wide">
                {t('developer_title')}
              </p>
              <div className="flex items-center justify-center gap-2">
                <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse shadow-lg"></div>
                <span className="text-gray-300 text-base font-medium tracking-wide">
                  {t('available_for_projects')}
                </span>
              </div>
            </div>
          </div>
          {/* WhatsApp Button */}
          <div className="space-y-6">
            <button
              onClick={handleWhatsAppClick}
              className="w-full bg-gradient-to-r from-green-600 via-green-500 to-green-600 hover:from-green-500 hover:via-green-400 hover:to-green-500 text-white text-lg font-bold py-4 px-6 rounded-xl flex items-center justify-center gap-3 transition-all duration-300 transform hover:scale-105 hover:shadow-2xl tracking-wide shadow-lg"
            >
              <MessageCircle className="h-6 w-6" />
              <span>{t('contact_on_whatsapp')}</span>
              <ExternalLink className="h-6 w-6" />
            </button>
            {/* Footer */}
            <div className="text-center pt-4 border-t border-gray-700">
              <p className="text-gray-400 text-sm flex items-center justify-center gap-2 tracking-wide">
                <span>{t('developed_with')}</span>
                <Heart className="h-4 w-4 text-red-500 animate-pulse" />
                <span>{t('by_developer')}</span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
} 