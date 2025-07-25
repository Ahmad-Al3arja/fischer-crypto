import Image from 'next/image'
import { ExternalLink, MessageCircle, Heart } from 'lucide-react'
import { useLanguage } from '@/contexts/LanguageContext'

export default function DeveloperSection() {
  const { t } = useLanguage()

  const handleWhatsAppClick = () => {
    window.open('https://wa.me/972594262092', '_blank')
  }

  return (
    <div className="w-full bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 border-t border-gray-600 py-6 px-4 mb-16">
      <div className="max-w-md mx-auto">
        <div className="bg-gradient-to-br from-gray-800 via-gray-700 to-gray-800 border border-gray-600 rounded-2xl shadow-lg p-6">
          {/* Header with image and text */}
          <div className="flex items-center gap-6 mb-6">
            {/* Developer image */}
            <div className="relative w-20 h-20 rounded-full overflow-hidden border-2 border-green-500 shadow-md flex-shrink-0">
              <Image
                src="/assets/ahmad.jpg"
                alt="Ahmed Alarjah"
                fill
                className="object-cover object-center"
                sizes="80px"
                priority
                quality={95}
                onError={(e) => {
                  console.error("Failed to load developer image");
                  e.currentTarget.style.display = 'none';
                }}
              />
              {/* Green glow effect */}
              <div className="absolute inset-0 rounded-full bg-green-500/20 animate-pulse"></div>
            </div>
            {/* Text content */}
            <div className="flex-1 min-w-0 flex flex-col gap-2">
              <h3 className="text-white font-bold text-lg leading-tight tracking-wide">{t('developer_name')}</h3>
              <p className="text-green-400 text-base font-medium tracking-wide">{t('developer_title')}</p>
              <div className="flex items-center gap-2 mt-1">
                <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                <span className="text-gray-400 text-sm tracking-wide">{t('available_for_projects')}</span>
              </div>
            </div>
          </div>
          {/* WhatsApp Button */}
          <div className="space-y-4">
            <button
              onClick={handleWhatsAppClick}
              className="w-full bg-gradient-to-r from-green-600 via-green-500 to-green-600 hover:from-green-500 hover:via-green-400 hover:to-green-500 text-white text-base font-semibold py-2.5 px-4 rounded-lg flex items-center justify-center gap-2 transition-all duration-300 transform hover:scale-105 hover:shadow-md tracking-wide"
            >
              <MessageCircle className="h-5 w-5" />
              <span>{t('contact_on_whatsapp')}</span>
              <ExternalLink className="h-5 w-5" />
            </button>
            {/* Footer */}
            <div className="text-center pt-3 border-t border-gray-700 mt-2">
              <p className="text-gray-400 text-sm flex items-center justify-center gap-2 tracking-wide">
                <span>{t('developed_with')}</span>
                <Heart className="h-4 w-4 text-red-500 fill-current animate-pulse" />
                <span>{t('by_developer')}</span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
} 