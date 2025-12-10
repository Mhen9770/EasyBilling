'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { authApi, type OnboardRequest } from '@/lib/api/auth/authApi';
import { useToastStore } from '@/components/ui/toast';

const businessTypes = [
  { value: 'retail', label: 'Retail Store' },
  { value: 'wholesale', label: 'Wholesale Business' },
  { value: 'restaurant', label: 'Restaurant/Cafe' },
  { value: 'pharmacy', label: 'Pharmacy' },
  { value: 'grocery', label: 'Grocery Store' },
  { value: 'electronics', label: 'Electronics Shop' },
  { value: 'fashion', label: 'Fashion/Apparel' },
  { value: 'services', label: 'Services' },
  { value: 'other', label: 'Other' },
];

export default function TenantOnboardingPage() {
  const router = useRouter();
  const { addToast } = useToastStore();
  const [currentStep, setCurrentStep] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const [formData, setFormData] = useState<OnboardRequest>({
    // Tenant information
    tenantName: '',
    businessName: '',
    businessType: 'retail',
    tenantEmail: '',
    tenantPhone: '',
    // Admin user information
    adminUsername: '',
    adminEmail: '',
    adminPassword: '',
    adminFirstName: '',
    adminLastName: '',
    adminPhone: '',
    // Optional business details
    address: '',
    city: '',
    state: '',
    pincode: '',
    country: 'India',
    gstin: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (currentStep < 3) {
      setCurrentStep(currentStep + 1);
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await authApi.onboard(formData);
      
      // Store auth tokens
      localStorage.setItem('authToken', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      localStorage.setItem('tenantId', response.user.tenantId);
      localStorage.setItem('userId', response.user.id);
      
      addToast('Welcome to EasyBilling! Your account is ready.', 'success');
      
      // Redirect to success page first, then to dashboard
      router.push('/onboarding/success');
    } catch (error: any) {
      console.error('Onboarding failed:', error);
      addToast(
        error?.response?.data?.error?.message || 'Onboarding failed. Please try again.',
        'error'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const renderStep1 = () => (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Business Information</h2>
        <p className="text-gray-600">Tell us about your business</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Business Name <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            name="businessName"
            required
            value={formData.businessName}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="e.g., ABC Retail Store"
          />
        </div>

        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Tenant Name <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            name="tenantName"
            required
            value={formData.tenantName}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="e.g., abc-retail (unique identifier for your account)"
          />
          <p className="text-xs text-gray-500 mt-1">This will be your unique account identifier</p>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Business Type <span className="text-red-500">*</span>
          </label>
          <select
            name="businessType"
            required
            value={formData.businessType}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          >
            {businessTypes.map((type) => (
              <option key={type.value} value={type.value}>
                {type.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Business Email <span className="text-red-500">*</span>
          </label>
          <input
            type="email"
            name="tenantEmail"
            required
            value={formData.tenantEmail}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="contact@business.com"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Business Phone <span className="text-red-500">*</span>
          </label>
          <input
            type="tel"
            name="tenantPhone"
            required
            value={formData.tenantPhone}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="+91 9876543210"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            GSTIN (Optional)
          </label>
          <input
            type="text"
            name="gstin"
            value={formData.gstin}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="GST Identification Number"
          />
        </div>
      </div>
    </div>
  );

  const renderStep2 = () => (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Business Address</h2>
        <p className="text-gray-600">Where is your business located?</p>
      </div>

      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Street Address
          </label>
          <textarea
            name="address"
            value={formData.address}
            onChange={handleChange}
            rows={3}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="Building, Street, Area"
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              City
            </label>
            <input
              type="text"
              name="city"
              value={formData.city}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., Mumbai"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              State
            </label>
            <input
              type="text"
              name="state"
              value={formData.state}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., Maharashtra"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Pincode
            </label>
            <input
              type="text"
              name="pincode"
              value={formData.pincode}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              placeholder="400001"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Country
            </label>
            <input
              type="text"
              name="country"
              value={formData.country}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              placeholder="India"
            />
          </div>
        </div>
      </div>
    </div>
  );

  const renderStep3 = () => (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Admin Account</h2>
        <p className="text-gray-600">Create your administrator account</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            First Name
          </label>
          <input
            type="text"
            name="adminFirstName"
            value={formData.adminFirstName}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="John"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Last Name
          </label>
          <input
            type="text"
            name="adminLastName"
            value={formData.adminLastName}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="Doe"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Username <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            name="adminUsername"
            required
            value={formData.adminUsername}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="admin"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Email <span className="text-red-500">*</span>
          </label>
          <input
            type="email"
            name="adminEmail"
            required
            value={formData.adminEmail}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="admin@business.com"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Phone
          </label>
          <input
            type="tel"
            name="adminPhone"
            value={formData.adminPhone}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="+91 9876543210"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Password <span className="text-red-500">*</span>
          </label>
          <input
            type="password"
            name="adminPassword"
            required
            value={formData.adminPassword}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="••••••••"
          />
          <p className="text-xs text-gray-500 mt-1">Minimum 8 characters</p>
        </div>
      </div>

      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <p className="text-sm text-blue-800">
          <strong>Note:</strong> This account will have administrator privileges with full access to all features.
        </p>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 flex items-center justify-center p-4">
      <div className="max-w-3xl w-full">
        {/* Logo and Header */}
        <div className="text-center mb-8">
          <div className="flex justify-center mb-4">
            <div className="h-16 w-16 bg-gradient-to-br from-blue-600 to-purple-600 rounded-xl" />
          </div>
          <h1 className="text-4xl font-bold text-gray-900">Start Your Free Trial</h1>
          <p className="text-gray-600 mt-2">Set up your EasyBilling account in minutes</p>
        </div>

        {/* Progress Steps */}
        <div className="mb-8">
          <div className="flex justify-between items-center">
            {[1, 2, 3].map((step) => (
              <div key={step} className="flex-1 flex items-center">
                <div className={`flex items-center justify-center w-10 h-10 rounded-full font-semibold ${
                  currentStep >= step
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-200 text-gray-600'
                }`}>
                  {step}
                </div>
                {step < 3 && (
                  <div className={`flex-1 h-1 mx-2 ${
                    currentStep > step ? 'bg-blue-600' : 'bg-gray-200'
                  }`} />
                )}
              </div>
            ))}
          </div>
          <div className="flex justify-between mt-2 text-xs text-gray-600">
            <span>Business Info</span>
            <span>Address</span>
            <span>Admin Account</span>
          </div>
        </div>

        {/* Form */}
        <div className="bg-white shadow-xl rounded-2xl p-8">
          <form onSubmit={handleSubmit}>
            {currentStep === 1 && renderStep1()}
            {currentStep === 2 && renderStep2()}
            {currentStep === 3 && renderStep3()}

            {/* Navigation Buttons */}
            <div className="flex gap-4 mt-8">
              {currentStep > 1 && (
                <button
                  type="button"
                  onClick={() => setCurrentStep(currentStep - 1)}
                  className="flex-1 px-6 py-3 border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50"
                >
                  Back
                </button>
              )}
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex-1 px-6 py-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg font-medium hover:from-blue-700 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isSubmitting ? (
                  <span className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Creating Account...
                  </span>
                ) : currentStep === 3 ? (
                  'Complete Setup'
                ) : (
                  'Continue'
                )}
              </button>
            </div>
          </form>
        </div>

        {/* Login Link */}
        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            Already have an account?{' '}
            <Link href="/login" className="font-medium text-blue-600 hover:text-blue-500">
              Sign in
            </Link>
          </p>
        </div>

        {/* Footer */}
        <p className="text-center text-xs text-gray-500 mt-8">
          By creating an account, you agree to our Terms of Service and Privacy Policy
        </p>
      </div>
    </div>
  );
}
