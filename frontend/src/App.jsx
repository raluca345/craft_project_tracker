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
} from "./api/apiProjects";
import Footer from "./ui/Footer";

function App() {
  const [lane, setLane] = useState(null);
  const [statuses, setStatuses] = useState([]);
  const [projects, setProjects] = useState([]);

  useEffect(() => {
    getStatuses().then(setStatuses);
    getProjects().then(setProjects);
  }, []);

  const projectsByStatus = Object.fromEntries(
    statuses.map((s) => [s, projects.filter((p) => p.status === s)]),
  );

  function handleCreateProject(project) {
    createProject(project).then((created) => {
      setProjects((prev) => [...prev, created]);
    });
  }

  return (
    <div className="min-h-screen flex flex-col">
      <DragDropProvider
        onDragEnd={(event) => {
          if (event.canceled) return;

          const { source, target } = event.operation;
          const newStatus = target?.id;
          if (!statuses.includes(newStatus)) return;

          setLane(newStatus);
          setProjects((prev) =>
            prev.map((p) =>
              p.id === source?.id ? { ...p, status: newStatus } : p,
            ),
          );
          updateProjectStatus(source?.id, newStatus);
        }}
      >
        <main className="flex-1 p-4">
          <h1 className="flex p-4 m-2 text-2xl font-bold text-(--accent-color2)">
            Craft Project Tracker
          </h1>
          <Board onSave={handleCreateProject}>
            {statuses.length > 0 ? (
              statuses.map((status) => (
                <ProjectLane
                  key={status}
                  id={status}
                  title={formatStatus(status)}
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
