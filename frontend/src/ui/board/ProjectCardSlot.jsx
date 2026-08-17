import { useDroppable } from "@dnd-kit/react";

export default function ProjectCardSlot({ status, index, children }) {
  const { ref } = useDroppable({
    id: `project-slot:${status}:${index}`,
  });

  return (
    <div ref={ref} className="h-full">
      {children}
    </div>
  );
}
