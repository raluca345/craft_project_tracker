import { Children, cloneElement, useCallback, useState } from "react";
import { useDroppable } from "@dnd-kit/react";
import { Navigation } from "swiper/modules";
import { Swiper, SwiperSlide } from "swiper/react";
import "swiper/css";
import "swiper/css/navigation";

const HEADING_COLORS = {
  TO_DO: "text-rose-700",
  IN_PROGRESS: "text-orange-600",
  ASSEMBLING: "text-teal-600",
  FINISHED: "text-sky-700",
};

export default function ProjectLane({ id, title, children, onEdit }) {
  const { ref } = useDroppable({
    id,
  });
  const [canNavigate, setCanNavigate] = useState(false);
  const cards = Children.map(children, (card) =>
    cloneElement(card, { onEdit }),
  );
  const updateCanNavigate = useCallback((swiper) => {
    if (!swiper || swiper.destroyed) return;

    setCanNavigate(swiper.virtualSize > swiper.width + 1);
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
      <Swiper
        modules={[Navigation]}
        navigation
        allowTouchMove={false}
        onSwiper={(swiper) => {
          updateCanNavigate(swiper);
          requestAnimationFrame(() => updateCanNavigate(swiper));
        }}
        onResize={updateCanNavigate}
        onSlidesUpdated={updateCanNavigate}
        slidesPerView="auto"
        spaceBetween={8}
        className={`status-carousel min-h-78 ${
          canNavigate ? "" : "status-carousel--locked"
        }`}
      >
        {cards.map((card, index) => (
          <SwiperSlide key={card.key ?? index} className="w-auto!">
            {card}
          </SwiperSlide>
        ))}
        <SwiperSlide className="w-auto!">
          <button
            type="button"
            command="show-modal"
            commandfor="dialog-ex"
            className="flex h-70 w-50 mt-8 mb-2 mx-6 cursor-pointer flex-col items-center justify-center gap-2 rounded-2xl border-5 border-dashed border-slate-300 bg-slate-50 text-sm text-slate-400 [font:inherit]"
          >
            <svg
              className="w-6 h-6 text-slate-300"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 8v4m0 0v4m0-4h4m-4 0H8"
              />
            </svg>
            Add new
          </button>
        </SwiperSlide>
      </Swiper>
    </section>
  );
}
