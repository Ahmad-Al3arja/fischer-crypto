import Image from 'next/image'
import { ExternalLink, MessageCircle } from 'lucide-react'

export default function DeveloperSection() {
  const handleWhatsAppClick = () => {
    window.open('https://wa.me/972594262092', '_blank')
  }

  return (
    <div className="fixed bottom-4 right-4 z-50">
      <div className="bg-gradient-to-r from-gray-900 to-gray-800 border border-gray-700 rounded-lg shadow-2xl p-4 max-w-xs">
        <div className="flex items-center space-x-3 mb-3">
          <div className="relative w-12 h-12 rounded-full overflow-hidden border-2 border-green-500">
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
          </div>
          <div className="flex-1">
            <h3 className="text-white font-bold text-sm">Ahmed Alarjah</h3>
            <p className="text-green-400 text-xs">Full Stack Developer</p>
          </div>
        </div>
        
        <div className="space-y-2">
          <button
            onClick={handleWhatsAppClick}
            className="w-full bg-gradient-to-r from-green-600 to-green-700 hover:from-green-500 hover:to-green-600 text-white text-xs font-medium py-2 px-3 rounded-lg flex items-center justify-center space-x-2 transition-all duration-200 transform hover:scale-105"
          >
            <MessageCircle className="h-3 w-3" />
            <span>Contact on WhatsApp</span>
            <ExternalLink className="h-3 w-3" />
          </button>
          
          <div className="text-center">
            <p className="text-gray-400 text-xs">
              Developed with ❤️ by Ahmed Alarjah
            </p>
          </div>
        </div>
      </div>
    </div>
  )
} 