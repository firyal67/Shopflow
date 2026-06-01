/** @type {import('next').NextConfig} */
const nextConfig = {
  // output: 'export' supprimé — incompatible avec les routes dynamiques
  images: {
    remotePatterns: [
      { protocol: 'https', hostname: '**' },
    ],
  },
};

module.exports = nextConfig;
