import { useState, useEffect, useMemo } from "react";
import { IMAGE_ACCEPT, validateImageFile } from "../../commons/imageValidation";

export default function ImageUploadField({
  onFileSelected,
  onInvalid = () => {},
  existingImageUrl,
}) {
  const [file, setFile] = useState(null);
  const previewUrl = useMemo(() => {
    if (file) return URL.createObjectURL(file);
    return existingImageUrl ?? null;
  }, [file, existingImageUrl]);

  useEffect(() => {
    return () => {
      if (file) URL.revokeObjectURL(previewUrl);
    };
  }, [file, previewUrl]);

  function handleChange(e) {
    const selected = e.target.files[0];
    if (!selected) return;

    const error = validateImageFile(selected);
    if (error) {
      onInvalid(error);
      return;
    }

    setFile(selected);
    onFileSelected(selected);
  }

  return (
    <div className="flex flex-col gap-2">
      {previewUrl && (
        <img
          src={previewUrl}
          alt="Cover preview"
          className="h-40 w-full rounded-lg border border-slate-200 object-cover"
        />
      )}
      <label className="cursor-pointer rounded-lg border border-dashed border-slate-300 px-3 py-2 text-center text-sm text-slate-500 transition-colors hover:border-fuchsia-400 hover:text-fuchsia-600">
        {file ? "Change image" : "Choose an image"}
        <input
          type="file"
          accept={IMAGE_ACCEPT}
          hidden
          onChange={handleChange}
        />
      </label>
    </div>
  );
}
