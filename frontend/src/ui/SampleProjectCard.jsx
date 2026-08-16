import ProjectTypeIcon from "./ProjectTypeIcon.jsx";
import TapeCorner from "./TapeCorner.jsx";
import {
  CARD_BORDER_COLORS,
  CARD_PLACEHOLDER_CLASSES,
  cardRotation,
} from "./projectCardTheme.js";

// Static, non-interactive version of ProjectCard used on the landing page to
// *show* what the board looks like instead of describing it. No dnd, no action
// buttons — just the tape-and-card aesthetic.
export default function SampleProjectCard({ project }) {
  return (
    <div className="card-container mt-8">
      <div
        className="relative flex h-70 w-50 flex-col my-1 mx-5"
        style={{ transform: `rotate(${cardRotation(project.id)})` }}
      >
        <TapeCorner position="top-left" rotation={-30} />
        <TapeCorner position="top-right" rotation={40} />
        <div
          className={`card flex flex-1 flex-col overflow-hidden border-5 border-dashed ${CARD_BORDER_COLORS[project.status] ?? "border-slate-300/80"} bg-white shadow-[0_1px_3px_0_rgba(0,0,0,0.06)]`}
        >
          <div
            className={`relative min-h-0 flex-1 ${CARD_PLACEHOLDER_CLASSES[project.status]}`}
          >
            <div className="h-full w-full flex items-center justify-center text-xs text-slate-600/80" />
          </div>

          <div className="shrink-0 border-t border-slate-200 bg-slate-50 px-3 py-2">
            <div className="flex flex-col items-start gap-1">
              <div className="flex items-center gap-1.5">
                <ProjectTypeIcon
                  type={project.craft}
                  className="w-5 h-5 font-bold"
                />
                <p className="font-semibold text-slate-800">
                  {project.patternName}
                </p>
              </div>
              <div className="mt-2 flex flex-wrap gap-2">
                {(project.tags ?? []).slice(0, 2).map((tag) => (
                  <span
                    key={tag}
                    className="inline-block rounded-md bg-amber-100 px-2 py-0.5 text-sm font-bold text-amber-700 shadow-sm"
                  >
                    #{tag}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}