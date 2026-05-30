"use client";

export default function GlobalError({
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html>
      <body>
        <div style={{ textAlign: "center", padding: "4rem" }}>
          <h2>Une erreur est survenue</h2>
          <button onClick={reset}>Reessayer</button>
        </div>
      </body>
    </html>
  );
}
