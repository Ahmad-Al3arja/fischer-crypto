import Image from 'next/image'
import { ExternalLink, MessageCircle, Heart } from 'lucide-react'
import { useLanguage } from '@/contexts/LanguageContext'

export default function DeveloperSection() {
  const { t } = useLanguage()
  
  const handleWhatsAppClick = () => {
    window.open('https://wa.me/972594262092', '_blank')
  }

  return (
    <div className="w-full bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 border-t border-gray-600 py-3 px-4 mb-16">
      <div className="max-w-sm mx-auto">
        <div className="bg-gradient-to-br from-gray-800 via-gray-700 to-gray-800 border border-gray-600 rounded-xl shadow-lg p-4">
          {/* Compact header */}
          <div className="flex items-center space-x-3 mb-3">
            {/* Smaller image */}
            <div className="relative w-16 h-16 rounded-full overflow-hidden border-2 border-green-500 shadow-md flex-shrink-0">
              <Image
                src="/assets/ahmad.jpg"
                alt="Ahmed Alarjah"
                fill
                className="object-cover object-center"
                sizes="64px"
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
            
            {/* Compact text content */}
            <div className="flex-1 min-w-0">
              <h3 className="text-white font-bold text-base leading-tight tracking-wide mb-1">{t('developer_name')}</h3>
              <p className="text-green-400 text-sm font-medium tracking-wide mb-1">{t('developer_title')}</p>
              <div className="flex items-center">
                <div className="w-2 h-2 bg-green-500 rounded-full mr-2 animate-pulse"></div>
                <span className="text-gray-400 text-xs tracking-wide">{t('available_for_projects')}</span>
              </div>
            </div>
          </div>
          
          {/* Compact WhatsApp Button */}
          <div className="space-y-3">
            <button
              onClick={handleWhatsAppClick}
              className="w-full bg-gradient-to-r from-green-600 via-green-500 to-green-600 hover:from-green-500 hover:via-green-400 hover:to-green-500 text-white text-sm font-semibold py-2 px-4 rounded-lg flex items-center justify-center space-x-2 transition-all duration-300 transform hover:scale-105 hover:shadow-md tracking-wide"
            >
              <MessageCircle className="h-4 w-4" />
              <span>{t('contact_on_whatsapp')}</span>
              <ExternalLink className="h-4 w-4" />
            </button>
            
            {/* Compact footer */}
            <div className="text-center pt-2 border-t border-gray-700">
              <p className="text-gray-400 text-xs flex items-center justify-center space-x-2 tracking-wide">
                <span>{t('developed_with')}</span>
                <Heart className="h-3 w-3 text-red-500 fill-current animate-pulse" />
                <span>{t('by_developer')}</span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
} 