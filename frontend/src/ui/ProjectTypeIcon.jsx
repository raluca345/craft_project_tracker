// ProjectTypeIcon.jsx
import { PROJECT_TYPE_ICONS } from "../projectTypeIcons";

export default function ProjectTypeIcon({ type, className = "w-4 h-4" }) {
  const src = PROJECT_TYPE_ICONS[type] ?? PROJECT_TYPE_ICONS.default;

  return (
    <img
      src={src}
      alt=""
      aria-hidden="true"
      className={className}
      draggable="false"
    />
  );
}
