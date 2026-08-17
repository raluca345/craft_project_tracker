import { PROJECT_TYPE_ICON_CREDITS } from "../../projectTypeIcons.js";

export default function Footer() {
  return (
    <footer className="mt-auto flex justify-center px-4 pb-4 pt-2 text-sm text-slate-600">
      <details className="group rounded-xl border border-slate-200 bg-white/80 px-4 py-2 shadow-sm backdrop-blur-sm">
        <summary className="cursor-pointer list-none font-medium text-slate-700">
          Icon credits
        </summary>
        <div className="mt-3 grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
          {PROJECT_TYPE_ICON_CREDITS.map((item) => (
            <div key={item.type} className="min-w-0">
              <p className="font-semibold text-slate-800">{item.creditLabel}</p>
              <p className="text-xs leading-5 text-slate-600">
                by {item.author} from{" "}
                <a
                  href={item.sourceUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="font-medium text-(--accent-color2) hover:underline"
                >
                  Noun Project
                </a>{" "}
                (CC BY 3.0)
              </p>
            </div>
          ))}
        </div>
      </details>
    </footer>
  );
}
