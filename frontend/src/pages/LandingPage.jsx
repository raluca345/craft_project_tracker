import { Link } from "react-router-dom";
import SampleProjectCard from "../ui/SampleProjectCard.jsx";
import Footer from "../ui/Footer.jsx";
import { LANDING_PROJECTS } from "../landingProjects.js";

export default function LandingPage() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="flex items-center justify-between px-6 py-4">
        <Link
          to="/"
          className="flex items-center gap-2 text-sm font-semibold text-(--accent-color2)"
        >
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-100 text-base shadow-sm">
            🧶
          </span>
          Craft Project Tracker
        </Link>
        <nav className="flex items-center gap-2">
          <Link
            to="/login"
            className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 transition-colors hover:bg-amber-100 hover:text-slate-800"
          >
            Log in
          </Link>
          <Link
            to="/signup"
            className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-fuchsia-600"
          >
            Sign up
          </Link>
        </nav>
      </header>

      <main className="flex-1 px-6">
        <section className="mx-auto flex max-w-5xl flex-col items-center gap-10 py-12 text-center md:py-16">
          <h1 className="max-w-2xl text-4xl font-bold leading-tight text-slate-800 md:text-5xl">
            A place to keep track of what you&apos;re making
          </h1>
          <p className="max-w-xl text-lg leading-relaxed text-slate-600">
            A scrappy little board of ideas and the projects forever stuck in
            assembling hell.
          </p>

          <div className="flex flex-wrap items-center justify-center gap-8 py-8 perspective-distant">
            {LANDING_PROJECTS.map((project, i) => (
              <div
                key={project.id}
                className={
                  i % 2 === 0
                    ? "-translate-y-3 md:-rotate-6"
                    : "translate-y-4 md:rotate-3"
                }
              >
                <SampleProjectCard project={project} />
              </div>
            ))}
          </div>

          <Link
            to="/signup"
            className="rounded-lg bg-fuchsia-500 px-6 py-3 text-base font-semibold text-white shadow-sm transition-colors hover:bg-fuchsia-600"
          >
            Start your own board
          </Link>
        </section>
      </main>

      <Footer />
    </div>
  );
}
