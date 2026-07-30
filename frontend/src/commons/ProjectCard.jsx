import { React, useState } from "react";
import { useDraggable } from "@dnd-kit/react";
import { FaNoteSticky, FaPenToSquare, FaTrashCan } from "react-icons/fa6";
import ProjectTypeIcon from "./ProjectTypeIcon.jsx";

const MAX_VISIBLE_TAGS = 2;

const BORDER_COLORS = {
  TO_DO: "border-rose-300",
  IN_PROGRESS: "border-orange-300",
  ASSEMBLING: "border-teal-300",
  FINISHED: "border-sky-300",
};

const PLACEHOLDER_CLASSES = {
  TO_DO: "placeholder-texture-rose",
  IN_PROGRESS: "placeholder-texture-orange",
  ASSEMBLING: "placeholder-texture-teal",
  FINISHED: "placeholder-texture-sky",
};

function cardRotation(id) {
  const hash = parseInt(id.replace(/-/g, "").slice(0, 8), 16);
  return (hash % 5) - 2 + "deg";
}

export default function ProjectCard({ project }) {
  const { ref } = useDraggable({
    id: project.id,
  });

  const [showAllTags, setShowAllTags] = useState(false);

  const visibleTags = showAllTags
    ? project.tags
    : project.tags.slice(0, MAX_VISIBLE_TAGS);
  const extraTagCount = project.tags.length - MAX_VISIBLE_TAGS;

  return (
    <div ref={ref} className="card-container">
      <div
        className={`card flex h-70 w-50 flex-col overflow-hidden rounded-2xl border-2 border-dashed ${BORDER_COLORS[project.status] ?? "border-slate-300"} bg-white my-1 mx-3 shadow-[0_1px_3px_0_rgba(0,0,0,0.06)]`}
        style={{ transform: `rotate(${cardRotation(project.id)})` }}
      >
        <div
          className={`relative min-h-0 flex-1 ${PLACEHOLDER_CLASSES[project.status]}`}
        >
          <div className="action-buttons absolute top-4 right-3 flex flex-row gap-1.5 text-slate-600">
            <button
              type="button"
              className="flex items-center justify-center w-7 h-7 rounded-full bg-white/70 backdrop-blur-xs transition-colors hover:bg-white hover:text-amber-600"
              aria-label="Open notes"
            >
              <FaNoteSticky className="text-xs" />
            </button>
            <button
              type="button"
              className="flex items-center justify-center w-7 h-7 rounded-full bg-white/70 backdrop-blur-xs transition-colors hover:bg-white hover:text-amber-600"
              aria-label="Edit project"
            >
              <FaPenToSquare className="text-xs" />
            </button>
            <button
              type="button"
              className="flex items-center justify-center w-7 h-7 rounded-full bg-white/70 backdrop-blur-xs transition-colors hover:bg-white hover:text-red-600"
              aria-label="Delete project"
            >
              <FaTrashCan className="text-xs" />
            </button>
          </div>
          <div className="h-full w-full flex items-center justify-center text-s text-slate-600">
            {project.imageUrl ? (
              <img
                src={project.imageUrl}
                alt=""
                className="h-full w-full object-cover"
              />
            ) : null}
          </div>
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
            <div className="mt-2 flex flex-col items-start gap-1">
              <div className="flex flex-wrap gap-2">
                {visibleTags.map((tag) => (
                  <span
                    key={tag}
                    title={tag}
                    className={`inline-block rounded-md bg-amber-100 px-2 py-0.5 text-sm font-bold text-amber-700 shadow-sm ${
                      showAllTags
                        ? ""
                        : "max-w-17.5 overflow-hidden text-ellipsis whitespace-nowrap"
                    }`}
                  >
                    #{tag}
                  </span>
                ))}
                {!showAllTags && extraTagCount > 0 && (
                  <button
                    type="button"
                    onClick={() => setShowAllTags(true)}
                    className="rounded-md border border-amber-200 bg-slate-50 px-2 py-0.5 text-sm font-bold text-slate-500 shadow-sm hover:bg-amber-50"
                  >
                    +{extraTagCount}
                  </button>
                )}

                {showAllTags && project.tags.length > MAX_VISIBLE_TAGS && (
                  <button
                    type="button"
                    onClick={() => setShowAllTags(false)}
                    className="text-sm font-medium text-slate-500 hover:text-slate-700"
                  >
                    Show less
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
