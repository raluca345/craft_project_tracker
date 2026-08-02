export default function TapeCorner({ position, rotation }) {
  const positionClasses = {
    "top-left": "top-3.5 -left-5",
    "top-right": "top-1.5 -right-7",
  };

  return (
    <div
      className={`absolute z-10 ${positionClasses[position]} w-30 h-8 bg-amber-100/90 shadow-sm`}
      style={{ transform: `rotate(${rotation}deg)` }}
    />
  );
}
