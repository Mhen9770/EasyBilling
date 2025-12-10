'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/lib/hooks/useAuth';
import { useRouter } from 'next/navigation';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { PageLoader } from '@/components/ui/Loader';

interface AppLayoutProps {
  children: React.ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
      if (window.innerWidth < 768) {
        setSidebarCollapsed(true);
      }
    };
    
    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading) {
    return <PageLoader text="Loading application..." />;
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen w-full bg-gray-50 flex">
      <Sidebar
        collapsed={sidebarCollapsed || isMobile}
        onToggle={() => setSidebarCollapsed(!sidebarCollapsed)}
        isMobile={isMobile}
      />
      <div
        className={`flex-1 w-full transition-all duration-300 ${
          !isMobile && (sidebarCollapsed ? 'ml-20' : 'ml-64')
        }`}
      >
        <Header 
          onMenuClick={() => setSidebarCollapsed(!sidebarCollapsed)}
          isMobile={isMobile}
        />
        <main className="w-full p-4 sm:p-6 lg:p-8">
          <div className="w-full max-w-full mx-auto">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
