import { useEffect, useState } from "react";
import "./App.css";
import Board from "./ui/Board";
import ProjectCard from "./ui/ProjectCard";
import ProjectLane from "./ui/ProjectLane";
import { DragDropProvider } from "@dnd-kit/react";
import { getStatuses, formatStatus } from "./api/apiStatuses";
import {
  getProjects,
  updateProjectStatus,
  createProject,
  editProject,
} from "./api/apiProjects";
import Footer from "./ui/Footer";

function parseProjectSlotId(id) {
  if (typeof id !== "string" || !id.startsWith("project-slot:")) {
    return null;
  }

  const [, status, index] = id.split(":");
  const parsedIndex = Number(index);

  if (!status || Number.isNaN(parsedIndex)) {
    return null;
  }

  return { status, index: parsedIndex };
}

function reorderProject(prevProjects, projectId, status, toIndex) {
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

function moveProjectToStatus(prevProjects, projectId, status, toIndex) {
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

function App() {
  const [lane, setLane] = useState(null);
  const [statuses, setStatuses] = useState([]);
  const [projects, setProjects] = useState([]);
  const [editingProject, setEditingProject] = useState(null);

  useEffect(() => {
    getStatuses().then(setStatuses);
    getProjects().then(setProjects);
  }, []);

  const projectsByStatus = Object.fromEntries(
    statuses.map((s) => [s, projects.filter((p) => p.status === s)]),
  );

  function handleEditProject(project) {
    setEditingProject(project);
  }

  async function handleSaveProject(project) {
    if (project.id) {
      const updated = await editProject(project.id, project);
      setProjects((prev) =>
        prev.map((p) => (p.id === updated.id ? updated : p)),
      );
      setEditingProject(null);
    } else {
      const created = await createProject(project);
      setProjects((prev) => [...prev, created]);
    }
  }

  function handleCloseModal() {
    setEditingProject(null);
  }

  return (
    <div className="min-h-screen flex flex-col">
      <DragDropProvider
        onDragEnd={(event) => {
          if (event.canceled) return;

          const { source, target } = event.operation;
          const sourceId = source?.id;
          if (sourceId == null) return;

          const project = projects.find((p) => p.id === sourceId);
          if (!project) return;

          const targetSlot = parseProjectSlotId(target?.id);
          if (targetSlot && statuses.includes(targetSlot.status)) {
            if (targetSlot.status === project.status) {
              setProjects((prev) =>
                reorderProject(prev, sourceId, project.status, targetSlot.index),
              );
              return;
            }

            setLane(targetSlot.status);
            setProjects((prev) =>
              moveProjectToStatus(prev, sourceId, targetSlot.status, targetSlot.index),
            );
            updateProjectStatus(sourceId, targetSlot.status);
            return;
          }

          // Cross-lane move: target is a lane (status id)
          const newStatus = target?.id;
          if (!statuses.includes(newStatus)) return;

          setLane(newStatus);
          setProjects((prev) =>
            prev.map((p) =>
              p.id === sourceId ? { ...p, status: newStatus } : p,
            ),
          );
          updateProjectStatus(sourceId, newStatus);
        }}
      >
        <main className="flex-1 p-4">
          <h1 className="flex p-4 m-2 text-2xl font-bold text-(--accent-color2)">
            Craft Project Tracker
          </h1>
          <Board
            onSave={handleSaveProject}
            existingProject={editingProject}
            onClose={handleCloseModal}
          >
            {statuses.length > 0 ? (
              statuses.map((status) => (
                <ProjectLane
                  key={status}
                  id={status}
                  title={formatStatus(status)}
                  onEdit={handleEditProject}
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
