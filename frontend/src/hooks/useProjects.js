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
import { getErrorMessage } from "../commons/errors";

export function useProjects() {
  const [projects, setProjects] = useState([]);
  const [editingProject, setEditingProject] = useState(null);
  const [editingNotes, setEditingNotes] = useState(null);
  const [isNewProjectOpen, setIsNewProjectOpen] = useState(false);
  const [projectToDelete, setProjectToDelete] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    getProjects()
      .then(setProjects)
      .catch((err) => setError(getErrorMessage(err)));
  }, []);

  const showError = useCallback((err) => {
    console.error(err);
    setError(getErrorMessage(err));
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const handleEditProject = useCallback((project) => {
    setEditingProject(project);
  }, []);

  const handleEditNotes = useCallback((project) => {
    setEditingNotes(project);
  }, []);

  const handleAddNew = useCallback(() => {
    setIsNewProjectOpen(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setEditingProject(null);
    setIsNewProjectOpen(false);
  }, []);

  const handleCloseNotesModal = useCallback(() => {
    setEditingNotes(null);
  }, []);

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
    [showError],
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
    [showError],
  );

  const handleDeleteProject = useCallback((project) => {
    setProjectToDelete(project);
  }, []);

  const handleCancelDelete = useCallback(() => {
    setProjectToDelete(null);
  }, []);

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
    [showError],
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
    editingProject,
    editingNotes,
    isNewProjectOpen,
    projectToDelete,
    error,
    handleEditProject,
    handleEditNotes,
    handleAddNew,
    handleCloseModal,
    handleCloseNotesModal,
    handleSaveProject,
    handleSaveNotes,
    handleDeleteProject,
    handleCancelDelete,
    handleConfirmDelete,
    handleMoveProject,
    clearError,
  };
}
