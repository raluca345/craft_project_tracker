export const INITIAL_FORM_STATE = {
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

export const INITIAL_ERRORS = {
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

export function validate(formState) {
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

export function hasErrors(errors) {
  return Object.values(errors).some(Boolean);
}

export function projectToForm(project) {
  return {
    patternName: project.patternName ?? "",
    craft: project.craft ?? "",
    toolType: project.toolType ?? "",
    toolSize: project.toolSize ?? "",
    yarnWeightCategory: project.yarnWeightCategory ?? "",
    yarnUsed: project.yarnUsed ?? "",
    amountUsed: project.amountUsed ?? 0,
    status: project.status ?? "TO_DO",
    imageUrl: project.imageUrl ?? "",
    notes: project.notes ?? "",
    tags: project.tags ?? [],
  };
}

export function formToPayload(formState, existingProject) {
  const payload = {
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

  if (existingProject) {
    return {
      id: existingProject.id,
      ...payload,
      amountUsed: Number(formState.amountUsed) || 0,
      status: formState.status,
    };
  }

  return payload;
}
