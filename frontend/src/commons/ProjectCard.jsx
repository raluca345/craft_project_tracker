import React from "react";
import { useDraggable } from "@dnd-kit/react";
import { FaNoteSticky, FaPenToSquare, FaTrashCan } from "react-icons/fa6";
import ProjectTypeIcon from "./ProjectTypeIcon.jsx";

const MAX_VISIBLE_TAGS = 2;

export default function ProjectCard({ project }) {
  const { ref } = useDraggable({
    id: project.id,
  });

  const visibleTags = project.tags.slice(0, MAX_VISIBLE_TAGS);
  const extraTagCount = project.tags.length - MAX_VISIBLE_TAGS;

  return (
    <div ref={ref} className="card-container">
      <div className="card flex h-70 w-50 flex-col overflow-hidden rounded-2xl border border-slate-300 bg-white my-2 mx-6 shadow-[0_1px_3px_0_rgba(0,0,0,0.06)]">
        <div className="relative min-h-0 flex-1 bg-linear-to-br from-fuchsia-100 to-fuchsia-200">
          <div className="action-buttons absolute top-4 right-3 flex flex-row gap-2 text-slate-700">
            <button
              type="button"
              className="cursor-pointer transition-colors hover:text-(--accent-color2)"
              aria-label="Open notes"
            >
              <FaNoteSticky />
            </button>
            <button
              type="button"
              className="cursor-pointer transition-colors hover:text-(--accent-color2)"
              aria-label="Edit project"
            >
              <FaPenToSquare />
            </button>
            <button
              type="button"
              className="cursor-pointer transition-colors hover:text-red-600"
              aria-label="Delete project"
            >
              <FaTrashCan />
            </button>
          </div>
          <div className="h-full w-full flex items-center justify-center text-s text-slate-600">
            {project.imageUrl ? (
              <img
                src={project.imageUrl}
                alt=""
                className="h-full w-full object-cover"
              />
            ) : (
              "Cover image"
            )}
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
            <div className="flex flex-col items-start gap-1">
              <span className="text-[0.65rem] font-bold text-slate-500">
                Craft
              </span>
              <div className="flex flex-wrap gap-1.5">
                <span className="rounded-md bg-fuchsia-300 px-2 py-0.5 text-sm font-bold text-(--accent-color2) shadow-sm">
                  {project.craft}
                </span>
              </div>
            </div>
            <div className="mt-2 flex flex-col items-start gap-1">
              <span className="text-[0.65rem] font-bold text-slate-500">
                Tags
              </span>
              <div className="flex flex-wrap gap-1">
                {visibleTags.map((tag) => (
                  <span
                    key={tag}
                    title={tag}
                    className="inline-block max-w-17.5 overflow-hidden text-ellipsis whitespace-nowrap rounded-md bg-fuchsia-200 px-2 py-0.5 text-sm font-bold text-(--accent-color2) shadow-sm"
                  >
                    #{tag}
                  </span>
                ))}
                {extraTagCount > 0 && (
                  <span className="rounded-md border border-fuchsia-300 bg-slate-50 px-2 py-0.5 text-sm font-bold text-slate-600 shadow-sm">
                    +{extraTagCount}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
