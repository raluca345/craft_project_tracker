import { useState } from "react";
import "./App.css";
import Board from "./commons/Board";
import ProjectCard from "./commons/ProjectCard";
import ProjectLane from "./commons/ProjectLane";
import { DragDropProvider } from "@dnd-kit/react";

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
      <footer className="mt-auto flex justify-center p-4 text-sm">
        <p>
          crochet icon by Ayub Irawan from{" "}
          <a
            href="https://thenounproject.com/browse/icons/term/crochet/"
            target="_blank"
            rel="noreferrer"
            title="crochet Icons"
          >
            Noun Project
          </a>{" "}
          (CC BY 3.0)
        </p>
      </footer>
    </div>
  );
}

export default App;
