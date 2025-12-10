'use client';

import Link from 'next/link';
import { useEffect } from 'react';
// import confetti from 'canvas-confetti'; // Install: npm install canvas-confetti @types/canvas-confetti

export default function OnboardingSuccessPage() {
  useEffect(() => {
    // Confetti animation - install canvas-confetti package to enable
    // npm install canvas-confetti @types/canvas-confetti
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 flex items-center justify-center px-4">
      <div className="max-w-md w-full text-center">
        <div className="bg-white shadow-xl rounded-2xl p-8">
          {/* Success Icon */}
          <div className="flex justify-center mb-6">
            <div className="h-20 w-20 bg-green-100 rounded-full flex items-center justify-center">
              <svg
                className="h-12 w-12 text-green-600"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
          </div>

          {/* Success Message */}
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            Welcome to EasyBilling!
          </h1>
          <p className="text-gray-600 mb-6">
            Your account has been successfully created. You're all set to start managing your business with EasyBilling.
          </p>

          {/* What's Next */}
          <div className="bg-blue-50 rounded-lg p-6 mb-6 text-left">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              What's next?
            </h3>
            <ul className="space-y-3">
              <li className="flex items-start">
                <svg
                  className="h-6 w-6 text-blue-600 mr-3 flex-shrink-0"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
                <span className="text-sm text-gray-700">
                  Add your products and inventory
                </span>
              </li>
              <li className="flex items-start">
                <svg
                  className="h-6 w-6 text-blue-600 mr-3 flex-shrink-0"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
                <span className="text-sm text-gray-700">
                  Set up your team members and permissions
                </span>
              </li>
              <li className="flex items-start">
                <svg
                  className="h-6 w-6 text-blue-600 mr-3 flex-shrink-0"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
                <span className="text-sm text-gray-700">
                  Create your first invoice
                </span>
              </li>
            </ul>
          </div>

          {/* Actions */}
          <div className="space-y-3">
            <Link
              href="/dashboard"
              className="block w-full py-3 px-4 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg font-medium hover:from-blue-700 hover:to-purple-700 transition-all"
            >
              Go to Dashboard
            </Link>
            <Link
              href="/settings"
              className="block w-full py-3 px-4 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-all"
            >
              Complete Profile Setup
            </Link>
          </div>
        </div>

        {/* Help */}
        <p className="mt-6 text-sm text-gray-600">
          Need help getting started?{' '}
          <a href="#" className="text-blue-600 hover:text-blue-500 font-medium">
            View our guide
          </a>
        </p>
      </div>
    </div>
  );
}
