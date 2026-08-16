import { useEffect, useState } from "react";
import "../App.css";
import SwimlaneBoard from "../ui/SwimlaneBoard";
import GridBoard from "../ui/GridBoard";
import { DragDropProvider } from "@dnd-kit/react";
import { getStatuses } from "../api/apiStatuses";
import NotesModal from "../ui/NotesModal";
import ConfirmDialog from "../ui/ConfirmDialog";
import ErrorBox from "../ui/ErrorBox";
import SearchBar from "../ui/SearchBar";
import Pagination from "../ui/Pagination";
import { useProjectError } from "../hooks/useProjectError";
import { useProjectModals } from "../hooks/useProjectModals";
import { useProjectsList } from "../hooks/useProjectsList";
import { parseProjectSlotId } from "../utils/projectOrdering";
import { filterProjects } from "../utils/searchProjects";

const PAGE_SIZE = 12;

function BoardPage() {
  const [statuses, setStatuses] = useState([]);
  const [draft, setDraft] = useState("");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(1);
  const { error, showError, clearError } = useProjectError();

  const {
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
  } = useProjectModals();

  const {
    projects,
    loading,
    handleSaveProject,
    handleSaveNotes,
    handleConfirmDelete,
    handleMoveProject,
  } = useProjectsList({
    showError,
    setEditingProject,
    setEditingNotes,
    setIsNewProjectOpen,
    setProjectToDelete,
  });

  useEffect(() => {
    getStatuses().then(setStatuses);
  }, []);

  const filteredProjects = filterProjects(projects, query);

  function handleTagClick(tag) {
    const nextQuery = `#${tag}`;
    setDraft(nextQuery);
    setQuery(nextQuery);
    setPage(1);
  }
  const totalPages = Math.max(
    1,
    Math.ceil(filteredProjects.length / PAGE_SIZE),
  );
  const currentPage = Math.min(page, totalPages);
  const pageProjects = filteredProjects.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE,
  );

  function handleDragEnd(event) {
    if (event.canceled) return;

    const { source, target } = event.operation;
    const sourceId = source?.id;
    if (sourceId == null) return;

    const project = projects.find((p) => p.id === sourceId);
    if (!project) return;

    const targetSlot = parseProjectSlotId(target?.id);
    if (targetSlot && statuses.includes(targetSlot.status)) {
      handleMoveProject(sourceId, targetSlot.status, targetSlot.index);
      return;
    }

    // Cross-lane move: target is a lane (status id)
    const newStatus = target?.id;
    if (!statuses.includes(newStatus)) return;

    handleMoveProject(sourceId, newStatus, undefined);
  }

  return (
    <div className="min-h-screen flex flex-col">
      <DragDropProvider onDragEnd={handleDragEnd}>
        <main className="flex-1 p-4">
          <ErrorBox message={error} onDismiss={clearError} />
          <NotesModal
            project={editingNotes}
            onCancel={handleCloseNotesModal}
            onSave={handleSaveNotes}
          />
          <ConfirmDialog
            project={projectToDelete}
            onCancel={handleCancelDelete}
            onConfirm={handleConfirmDelete}
          />
          <div className="flex items-center justify-between">
            <h1 className="p-4 m-2 text-2xl font-bold text-(--accent-color2)">
              Craft Project Tracker
            </h1>
            <SearchBar
              value={draft}
              onChange={setDraft}
              onSearch={(q) => {
                setQuery(q);
                setPage(1);
              }}
            />
          </div>
          {query.trim() ? (
            loading ? (
              <p className="text-slate-400 text-sm p-4">Loading projects…</p>
            ) : (
              <>
                <GridBoard
                  projects={pageProjects}
                  onSave={handleSaveProject}
                  existingProject={editingProject}
                  onClose={handleCloseModal}
                  isNewProjectOpen={isNewProjectOpen}
                  onEditProject={handleEditProject}
                  onEditNotes={handleEditNotes}
                  onDelete={handleDeleteProject}
                  onTagClick={handleTagClick}
                />
                <Pagination
                  page={currentPage}
                  totalPages={totalPages}
                  onPageChange={setPage}
                />
              </>
            )
          ) : (
            <SwimlaneBoard
              statuses={statuses}
              projects={filteredProjects}
              onSave={handleSaveProject}
              existingProject={editingProject}
              onClose={handleCloseModal}
              isNewProjectOpen={isNewProjectOpen}
              onEditProject={handleEditProject}
              onEditNotes={handleEditNotes}
              onDelete={handleDeleteProject}
              onTagClick={handleTagClick}
              onAddNew={handleAddNew}
            />
          )}
        </main>
      </DragDropProvider>
    </div>
  );
}

export default BoardPage;