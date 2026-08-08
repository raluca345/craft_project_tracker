import { useCallback, useEffect, useState } from "react";
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
  setEditingProject,
  setEditingNotes,
  setIsNewProjectOpen,
  setProjectToDelete,
}) {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

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

  const handleMoveProject = useCallback(
    async (projectId, status, toIndex) => {
      const project = projects.find((p) => p.id === projectId);
      if (!project) return;

      const previousProjects = projects;

      if (status === project.status) {
        setProjects((prev) => reorderProject(prev, projectId, status, toIndex));
        return;
      }

      setProjects((prev) =>
        moveProjectToStatus(prev, projectId, status, toIndex),
      );

      try {
        await updateProjectStatus(projectId, status);
      } catch (err) {
        setProjects(previousProjects);
        showError(err);
      }
    },
    [projects, showError],
  );

  return {
    projects,
    loading,
    handleSaveProject,
    handleSaveNotes,
    handleConfirmDelete,
    handleMoveProject,
  };
}
