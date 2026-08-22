import { useCallback, useEffect, useRef, useState } from "react";
import {
  getProjects,
  createProject,
  editProject,
  updateProjectStatus,
  updateProjectNotes,
  deleteProject,
} from "../api/apiProjects";
import { reorderProject, moveProjectToStatus } from "../utils/projectOrdering";

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

  // State as it was before the current drag started, for rollback.
  const preDragProjectsRef = useRef(null);

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

  const applyLiveMove = useCallback((prev, projectId, status, toIndex) => {
    const project = prev.find((p) => p.id === projectId);
    if (!project) return prev;
    if (project.status === status) {
      return reorderProject(prev, projectId, status, toIndex);
    }
    return moveProjectToStatus(prev, projectId, status, toIndex);
  }, []);

  const handleDragStart = useCallback(() => {
    preDragProjectsRef.current = projectsRef.current;
  }, []);

  // While hovering other cards/lanes, mirror the move into React state so
  // React stays the single owner of the DOM and renders the preview.
  const handleDragOver = useCallback(
    (event) => {
      const { source, target } = event.operation;
      const projectId = source?.id;
      if (projectId == null || !target) return;

      const laneIds = laneIdsRef.current;

      // Hovering a lane's empty area: move to the end of that lane.
      if (laneIds.includes(target.id)) {
        setProjects((prev) => {
          const project = prev.find((p) => p.id === projectId);
          if (!project || project.status === target.id) return prev;
          return moveProjectToStatus(prev, projectId, target.id, undefined);
        });
        return;
      }

      // Hovering another card: land at that card's slot. Use the pointer's
      // horizontal position relative to the card to insert before/after it,
      // which keeps repeated hover events stable (no flip-flopping).
      const targetStatus = target.sortable?.group;
      const targetIndex = target.sortable?.index;
      if (
        targetStatus == null ||
        !laneIds.includes(targetStatus) ||
        typeof targetIndex !== "number"
      ) {
        return;
      }
      let insertion = targetIndex;
      const rect = target.element?.getBoundingClientRect();
      const pointerX = event.operation.position.current.x;
      if (rect && pointerX > rect.left + rect.width / 2) {
        insertion += 1;
      }

      setProjects((prev) => {
        const lane = prev.filter((p) => p.status === targetStatus);
        const from = lane.findIndex((p) => p.id === projectId);
        if (from !== -1 && (insertion === from || insertion === from + 1)) {
          return prev;
        }
        return applyLiveMove(prev, projectId, targetStatus, insertion);
      });
    },
    [applyLiveMove],
  );

  const handleDragEnd = useCallback(
    (event) => {
      const { source, target } = event.operation;
      const projectId = source?.id;
      const snapshot = preDragProjectsRef.current;
      preDragProjectsRef.current = null;

      if (event.canceled || projectId == null) {
        if (snapshot) setProjects(snapshot);
        return;
      }

      const laneIds = laneIdsRef.current;
      const finalStatus = laneIds.includes(target?.id)
        ? target.id
        : projectsRef.current.find((p) => p.id === projectId)?.status;
      if (!finalStatus || !laneIds.includes(finalStatus)) return;

      // Compare against the pre-drag snapshot: the live dragover mirror has
      // already applied the move by now, so current state alone can't tell
      // us whether this drag changed the lane.
      const originalStatus = snapshot?.find((p) => p.id === projectId)?.status;
      if (snapshot == null || originalStatus === finalStatus) return;

      // Safety net for drops that never triggered a dragover (fast flicks).
      setProjects((prev) => {
        const project = prev.find((p) => p.id === projectId);
        if (!project || project.status === finalStatus) return prev;
        return moveProjectToStatus(prev, projectId, finalStatus, undefined);
      });

      updateProjectStatus(projectId, finalStatus).catch((err) => {
        if (snapshot) setProjects(snapshot);
        showError(err);
      });
    },
    [showError],
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
