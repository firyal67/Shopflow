import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const PROTECTED_ROUTES = ["/cart", "/checkout", "/orders", "/profile", "/seller", "/admin"];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Lire le store Zustand depuis le cookie
  const authCookie = request.cookies.get("shopflow-auth");
  let isAuthenticated = false;
  let userRole = "";

  if (authCookie) {
    try {
      const parsed = JSON.parse(decodeURIComponent(authCookie.value));
      isAuthenticated = !!parsed?.state?.accessToken;
      userRole = parsed?.state?.user?.role || "";
    } catch {
      isAuthenticated = false;
    }
  }

  // Rediriger vers login si route protegee et non authentifie
  if (PROTECTED_ROUTES.some(r => pathname.startsWith(r)) && !isAuthenticated) {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  // Guard admin
  if (pathname.startsWith("/admin") && userRole !== "ADMIN") {
    return NextResponse.redirect(new URL("/", request.url));
  }

  // Guard seller
  if (pathname.startsWith("/seller") && userRole !== "SELLER" && userRole !== "ADMIN") {
    return NextResponse.redirect(new URL("/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/((?!api|_next/static|_next/image|favicon.ico|placeholder.png).*)",
  ],
};
