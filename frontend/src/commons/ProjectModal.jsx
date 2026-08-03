import React, { useEffect, useRef, useState } from "react";
import FormField from "../ui/FormField";
import AutocompleteField from "../ui/AutocompleteField";
import TagInput from "../ui/TagInput";
import { formatStatus } from "../api/apiStatuses";

const INPUT_STYLE =
  "rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-fuchsia-400 focus:outline-none";

const INPUT_ERROR_STYLE =
  "rounded-lg border border-red-400 px-3 py-2 text-sm focus:border-red-500 focus:outline-none";

const CRAFT_SUGGESTIONS = [
  "knitting",
  "crocheting",
  "weaving",
  "macrame",
  "embroidery",
];

const TOOL_SUGGESTIONS = [
  "knitting needles",
  "sewing needles",
  "crochet hook",
  "loom",
  "fingers",
  "knitting machine",
];

const INITIAL_FORM_STATE = {
  patternName: "",
  craft: "",
  toolType: "",
  toolSize: "",
  yarnWeightCategory: "",
  yarnUsed: "",
  amountUsed: 0,
  status: "TO_DO",
  imageUrl: "",
  notes: "",
  tags: [],
};

const INITIAL_ERRORS = {
  patternName: null,
  craft: null,
  toolType: null,
  toolSize: null,
  yarnWeightCategory: null,
  yarnUsed: null,
  amountUsed: null,
  status: null,
  imageUrl: null,
  notes: null,
  tags: null,
};

function validate(formState) {
  const errors = { ...INITIAL_ERRORS };

  if (!formState.patternName.trim()) {
    errors.patternName = "Pattern name is required";
  } else if (formState.patternName.length > 255) {
    errors.patternName = "Pattern name must be 255 characters or fewer";
  }

  if (!formState.craft.trim()) {
    errors.craft = "Craft is required";
  } else if (formState.craft.length > 100) {
    errors.craft = "Craft must be 100 characters or fewer";
  }

  if (!formState.toolType.trim()) {
    errors.toolType = "Tool is required";
  } else if (formState.toolType.length > 100) {
    errors.toolType = "Tool must be 100 characters or fewer";
  }

  if (!formState.toolSize.trim()) {
    errors.toolSize = "Tool size is required";
  } else if (formState.toolSize.length > 100) {
    errors.toolSize = "Tool size must be 100 characters or fewer";
  }

  if (formState.yarnWeightCategory.length > 100) {
    errors.yarnWeightCategory = "Yarn weight must be 100 characters or fewer";
  }

  if (formState.yarnUsed.length > 255) {
    errors.yarnUsed = "Yarn used must be 255 characters or fewer";
  }

  if (formState.amountUsed < 0) {
    errors.amountUsed = "Amount used cannot be negative";
  }

  if (!formState.status) {
    errors.status = "Status is required";
  }

  if (formState.imageUrl.length > 2048) {
    errors.imageUrl = "Image URL must be 2048 characters or fewer";
  }

  if (formState.tags.some((t) => t.length > 50)) {
    errors.tags = "Tags must be 50 characters or fewer";
  }

  return errors;
}

function hasErrors(errors) {
  return Object.values(errors).some(Boolean);
}

export default function ProjectModal({
  onSave,
  onClose,
  existingProject = null,
}) {
  const dialogRef = useRef(null);
  const [formState, setFormState] = useState(INITIAL_FORM_STATE);
  const [errors, setErrors] = useState(INITIAL_ERRORS);

  useEffect(() => {
    if (existingProject) {
      setFormState({
        patternName: existingProject.patternName ?? "",
        craft: existingProject.craft ?? "",
        toolType: existingProject.toolType ?? "",
        toolSize: existingProject.toolSize ?? "",
        yarnWeightCategory: existingProject.yarnWeightCategory ?? "",
        yarnUsed: existingProject.yarnUsed ?? "",
        amountUsed: existingProject.amountUsed ?? 0,
        status: existingProject.status ?? "TO_DO",
        imageUrl: existingProject.imageUrl ?? "",
        notes: existingProject.notes ?? "",
        tags: existingProject.tags ?? [],
      });
      setErrors(INITIAL_ERRORS);
      if (!dialogRef.current?.open) {
        dialogRef.current?.showModal();
      }
    } else {
      setFormState(INITIAL_FORM_STATE);
      setErrors(INITIAL_ERRORS);
    }
  }, [existingProject]);

  function updateField(field, value) {
    setFormState((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: null }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    const nextErrors = validate(formState);
    setErrors(nextErrors);
    if (hasErrors(nextErrors)) return;

    let payload;
    if (existingProject) {
      payload = {
        id: existingProject.id,
        patternName: formState.patternName,
        craft: formState.craft,
        toolType: formState.toolType,
        toolSize: formState.toolSize,
        yarnWeightCategory: formState.yarnWeightCategory || null,
        yarnUsed: formState.yarnUsed || null,
        amountUsed: Number(formState.amountUsed) || 0,
        status: formState.status,
        imageUrl: formState.imageUrl || null,
        notes: formState.notes || null,
        tags: formState.tags,
      };
    } else {
      payload = {
        patternName: formState.patternName,
        craft: formState.craft,
        toolType: formState.toolType,
        toolSize: formState.toolSize,
        yarnWeightCategory: formState.yarnWeightCategory || null,
        yarnUsed: formState.yarnUsed || null,
        imageUrl: formState.imageUrl || null,
        notes: formState.notes || null,
        tags: formState.tags,
      };
    }

    Promise.resolve(onSave(payload)).then(() => {
      dialogRef.current?.close();
      setFormState(INITIAL_FORM_STATE);
      setErrors(INITIAL_ERRORS);
    });
  }

  return (
    <dialog
      ref={dialogRef}
      id="dialog-ex"
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
            commandfor="dialog-ex"
            command="close"
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
