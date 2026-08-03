import ProjectModal from "../commons/ProjectModal";

export default function Board({
  children,
  onSave,
  onClose,
  existingProject = null,
}) {
  return (
    <div className="flex flex-col gap-6 pb-4">
      <ProjectModal
        onSave={onSave}
        onClose={onClose}
        existingProject={existingProject}
      />
      {children}
    </div>
  );
}
