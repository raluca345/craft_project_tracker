import { useCallback, useEffect, useRef, useState } from "react";
import {
  getProjects,
  createProject,
  editProject,
  updateProjectStatus,
  updateProjectNotes,
  deleteProject,
} from "../api/apiProjects";
import { moveProject } from "../utils/projectOrdering";

export function useProjectsList({
  showError,
  statuses = [],
  setEditingProject,
  setEditingNotes,
  setIsNewProjectOpen,
  setProjectToDelete,
}) {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  const projectsRef = useRef(projects);
  useEffect(() => {
    projectsRef.current = projects;
  }, [projects]);

  const laneIdsRef = useRef(statuses);
  useEffect(() => {
    laneIdsRef.current = statuses;
  }, [statuses]);

  // Lane the dragged card started in, used to undo cancels and failed saves.
  const originLaneRef = useRef(null);

  useEffect(() => {
    getProjects()
      .then((data) => {
        setProjects(data);
        setLoading(false);
      })
      .catch((err) => {
        showError(err);
        setLoading(false);
      });
  }, [showError]);

  const handleSaveProject = useCallback(
    async (project) => {
      try {
        if (project.id) {
          const updated = await editProject(project.id, project);
          setProjects((prev) =>
            prev.map((p) => (p.id === updated.id ? updated : p)),
          );
          setEditingProject(null);
        } else {
          const created = await createProject(project);
          setProjects((prev) => [...prev, created]);
          setIsNewProjectOpen(false);
        }
      } catch (err) {
        showError(err);
        throw err;
      }
    },
    [showError, setEditingProject, setIsNewProjectOpen],
  );

  const handleSaveNotes = useCallback(
    async (projectId, notes) => {
      try {
        const updated = await updateProjectNotes(projectId, notes);
        setProjects((prev) =>
          prev.map((project) =>
            project.id === updated.id ? updated : project,
          ),
        );
        setEditingNotes(null);
      } catch (err) {
        showError(err);
        throw err;
      }
    },
    [showError, setEditingNotes],
  );

  const handleConfirmDelete = useCallback(
    async (project) => {
      try {
        await deleteProject(project.id);
        setProjects((prev) => prev.filter((p) => p.id !== project.id));
        setProjectToDelete(null);
      } catch (err) {
        showError(err);
        throw err;
      }
    },
    [showError, setProjectToDelete],
  );

  // Mirror a would-be card position into React state so React stays the
  // single owner of the DOM and renders the drag preview. No-ops when the
  // position wouldn't change anything (keeps repeated hover events stable).
  const mirrorMove = useCallback((projectId, status, insertion) => {
    if (!laneIdsRef.current.includes(status)) return;
    if (insertion != null && typeof insertion !== "number") return;
    setProjects((prev) => {
      const lane = prev.filter((p) => p.status === status);
      const from = lane.findIndex((p) => p.id === projectId);
      const to = insertion ?? lane.length;
      if (from !== -1 && (to === from || to === from + 1)) {
        return prev;
      }
      return moveProject(prev, projectId, status, insertion);
    });
  }, []);

  const handleDragStart = useCallback((event) => {
    const projectId = event.operation.source?.id;
    originLaneRef.current =
      projectsRef.current.find((p) => p.id === projectId)?.status ?? null;
  }, []);

  // While hovering other cards/lanes, mirror the move into React state.
  const handleDragOver = useCallback(
    (event) => {
      const { source, target } = event.operation;
      const projectId = source?.id;
      if (projectId == null || !target) return;

      // Hovering a lane's empty area: park the card at the end of that lane.
      if (laneIdsRef.current.includes(target.id)) {
        mirrorMove(projectId, target.id, null);
        return;
      }

      // Hovering another card: land just before it.
      mirrorMove(projectId, target.sortable?.group, target.sortable?.index);
    },
    [mirrorMove],
  );

  const handleDragEnd = useCallback(
    (event) => {
      const { source, target } = event.operation;
      const projectId = source?.id;
      const originLane = originLaneRef.current;
      originLaneRef.current = null;
      if (projectId == null) return;

      // Cancelled drag (e.g. Escape): undo the live preview by parking the
      // card back at the end of its origin lane.
      if (event.canceled) {
        if (originLane) mirrorMove(projectId, originLane, null);
        return;
      }

      // Which lane did the drop land in?
      let finalLane = null;
      let insertion = null;
      if (laneIdsRef.current.includes(target?.id)) {
        finalLane = target.id;
      } else if (typeof target?.sortable?.index === "number") {
        finalLane = target.sortable.group;
        insertion = target.sortable.index;
      }
      // Very fast flicks can drop without any dragover; fall back to wherever
      // the live preview last left the card and only worry about saving.
      if (!finalLane || !laneIdsRef.current.includes(finalLane)) {
        finalLane = projectsRef.current.find((p) => p.id === projectId)?.status;
        insertion = null;
      }
      if (!finalLane) return;

      mirrorMove(projectId, finalLane, insertion);

      // Order within a lane isn't persisted, only the lane itself.
      if (finalLane !== originLane) {
        updateProjectStatus(projectId, finalLane).catch((err) => {
          if (originLane) mirrorMove(projectId, originLane, null);
          showError(err);
        });
      }
    },
    [mirrorMove, showError],
  );

  return {
    projects,
    loading,
    handleSaveProject,
    handleSaveNotes,
    handleConfirmDelete,
    handleDragStart,
    handleDragOver,
    handleDragEnd,
  };
}
