import { useState } from "react";
import { SortableKeyboardPlugin } from "@dnd-kit/dom/sortable";
import { useSortable } from "@dnd-kit/react/sortable";
import { FaNoteSticky, FaPenToSquare, FaTrashCan } from "react-icons/fa6";
import { API_ROOT } from "../../api/apiCore";
import ProjectTypeIcon from "./ProjectTypeIcon.jsx";
import TapeCorner from "./TapeCorner.jsx";
import {
  CARD_BORDER_COLORS,
  CARD_PLACEHOLDER_CLASSES,
  cardRotation,
} from "./projectCardTheme.js";

const MAX_VISIBLE_TAGS = 2;

// dnd-kit's default OptimisticSortingPlugin physically re-parents our DOM
// nodes while dragging, which desyncs React and crashes reconciliation.
const SORTABLE_PLUGINS = [SortableKeyboardPlugin];

export default function ProjectCard({
  project,
  index,
  onEdit,
  onEditNotes,
  onDelete,
  onTagClick,
}) {
  const [showAllTags, setShowAllTags] = useState(false);

  const tags = project.tags ?? [];
  const visibleTags = showAllTags ? tags : tags.slice(0, MAX_VISIBLE_TAGS);
  const extraTagCount = tags.length - MAX_VISIBLE_TAGS;

  // Sortable when rendered in a lane (index given); inert in the search grid.
  const { ref } = useSortable({
    id: project.id,
    index,
    group: project.status,
    type: "project",
    plugins: SORTABLE_PLUGINS,
    disabled: !Number.isInteger(index),
  });

  return (
    <div
      ref={ref}
      data-card
      className="relative shrink-0 card-container mt-8"
    >
      <div
        className="relative flex h-70 w-50 flex-col my-1 mx-5"
        style={{ transform: `rotate(${cardRotation(project.id)})` }}
      >
        <TapeCorner position="top-left" rotation={-30} />
        <TapeCorner position="top-right" rotation={40} />
        <div
          className={`card group flex flex-1 flex-col overflow-hidden border-5 border-dashed ${CARD_BORDER_COLORS[project.status] ?? "border-slate-300/80"} bg-white shadow-[0_1px_3px_0_rgba(0,0,0,0.06)]`}
        >
          <div
            className={`relative min-h-0 flex-1 ${CARD_PLACEHOLDER_CLASSES[project.status]}`}
          >
            <div className="action-buttons absolute top-4 right-3 z-20 flex flex-row gap-1.5 text-slate-600 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
              <button
                type="button"
                onClick={() => onEditNotes(project)}
                className="flex items-center justify-center w-7 h-7 rounded-full bg-white/70 backdrop-blur-xs transition-colors hover:bg-white hover:text-amber-600"
                aria-label="Open notes"
              >
                <FaNoteSticky className="text-xs" />
              </button>
              <button
                type="button"
                onClick={() => onEdit(project)}
                className="flex items-center justify-center w-7 h-7 rounded-full bg-white/70 backdrop-blur-xs transition-colors hover:bg-white hover:text-amber-600"
                aria-label="Edit project"
              >
                <FaPenToSquare className="text-xs" />
              </button>
              <button
                type="button"
                onClick={() => onDelete(project)}
                className="flex items-center justify-center w-7 h-7 rounded-full bg-white/70 backdrop-blur-xs transition-colors hover:bg-white hover:text-red-600"
                aria-label="Delete project"
              >
                <FaTrashCan className="text-xs" />
              </button>
            </div>
            <div className="h-full w-full flex items-center justify-center text-s text-slate-600">
              {project.imageKey ? (
                <img
                  src={`${API_ROOT}/images/${project.imageKey}`}
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
                <div
                  className={`grid items-start gap-2 ${
                    showAllTags
                      ? "grid-cols-1"
                      : "grid-cols-[minmax(0,1fr)_auto]"
                  }`}
                >
                  <div
                    className={`flex gap-2 ${
                      showAllTags ? "flex-wrap" : "flex-nowrap overflow-hidden"
                    }`}
                  >
                    {visibleTags.map((tag) => (
                      <button
                        key={tag}
                        type="button"
                        title={tag}
                        aria-label={`Search projects tagged ${tag}`}
                        onClick={() => onTagClick(tag)}
                        className={`inline-block cursor-pointer rounded-md bg-amber-100 px-2 py-0.5 text-sm font-bold text-amber-700 shadow-sm transition-colors hover:bg-amber-200 ${
                          !showAllTags
                            ? "min-w-0 shrink overflow-hidden text-ellipsis whitespace-nowrap"
                            : ""
                        }`}
                      >
                        #{tag}
                      </button>
                    ))}
                  </div>
                  {!showAllTags && extraTagCount > 0 && (
                    <button
                      type="button"
                      onClick={() => setShowAllTags(true)}
                      className="rounded-md border border-amber-200 bg-slate-50 px-2 py-0.5 text-sm font-bold text-slate-500 shadow-sm hover:bg-amber-50"
                    >
                      +{extraTagCount}
                    </button>
                  )}
                </div>
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
