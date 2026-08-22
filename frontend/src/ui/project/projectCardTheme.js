// Shared visual tokens for the "tape and card" project cards, so the real
// ProjectCard and the static sample cards on the landing page stay in sync.

export const CARD_BORDER_COLORS = {
  TO_DO: "border-rose-300/50",
  IN_PROGRESS: "border-orange-300/50",
  ASSEMBLING: "border-teal-300/50",
  FINISHED: "border-sky-300/50",
};

export const CARD_PLACEHOLDER_CLASSES = {
  TO_DO: "placeholder-texture-rose",
  IN_PROGRESS: "placeholder-texture-orange",
  ASSEMBLING: "placeholder-texture-teal",
  FINISHED: "placeholder-texture-sky",
};

// Deterministic, tiny pseudo-random rotation (between -2 and +2 deg) per card id,
// so each card settles at a slightly different scrapbook angle.
export function cardRotation(id) {
  const hash = parseInt((id ?? "").replace(/-/g, "").slice(0, 8), 16);
  if (Number.isNaN(hash)) return "0deg";
  return (hash % 5) - 2 + "deg";
}
