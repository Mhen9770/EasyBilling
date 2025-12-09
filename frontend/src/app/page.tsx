import Link from 'next/link';

export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      {/* Header */}
      <header className="border-b bg-white/80 backdrop-blur-sm">
        <nav className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="h-8 w-8 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg" />
            <span className="text-xl font-bold text-gray-900">EasyBilling</span>
          </div>
          <div className="flex items-center space-x-4">
            <Link href="/login" className="text-gray-600 hover:text-gray-900">
              Login
            </Link>
            <Link
              href="/signup"
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Get Started
            </Link>
          </div>
        </nav>
      </header>

      {/* Hero Section */}
      <main className="container mx-auto px-4 py-20">
        <div className="text-center max-w-4xl mx-auto">
          <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6">
            Complete Billing Solution for Your Business
          </h1>
          <p className="text-xl text-gray-600 mb-8">
            Enterprise-grade, multi-tenant billing and POS platform designed for
            retail stores, showrooms, and businesses of all sizes.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              href="/signup"
              className="px-8 py-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
            >
              Start Free Trial
            </Link>
            <Link
              href="/demo"
              className="px-8 py-4 border-2 border-gray-300 text-gray-700 rounded-lg hover:border-gray-400 transition-colors font-semibold"
            >
              View Demo
            </Link>
          </div>
        </div>

        {/* Features Grid */}
        <div className="mt-20 grid md:grid-cols-3 gap-8">
          <FeatureCard
            title="Multi-Tenant Architecture"
            description="Isolated data and customizable settings for each tenant with schema-per-tenant approach."
            icon="🏢"
          />
          <FeatureCard
            title="POS & Billing"
            description="Fast and intuitive point-of-sale system with barcode scanning, multiple payment modes."
            icon="🛒"
          />
          <FeatureCard
            title="Inventory Management"
            description="Track products, variants, stock levels, and automate reordering across locations."
            icon="📦"
          />
          <FeatureCard
            title="Customer Loyalty"
            description="Build customer relationships with loyalty points, memberships, and personalized offers."
            icon="⭐"
          />
          <FeatureCard
            title="Reports & Analytics"
            description="Comprehensive reports, dashboards, and insights to drive business decisions."
            icon="📊"
          />
          <FeatureCard
            title="Tax Compliance"
            description="GST/VAT support with automated tax calculations and ready-to-file reports."
            icon="📝"
          />
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t mt-20 py-8 bg-white">
        <div className="container mx-auto px-4 text-center text-gray-600">
          <p>&copy; 2024 EasyBilling. Built with ❤️ for modern retail businesses.</p>
        </div>
      </footer>
    </div>
  );
}

interface FeatureCardProps {
  title: string;
  description: string;
  icon: string;
}

function FeatureCard({ title, description, icon }: FeatureCardProps) {
  return (
    <div className="p-6 bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100">
      <div className="text-4xl mb-4">{icon}</div>
      <h3 className="text-xl font-semibold text-gray-900 mb-2">{title}</h3>
      <p className="text-gray-600">{description}</p>
    </div>
  );
}
