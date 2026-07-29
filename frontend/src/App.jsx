import { useState } from "react";
import "./App.css";
import Board from "./commons/Board";
import ProjectCard from "./commons/ProjectCard";
import ProjectLane from "./commons/ProjectLane";
import { DragDropProvider } from "@dnd-kit/react";
import { PROJECT_TYPE_ICON_CREDITS } from "./projectTypeIcons";

function App() {
  const [lane, setLane] = useState("not-started");

  return (
    <div className="min-h-screen flex flex-col">
      <DragDropProvider
        onDragEnd={(event) => {
          if (event.canceled) return;

          const { target } = event.operation;
          if (target?.id === "not-started" || target?.id === "in-progress") {
            setLane(target.id);
          }
        }}
      >
        <main className="flex-1 p-4">
          <h1 className="flex p-4 m-2 text-2xl font-bold text-(--accent-color2)">
            Craft Project Tracker
          </h1>
          <Board>
            <ProjectLane id="not-started" title="Not Started">
              {lane === "not-started" && <ProjectCard id="project-card" />}
            </ProjectLane>
            <ProjectLane id="in-progress" title="In Progress">
              {lane === "in-progress" && <ProjectCard id="project-card" />}
            </ProjectLane>
          </Board>
        </main>
      </DragDropProvider>
      <footer className="mt-auto flex justify-center px-4 pb-4 pt-2 text-sm text-slate-600">
        <details className="group rounded-xl border border-slate-200 bg-white/80 px-4 py-2 shadow-sm backdrop-blur-sm">
          <summary className="cursor-pointer list-none font-medium text-slate-700">
            Icon credits
          </summary>
          <div className="mt-3 grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
            {PROJECT_TYPE_ICON_CREDITS.map((item) => (
              <div key={item.type} className="min-w-0">
                <p className="font-semibold text-slate-800">{item.creditLabel}</p>
                <p className="text-xs leading-5 text-slate-600">
                  by {item.author} from{" "}
                  <a
                    href={item.sourceUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="font-medium text-(--accent-color2) hover:underline"
                  >
                    Noun Project
                  </a>{" "}
                  (CC BY 3.0)
                </p>
              </div>
            ))}
          </div>
        </details>
      </footer>
    </div>
  );
}

export default App;
