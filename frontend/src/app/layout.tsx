import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Providers } from "./providers";
import Navbar from "@/components/layout/Navbar";

const inter = Inter({
  subsets: ["latin"],
  display: "swap",
  variable: "--font-inter",
});

export const metadata: Metadata = {
  title: "ShopFlow — Boutique en Ligne",
  description: "Plateforme e-commerce ShopFlow",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr" className="dark">
      <body className={`${inter.variable} font-sans bg-[#09090b] min-h-screen text-zinc-100 antialiased selection:bg-amber-500/30`}>
        <Providers>
          <Navbar />
          <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 md:py-8 animate-fade-in">
            {children}
          </main>
        </Providers>
      </body>
    </html>
  );
}
