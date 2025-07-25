# FISCHER Frontend

A modern React/Next.js frontend for the FISCHER investment platform.

## Features

- 🔐 User authentication (login/register)
- 📊 Dashboard with real-time data
- 💰 Deposit and withdrawal management
- 👥 Referral system
- 📱 Responsive design
- 🎨 Modern UI with Tailwind CSS

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create environment file:
```bash
# Create .env.local file
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

3. Run the development server:
```bash
npm run dev
```

4. Open [http://localhost:3000](http://localhost:3000) in your browser.

## Pages

- `/login` - User login
- `/register` - User registration
- `/dashboard` - Main dashboard
- `/deposit` - Make deposits
- `/withdraw` - Withdraw funds
- `/withdrawal-history` - View withdrawal history
- `/referrals` - Referral network
- `/profile` - User profile
- `/plans` - Investment plans
- `/wallet` - Wallet settings

## Tech Stack

- **Framework**: Next.js 14
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: Radix UI + shadcn/ui
- **State Management**: React Context
- **HTTP Client**: Fetch API
- **Icons**: Lucide React

## API Integration

The frontend communicates with the backend API through the `apiService` in `/services/api.ts`. Make sure the backend is running on `http://localhost:8080` or update the `NEXT_PUBLIC_API_URL` environment variable.

## Development

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run start` - Start production server
- `npm run lint` - Run ESLint

## Project Structure

```
front-user/
├── app/                 # Next.js app directory
│   ├── login/          # Login page
│   ├── register/       # Register page
│   ├── dashboard/      # Dashboard page
│   ├── deposit/        # Deposit page
│   ├── withdraw/       # Withdraw page
│   ├── withdrawal-history/ # Withdrawal history
│   ├── referrals/      # Referrals page
│   ├── profile/        # Profile page
│   ├── plans/          # Plans page
│   ├── wallet/         # Wallet page
│   └── layout.tsx      # Root layout
├── components/         # Reusable components
│   ├── ui/            # UI components (shadcn/ui)
│   ├── ProtectedRoute.tsx # Route protection
│   └── Navbar.tsx     # Navigation bar
├── contexts/          # React contexts
│   └── AuthContext.tsx # Authentication context
├── hooks/             # Custom hooks
│   └── use-toast.ts   # Toast notifications
├── lib/               # Utility libraries
│   └── utils.ts       # Utility functions
├── services/          # API services
│   └── api.ts         # API client
└── types/             # TypeScript types
    └── index.ts       # Type definitions
``` 