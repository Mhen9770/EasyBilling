import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "./providers";
import { ToastContainer } from "@/components/ui/toast";

export const metadata: Metadata = {
  title: "EasyBilling - Multi-Tenant Billing Platform",
  description: "Enterprise-grade billing and POS platform for retail businesses",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full">
      <body className="antialiased h-full w-full overflow-x-hidden">
        <Providers>
          <div className="h-full w-full">
            {children}
          </div>
          <ToastContainer />
        </Providers>
      </body>
    </html>
  );
}
