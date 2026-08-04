import { useEffect, useRef, useState } from "react";
import FormField from "../ui/FormField";
import AutocompleteField from "../ui/AutocompleteField";
import TagInput from "../ui/TagInput";
import { formatStatus } from "../api/apiStatuses";
import {
  INITIAL_FORM_STATE,
  INITIAL_ERRORS,
  validate,
  hasErrors,
  projectToForm,
  formToPayload,
} from "./projectForm";
import {
  CRAFT_SUGGESTIONS,
  TOOL_SUGGESTIONS,
} from "../constants/projectSuggestions";

const INPUT_STYLE =
  "rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-fuchsia-400 focus:outline-none";

const INPUT_ERROR_STYLE =
  "rounded-lg border border-red-400 px-3 py-2 text-sm focus:border-red-500 focus:outline-none";

export default function ProjectModal({
  onSave,
  onClose,
  existingProject = null,
  isOpen = false,
}) {
  const dialogRef = useRef(null);
  const [formState, setFormState] = useState(INITIAL_FORM_STATE);
  const [errors, setErrors] = useState(INITIAL_ERRORS);
  const [prevOpenRequest, setPrevOpenRequest] = useState(
    existingProject ?? isOpen,
  );

  const openRequest = existingProject ?? isOpen;
  if (openRequest !== prevOpenRequest) {
    setPrevOpenRequest(openRequest);
    setFormState(
      existingProject ? projectToForm(existingProject) : INITIAL_FORM_STATE,
    );
    setErrors(INITIAL_ERRORS);
  }

  useEffect(() => {
    if (existingProject || isOpen) {
      if (!dialogRef.current?.open) {
        dialogRef.current?.showModal();
      }
    } else if (dialogRef.current?.open) {
      dialogRef.current?.close();
    }
  }, [existingProject, isOpen]);

  function updateField(field, value) {
    setFormState((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: null }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    const nextErrors = validate(formState);
    setErrors(nextErrors);
    if (hasErrors(nextErrors)) return;

    const payload = formToPayload(formState, existingProject);

    Promise.resolve(onSave(payload))
      .then(() => {
        dialogRef.current?.close();
        setFormState(INITIAL_FORM_STATE);
        setErrors(INITIAL_ERRORS);
      })
      .catch(() => {
        // Keep the dialog open so the user can retry.
      });
  }

  return (
    <dialog
      ref={dialogRef}
      onClose={onClose}
      className="m-auto w-96 max-w-[90vw] overflow-auto scrollbar-thumb-fuchsia-100 scrollbar-track-fuchsia-200 rounded-2xl p-6 shadow-xl"
    >
      <form className="flex flex-col gap-4" onSubmit={handleSubmit} noValidate>
        <h2 className="text-lg font-semibold text-slate-800">
          {existingProject ? "Edit Project" : "New Project"}
        </h2>
        <FormField label="Pattern Name" error={errors.patternName}>
          <input
            type="text"
            id="project-name"
            placeholder="e.g. Easy Knitted Cable Hat"
            className={errors.patternName ? INPUT_ERROR_STYLE : INPUT_STYLE}
            value={formState.patternName}
            onChange={(e) => updateField("patternName", e.target.value)}
          />
        </FormField>
        <AutocompleteField
          label="Craft"
          value={formState.craft}
          onChange={(value) => updateField("craft", value)}
          suggestions={CRAFT_SUGGESTIONS}
          error={errors.craft}
        />
        <AutocompleteField
          label="Tool"
          value={formState.toolType}
          onChange={(value) => updateField("toolType", value)}
          suggestions={TOOL_SUGGESTIONS}
          error={errors.toolType}
        />
        <FormField label="Tool Size" error={errors.toolSize}>
          <input
            type="text"
            id="tool-size"
            placeholder="e.g. 5.0 mm / US 8"
            className={errors.toolSize ? INPUT_ERROR_STYLE : INPUT_STYLE}
            value={formState.toolSize}
            onChange={(e) => updateField("toolSize", e.target.value)}
          />
        </FormField>
        <FormField label="Yarn Weight" error={errors.yarnWeightCategory}>
          <input
            type="text"
            id="yarn-weight"
            placeholder="e.g. Worsted"
            className={
              errors.yarnWeightCategory ? INPUT_ERROR_STYLE : INPUT_STYLE
            }
            value={formState.yarnWeightCategory}
            onChange={(e) => updateField("yarnWeightCategory", e.target.value)}
          />
        </FormField>
        <FormField label="Yarn Used" error={errors.yarnUsed}>
          <input
            type="text"
            id="yarn-used"
            placeholder="e.g. Lion Brand Wool-Ease"
            className={errors.yarnUsed ? INPUT_ERROR_STYLE : INPUT_STYLE}
            value={formState.yarnUsed}
            onChange={(e) => updateField("yarnUsed", e.target.value)}
          />
        </FormField>
        {existingProject && (
          <>
            <FormField label="Amount Used" error={errors.amountUsed}>
              <div className="relative">
                <input
                  type="number"
                  id="amount-used"
                  min="0"
                  className={`${
                    errors.amountUsed ? INPUT_ERROR_STYLE : INPUT_STYLE
                  } pr-10`}
                  value={formState.amountUsed}
                  onChange={(e) => updateField("amountUsed", e.target.value)}
                />
                <span className="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-sm text-slate-400">
                  g
                </span>
              </div>
            </FormField>
            <FormField label="Status" error={errors.status}>
              <select
                id="project-status"
                className={errors.status ? INPUT_ERROR_STYLE : INPUT_STYLE}
                value={formState.status}
                onChange={(e) => updateField("status", e.target.value)}
              >
                <option value="TO_DO">{formatStatus("TO_DO")}</option>
                <option value="IN_PROGRESS">
                  {formatStatus("IN_PROGRESS")}
                </option>
                <option value="ASSEMBLING">{formatStatus("ASSEMBLING")}</option>
                <option value="FINISHED">{formatStatus("FINISHED")}</option>
              </select>
            </FormField>
          </>
        )}
        <FormField label="Image URL" error={errors.imageUrl}>
          <input
            type="url"
            id="image-url"
            placeholder="https://..."
            className={errors.imageUrl ? INPUT_ERROR_STYLE : INPUT_STYLE}
            value={formState.imageUrl}
            onChange={(e) => updateField("imageUrl", e.target.value)}
          />
        </FormField>
        <FormField label="Notes" error={errors.notes}>
          <textarea
            id="notes"
            rows="3"
            placeholder="Pattern notes, modifications, ideas..."
            className={errors.notes ? INPUT_ERROR_STYLE : INPUT_STYLE}
            value={formState.notes}
            onChange={(e) => updateField("notes", e.target.value)}
          />
        </FormField>
        <FormField label="Tags" error={errors.tags}>
          <TagInput
            value={formState.tags}
            onChange={(tags) => updateField("tags", tags)}
          />
        </FormField>
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={() => dialogRef.current?.close()}
            className="rounded-lg px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
          >
            Close
          </button>
          <button
            type="submit"
            className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white hover:bg-fuchsia-600"
          >
            Save
          </button>
        </div>
      </form>
    </dialog>
  );
}
