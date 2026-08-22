export function reorderProject(prevProjects, projectId, status, toIndex) {
  const laneProjects = prevProjects.filter((p) => p.status === status);
  const fromIndex = laneProjects.findIndex((p) => p.id === projectId);

  if (fromIndex === -1 || fromIndex === toIndex) {
    return prevProjects;
  }

  const reordered = [...laneProjects];
  const [moved] = reordered.splice(fromIndex, 1);
  reordered.splice(toIndex, 0, moved);

  let nextLaneIndex = 0;

  return prevProjects.map((project) =>
    project.status === status ? reordered[nextLaneIndex++] : project,
  );
}

export function moveProjectToStatus(prevProjects, projectId, status, toIndex) {
  const movedProject = prevProjects.find((p) => p.id === projectId);

  if (!movedProject) {
    return prevProjects;
  }

  const withoutMoved = prevProjects.filter((p) => p.id !== projectId);
  const nextLaneProjects = withoutMoved
    .filter((p) => p.status === status)
    .map((p) => p.id);
  const insertBeforeProjectId = nextLaneProjects[toIndex];

  const movedWithStatus = { ...movedProject, status };

  if (!insertBeforeProjectId) {
    return [...withoutMoved, movedWithStatus];
  }

  return withoutMoved.flatMap((project) =>
    project.id === insertBeforeProjectId ? [movedWithStatus, project] : project,
  );
}
