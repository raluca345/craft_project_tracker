import ProjectModal from "../project/ProjectModal";
import ProjectCard from "../project/ProjectCard";
import ProjectLane from "./ProjectLane";
import { formatStatus } from "../../api/apiStatuses";

export default function SwimlaneBoard({
  statuses,
  projects,
  onSave,
  onClose,
  existingProject = null,
  isNewProjectOpen = false,
  onEditProject,
  onEditNotes,
  onDelete,
  onTagClick,
  onAddNew,
}) {
  const projectsByStatus = Object.fromEntries(
    statuses.map((s) => [s, projects.filter((p) => p.status === s)]),
  );

  return (
    <div className="flex flex-col gap-6 pb-4">
      <ProjectModal
        onSave={onSave}
        onClose={onClose}
        existingProject={existingProject}
        isOpen={isNewProjectOpen}
      />
      {statuses.length > 0 ? (
        projects.length > 0 ? (
          statuses.map((status) => (
            <ProjectLane
              key={status}
              id={status}
              title={formatStatus(status)}
              onAddNew={onAddNew}
            >
              {projectsByStatus[status]?.map((project, index) => (
                <ProjectCard
                  key={project.id}
                  project={project}
                  index={index}
                  onEdit={onEditProject}
                  onEditNotes={onEditNotes}
                  onDelete={onDelete}
                  onTagClick={onTagClick}
                />
              ))}
            </ProjectLane>
          ))
        ) : (
          <p className="text-slate-400 text-sm p-4">No projects found</p>
        )
      ) : (
        <p className="text-slate-400 text-sm p-4">Loading lanes…</p>
      )}
    </div>
  );
}
