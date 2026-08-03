import ProjectModal from "../commons/ProjectModal";

export default function Board({ children, onSave }) {
  return (
    <div className="flex flex-col gap-6 pb-4">
      <ProjectModal onSave={onSave} />
      {children}
    </div>
  );
}
