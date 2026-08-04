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
import { useProjects } from "./hooks/useProjects";
import { parseProjectSlotId } from "./utils/projectOrdering";

function App() {
  const [statuses, setStatuses] = useState([]);
  const {
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
  } = useProjects();

  useEffect(() => {
    getStatuses().then(setStatuses);
  }, []);

  const projectsByStatus = Object.fromEntries(
    statuses.map((s) => [s, projects.filter((p) => p.status === s)]),
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
          <h1 className="flex p-4 m-2 text-2xl font-bold text-(--accent-color2)">
            Craft Project Tracker
          </h1>
          <Board
            onSave={handleSaveProject}
            existingProject={editingProject}
            onClose={handleCloseModal}
            isNewProjectOpen={isNewProjectOpen}
          >
            {statuses.length > 0 ? (
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
