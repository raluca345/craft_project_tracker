import { useCallback, useState } from "react";

export function useProjectModals() {
  const [editingProject, setEditingProject] = useState(null);
  const [editingNotes, setEditingNotes] = useState(null);
  const [isNewProjectOpen, setIsNewProjectOpen] = useState(false);
  const [projectToDelete, setProjectToDelete] = useState(null);

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

  const handleDeleteProject = useCallback((project) => {
    setProjectToDelete(project);
  }, []);

  const handleCancelDelete = useCallback(() => {
    setProjectToDelete(null);
  }, []);

  return {
    editingProject,
    editingNotes,
    isNewProjectOpen,
    projectToDelete,
    handleEditProject,
    handleEditNotes,
    handleAddNew,
    handleCloseModal,
    handleCloseNotesModal,
    handleDeleteProject,
    handleCancelDelete,
    setEditingProject,
    setEditingNotes,
    setIsNewProjectOpen,
    setProjectToDelete,
  };
}
