import { Children, useCallback, useState } from "react";
import { useDroppable } from "@dnd-kit/react";
import { Navigation } from "swiper/modules";
import { Swiper, SwiperSlide } from "swiper/react";
import "swiper/css";
import "swiper/css/navigation";

export default function ProjectLane({ id, title, children }) {
  const { ref } = useDroppable({
    id,
  });
  const [canNavigate, setCanNavigate] = useState(false);
  const cards = Children.toArray(children);
  const updateCanNavigate = useCallback((swiper) => {
    if (!swiper || swiper.destroyed) return;

    setCanNavigate(swiper.virtualSize > swiper.width + 1);
  }, []);

  return (
    <section
      ref={ref}
      className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-sm"
    >
      <div className="mb-3 flex items-center justify-between gap-4">
        <h2 className="font-semibold text-slate-700">{title}</h2>
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
        spaceBetween={16}
        className={`status-carousel min-h-78 ${
          canNavigate ? "" : "status-carousel--locked"
        }`}
      >
        {cards.length > 0 ? (
          cards.map((card, index) => (
            <SwiperSlide key={card.key ?? index} className="w-auto!">
              {card}
            </SwiperSlide>
          ))
        ) : (
          <SwiperSlide className="w-auto!">
            <div className="flex h-70 w-50 items-center justify-center rounded-2xl border border-dashed border-slate-300 bg-slate-50 text-sm text-slate-400">
              Drop here
            </div>
          </SwiperSlide>
        )}
      </Swiper>
    </section>
  );
}
