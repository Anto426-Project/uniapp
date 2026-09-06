/** @type {import('next').NextConfig} */
const basePath = process.env.NEXT_PUBLIC_BASE_PATH || '';

const nextConfig = {
  output: 'export',
  basePath: basePath || undefined,
  trailingSlash: true,
  transpilePackages: ['ogl'],
  images: {
    unoptimized: true,
  },
};

export default nextConfig;
