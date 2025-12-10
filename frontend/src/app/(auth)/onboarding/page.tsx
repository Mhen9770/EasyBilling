'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

interface TenantFormData {
  companyName: string;
  businessType: string;
  companyEmail: string;
  adminUsername: string;
  adminPassword: string;
  subscriptionPlan: 'starter' | 'professional' | 'enterprise';
}

export default function TenantOnboardingPage() {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const [formData, setFormData] = useState<TenantFormData>({
    companyName: '',
    businessType: 'retail',
    companyEmail: '',
    adminUsername: '',
    adminPassword: '',
    subscriptionPlan: 'professional',
  });

  const handleSubmit = async () => {
    setIsSubmitting(true);
    try {
      // TODO: Call API
      console.log('Submitting:', formData);
      await new Promise(resolve => setTimeout(resolve, 2000));
      router.push('/login');
    } catch (error) {
      console.error('Failed:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 flex items-center justify-center p-4">
      <div className="max-w-2xl w-full bg-white rounded-2xl shadow-xl p-8">
        <h1 className="text-3xl font-bold text-gray-900 text-center mb-8">
          Tenant Onboarding (Simple Version)
        </h1>
        <p className="text-center text-gray-600 mb-8">
          This is a simplified onboarding page. Full multi-step form coming soon!
        </p>
        
        <div className="space-y-4">
          <input
            type="text"
            placeholder="Company Name"
            className="w-full px-4 py-3 border rounded-lg"
            value={formData.companyName}
            onChange={(e) => setFormData({...formData, companyName: e.target.value})}
          />
          
          <input
            type="email"
            placeholder="Company Email"
            className="w-full px-4 py-3 border rounded-lg"
            value={formData.companyEmail}
            onChange={(e) => setFormData({...formData, companyEmail: e.target.value})}
          />
          
          <input
            type="text"
            placeholder="Admin Username"
            className="w-full px-4 py-3 border rounded-lg"
            value={formData.adminUsername}
            onChange={(e) => setFormData({...formData, adminUsername: e.target.value})}
          />
          
          <input
            type="password"
            placeholder="Password"
            className="w-full px-4 py-3 border rounded-lg"
            value={formData.adminPassword}
            onChange={(e) => setFormData({...formData, adminPassword: e.target.value})}
          />
          
          <button
            onClick={handleSubmit}
            disabled={isSubmitting}
            className="w-full py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
          >
            {isSubmitting ? 'Creating...' : 'Create Tenant'}
          </button>
        </div>
        
        <div className="mt-6 text-center">
          <Link href="/login" className="text-blue-600 hover:text-blue-700">
            Already have an account? Sign in
          </Link>
        </div>
      </div>
    </div>
  );
}
