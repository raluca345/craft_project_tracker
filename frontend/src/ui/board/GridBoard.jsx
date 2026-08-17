import ProjectModal from "../project/ProjectModal";
import ProjectCard from "../project/ProjectCard";

export default function GridBoard({
  projects,
  onSave,
  onClose,
  existingProject = null,
  isNewProjectOpen = false,
  onEditProject,
  onEditNotes,
  onDelete,
  onTagClick,
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
            onTagClick={onTagClick}
          />
        ))
      ) : (
        <p className="text-slate-400 text-sm p-4">No projects found</p>
      )}
    </div>
  );
}
