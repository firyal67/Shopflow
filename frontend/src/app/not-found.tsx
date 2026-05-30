import Link from "next/link";

export default function NotFound() {
  return (
    <div className="text-center py-24">
      <h1 className="text-6xl font-bold text-gray-200 mb-4">404</h1>
      <p className="text-xl text-gray-600 mb-6">Page introuvable</p>
      <Link href="/" className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
        Retour accueil
      </Link>
    </div>
  );
}
