/** @type {import('next').NextConfig} */
const nextConfig = {
  images: {
    remotePatterns: [
      { protocol: 'https', hostname: '**' },
    ],
  },
  // Nécessaire pour Cloudflare Pages
  output: 'standalone',
};

module.exports = nextConfig;
