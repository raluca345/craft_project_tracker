// Move a project into `status` at position `toIndex` within that lane.
// `toIndex` counts positions without the dragged card and is clamped to the
// lane bounds; null/undefined appends to the end of the lane. Works for
// reordering within a lane and moving across lanes alike.
export function moveProject(prevProjects, projectId, status, toIndex) {
  const moved = prevProjects.find((p) => p.id === projectId);
  if (!moved) return prevProjects;

  const others = prevProjects.filter((p) => p.id !== projectId);
  const lane = others.filter((p) => p.status === status);
  const index = Math.min(Math.max(toIndex ?? lane.length, 0), lane.length);
  const insertBeforeId = lane[index]?.id;
  const movedWithStatus = { ...moved, status };

  if (insertBeforeId == null) {
    return [...others, movedWithStatus];
  }

  return others.flatMap((project) =>
    project.id === insertBeforeId ? [movedWithStatus, project] : project,
  );
}
