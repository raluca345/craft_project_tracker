import {
  useCallback,
  useLayoutEffect,
  useRef,
  useState,
} from "react";
import { useDroppable } from "@dnd-kit/react";
import { CollisionPriority } from "@dnd-kit/abstract";
import { HiChevronLeft, HiChevronRight, HiPlus } from "react-icons/hi2";

const HEADING_COLORS = {
  TO_DO: "text-rose-700",
  IN_PROGRESS: "text-orange-600",
  ASSEMBLING: "text-teal-600",
  FINISHED: "text-sky-700",
};

export default function ProjectLane({ id, title, children, onAddNew }) {
  const { ref } = useDroppable({
    id,
    accept: ["project"],
    collisionPriority: CollisionPriority.Low,
  });
  const scrollRef = useRef(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  const updateScrollButtons = useCallback(() => {
    const el = scrollRef.current;
    if (!el) return;
    setCanScrollLeft(el.scrollLeft > 1);
    setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 1);
  }, []);

  useLayoutEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    updateScrollButtons();
    const resizeObserver = new ResizeObserver(updateScrollButtons);
    resizeObserver.observe(el);
    const mutationObserver = new MutationObserver(updateScrollButtons);
    mutationObserver.observe(el, { childList: true });
    return () => {
      resizeObserver.disconnect();
      mutationObserver.disconnect();
    };
  }, [updateScrollButtons]);

  const scrollByCard = useCallback((direction) => {
    const el = scrollRef.current;
    if (!el) return;
    const firstCard = el.querySelector("[data-card]");
    const cardWidth = firstCard?.offsetWidth ?? 200;
    el.scrollBy({
      left: direction * (cardWidth + 8),
      behavior: "smooth",
    });
  }, []);

  return (
    <section ref={ref} className="w-full">
      <div className="m-4 flex items-center gap-4">
        <h2
          className={`font-semibold ${HEADING_COLORS[id] ?? "text-slate-700"}`}
        >
          {title}
        </h2>
        <div className="flex-1 border-t border-slate-200" />
      </div>
      <div className="relative">
        <div
          ref={scrollRef}
          onScroll={updateScrollButtons}
          className="status-carousel flex flex-nowrap gap-2 overflow-x-auto overflow-y-hidden px-4 py-2"
        >
          {children}
          <div className="shrink-0">
            <button
              type="button"
              onClick={onAddNew}
              className="flex h-70 w-50 mt-8 mb-2 mx-6 cursor-pointer flex-col items-center justify-center gap-2 rounded-2xl border-5 border-dashed border-slate-300 bg-slate-50 text-sm text-slate-400 [font:inherit]"
            >
              <HiPlus className="w-6 h-6 text-slate-300" />
              Add new
            </button>
          </div>
        </div>
        {canScrollLeft && (
          <button
            type="button"
            aria-label="Scroll left"
            onClick={() => scrollByCard(-1)}
            className="absolute left-0 top-1/2 -translate-y-1/2 flex h-8 w-8 items-center justify-center rounded-full bg-white/80 text-slate-600 shadow-md backdrop-blur-sm transition-colors hover:bg-white hover:text-slate-900"
          >
            <HiChevronLeft className="h-5 w-5" />
          </button>
        )}
        {canScrollRight && (
          <button
            type="button"
            aria-label="Scroll right"
            onClick={() => scrollByCard(1)}
            className="absolute right-0 top-1/2 -translate-y-1/2 flex h-8 w-8 items-center justify-center rounded-full bg-white/80 text-slate-600 shadow-md backdrop-blur-sm transition-colors hover:bg-white hover:text-slate-900"
          >
            <HiChevronRight className="h-5 w-5" />
          </button>
        )}
      </div>
    </section>
  );
}
