import ProjectModal from "../commons/ProjectModal";
import ProjectCard from "./ProjectCard";

export default function GridBoard({
  projects,
  onSave,
  onClose,
  existingProject = null,
  isNewProjectOpen = false,
  onEditProject,
  onEditNotes,
  onDelete,
}) {
  return (
    <div className="grid grid-cols-[repeat(auto-fill,minmax(200px,1fr))] gap-4">
      <ProjectModal
        onSave={onSave}
        onClose={onClose}
        existingProject={existingProject}
        isOpen={isNewProjectOpen}
      />
      {projects.length > 0 ? (
        projects.map((project) => (
          <ProjectCard
            key={project.id}
            project={project}
            draggable={false}
            onEdit={onEditProject}
            onEditNotes={onEditNotes}
            onDelete={onDelete}
          />
        ))
      ) : (
        <p className="text-slate-400 text-sm p-4">No projects found</p>
      )}
    </div>
  );
}
