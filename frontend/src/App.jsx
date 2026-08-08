import { useEffect, useState } from "react";
import "./App.css";
import Board from "./ui/Board";
import ProjectCard from "./ui/ProjectCard";
import ProjectLane from "./ui/ProjectLane";
import { DragDropProvider } from "@dnd-kit/react";
import { getStatuses, formatStatus } from "./api/apiStatuses";
import Footer from "./ui/Footer";
import NotesModal from "./ui/NotesModal";
import ConfirmDialog from "./ui/ConfirmDialog";
import ErrorBox from "./ui/ErrorBox";
import SearchBar from "./ui/SearchBar";
import { useProjectError } from "./hooks/useProjectError";
import { useProjectModals } from "./hooks/useProjectModals";
import { useProjectsList } from "./hooks/useProjectsList";
import { parseProjectSlotId } from "./utils/projectOrdering";
import { filterProjects } from "./utils/searchProjects";

//TODO: search bar and the grid view for the results
// the grid component
// pagination controls
// the tags should be blue and clickable. on click in searches only by that tag
// real auth. maybe a stylized login and sign up page?

function App() {
  const [statuses, setStatuses] = useState([]);
  const [query, setQuery] = useState("");
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

  const projectsByStatus = Object.fromEntries(
    statuses.map((s) => [s, filteredProjects.filter((p) => p.status === s)]),
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
            <SearchBar onSearch={setQuery} />
          </div>
          <Board
            onSave={handleSaveProject}
            existingProject={editingProject}
            onClose={handleCloseModal}
            isNewProjectOpen={isNewProjectOpen}
          >
            {statuses.length > 0 ? (
              filteredProjects.length > 0 ? (
                statuses.map((status) => (
                  <ProjectLane
                    key={status}
                    id={status}
                    title={formatStatus(status)}
                    onEdit={handleEditProject}
                    onEditNotes={handleEditNotes}
                    onDelete={handleDeleteProject}
                    onAddNew={handleAddNew}
                  >
                    {projectsByStatus[status]?.map((project) => (
                      <ProjectCard key={project.id} project={project} />
                    ))}
                  </ProjectLane>
                ))
              ) : (
                <p className="text-slate-400 text-sm p-4">No projects found</p>
              )
            ) : (
              <p className="text-slate-400 text-sm p-4">Loading lanes…</p>
            )}
          </Board>
        </main>
      </DragDropProvider>
      <Footer />
    </div>
  );
}

export default App;
