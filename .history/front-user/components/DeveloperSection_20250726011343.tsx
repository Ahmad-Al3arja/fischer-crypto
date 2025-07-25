import Image from 'next/image'
import { ExternalLink, MessageCircle, Heart } from 'lucide-react'

export default function DeveloperSection() {
  const handleWhatsAppClick = () => {
    window.open('https://wa.me/972594262092', '_blank')
  }

  return (
    <div className="fixed bottom-6 right-6 z-50">
      <div className="bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 border border-gray-600 rounded-2xl shadow-2xl p-6 max-w-md backdrop-blur-sm">
        {/* Header with image and stacked text */}
        <div className="flex items-start space-x-5 mb-5">
          {/* Image on the right */}
          <div className="flex-1 min-w-0">
            <h3 className="text-white font-bold text-xl leading-tight tracking-wide mb-2">Ahmed Alarjah</h3>
            <p className="text-green-400 text-base font-medium tracking-wide mb-3">Full Stack Developer</p>
            <div className="flex items-center">
              <div className="w-2.5 h-2.5 bg-green-500 rounded-full mr-3 animate-pulse"></div>
              <span className="text-gray-400 text-sm tracking-wide">Available for projects</span>
            </div>
          </div>
          
          {/* Image on the left */}
          <div className="relative w-28 h-28 rounded-full overflow-hidden border-3 border-green-500 shadow-lg flex-shrink-0">
            <Image
              src="/assets/ahmad.jpg"
              alt="Ahmed Alarjah"
              fill
              className="object-cover object-center"
              sizes="112px"
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
        </div>
        
        {/* WhatsApp Button */}
        <div className="space-y-5">
          <button
            onClick={handleWhatsAppClick}
            className="w-full bg-gradient-to-r from-green-600 via-green-500 to-green-600 hover:from-green-500 hover:via-green-400 hover:to-green-500 text-white text-base font-semibold py-4 px-6 rounded-xl flex items-center justify-center space-x-3 transition-all duration-300 transform hover:scale-105 hover:shadow-lg shadow-md tracking-wide"
          >
            <MessageCircle className="h-5 w-5" />
            <span>Contact on WhatsApp</span>
            <ExternalLink className="h-5 w-5" />
          </button>
          
          {/* Footer with heart icon */}
          <div className="text-center pt-4 border-t border-gray-700">
            <p className="text-gray-400 text-sm flex items-center justify-center space-x-3 tracking-wide">
              <span>Developed with</span>
              <Heart className="h-4 w-4 text-red-500 fill-current animate-pulse" />
              <span>by Ahmed Alarjah</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
} 