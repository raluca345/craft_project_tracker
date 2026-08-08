import ProjectModal from "../commons/ProjectModal";
import ProjectLane from "./ProjectLane";
import ProjectCard from "./ProjectCard";
import { formatStatus } from "../api/apiStatuses";

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
              onEdit={onEditProject}
              onEditNotes={onEditNotes}
              onDelete={onDelete}
              onAddNew={onAddNew}
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
    </div>
  );
}
