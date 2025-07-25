import Image from 'next/image'
import { ExternalLink, MessageCircle, Heart } from 'lucide-react'
import { useLanguage } from '@/contexts/LanguageContext'

export default function DeveloperSection() {
  const { t } = useLanguage()

  const handleWhatsAppClick = () => {
    window.open('https://wa.me/972594262092', '_blank')
  }

  return (
    <div className="w-full bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 border-t border-gray-600 py-8 px-4 mb-16">
      <div className="max-w-lg mx-auto">
        <div className="bg-gradient-to-br from-gray-800 via-gray-700 to-gray-800 border border-gray-600 rounded-2xl shadow-xl p-8">
          {/* Header with image and text - Enhanced for better attention */}
          <div className="flex flex-col items-center text-center mb-8">
            {/* Developer image - Much larger and more prominent */}
            <div className="relative w-32 h-32 rounded-full overflow-hidden border-4 border-green-500 shadow-2xl flex-shrink-0 mb-6 transform hover:scale-105 transition-transform duration-300">
              <Image
                src="/assets/ahmad.jpg"
                alt="Ahmed Alarjah"
                fill
                className="object-cover"
                style={{ objectPosition: 'center 30%' }}
                sizes="(max-width: 768px) 128px, 128px"
                priority
                quality={100}
                onError={(e) => {
                  console.error("Failed to load developer image");
                  e.currentTarget.style.display = 'none';
                }}
              />
              {/* Enhanced green glow effect */}
              <div className="absolute inset-0 rounded-full bg-green-500/30 animate-pulse"></div>
              {/* Additional outer glow */}
              <div className="absolute -inset-2 rounded-full bg-green-500/20 blur-xl animate-pulse"></div>
            </div>
            
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