import Image from 'next/image'
import { ExternalLink, MessageCircle, Heart } from 'lucide-react'

export default function DeveloperSection() {
  const handleWhatsAppClick = () => {
    window.open('https://wa.me/972594262092', '_blank')
  }

  return (
    <div className="fixed bottom-6 right-6 z-50">
      <div className="bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 border border-gray-600 rounded-2xl shadow-2xl p-6 max-w-sm backdrop-blur-sm">
        {/* Header with larger image */}
        <div className="flex items-start space-x-4 mb-4">
          <div className="relative w-16 h-16 rounded-full overflow-hidden border-3 border-green-500 shadow-lg flex-shrink-0">
            <Image
              src="/assets/ahmad.jpg"
              alt="Ahmed Alarjah"
              fill
              className="object-cover"
              onError={(e) => {
                console.error("Failed to load developer image");
                e.currentTarget.style.display = 'none';
              }}
            />
            {/* Green glow effect */}
            <div className="absolute inset-0 rounded-full bg-green-500/20 animate-pulse"></div>
          </div>
          <div className="flex-1 min-w-0">
            <h3 className="text-white font-bold text-lg leading-tight">Ahmed Alarjah</h3>
            <p className="text-green-400 text-sm font-medium">Full Stack Developer</p>
            <div className="flex items-center mt-1">
              <div className="w-2 h-2 bg-green-500 rounded-full mr-2 animate-pulse"></div>
              <span className="text-gray-400 text-xs">Available for projects</span>
            </div>
          </div>
        </div>
        
        {/* WhatsApp Button */}
        <div className="space-y-3">
          <button
            onClick={handleWhatsAppClick}
            className="w-full bg-gradient-to-r from-green-600 via-green-500 to-green-600 hover:from-green-500 hover:via-green-400 hover:to-green-500 text-white text-sm font-semibold py-3 px-4 rounded-xl flex items-center justify-center space-x-3 transition-all duration-300 transform hover:scale-105 hover:shadow-lg shadow-md"
          >
            <MessageCircle className="h-4 w-4" />
            <span>Contact on WhatsApp</span>
            <ExternalLink className="h-4 w-4" />
          </button>
          
          {/* Footer with heart icon */}
          <div className="text-center pt-2 border-t border-gray-700">
            <p className="text-gray-400 text-xs flex items-center justify-center space-x-1">
              <span>Developed with</span>
              <Heart className="h-3 w-3 text-red-500 fill-current animate-pulse" />
              <span>by Ahmed Alarjah</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
} 