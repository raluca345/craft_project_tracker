import { Link } from "react-router-dom";
import AppHeader from "../ui/layout/AppHeader.jsx";

export default function NotFoundPage({ isLoggedIn, user }) {
  return (
    <div className="flex min-h-screen flex-col bg-slate-50">
      <AppHeader isLoggedIn={isLoggedIn} user={user} hideAuth />

      <main className="flex flex-1 flex-col">
        <h1 className="mt-8 text-center text-2xl font-semibold text-slate-800">
          Dropped needles
        </h1>

        <div className="flex flex-1 items-center justify-center px-6 py-10">
          <div className="w-full max-w-sm text-center">
            <p className="text-sm text-slate-600">
              Oops! We couldn&apos;t find what you were looking for. Hang on
              tight while we&apos;re looking through our stash — or you can
              return home if you want.
            </p>
            <div className="mt-6 flex flex-col gap-2">
              <Link
                to="/"
                className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-fuchsia-600"
              >
                Back to the home page
              </Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
