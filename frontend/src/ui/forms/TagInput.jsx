import { useState, useEffect } from "react";
import { autocomplete } from "../../api/apiTags";

export default function TagInput({ value, onChange }) {
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState([]);

  useEffect(() => {
    if (!query) return;
    const timeout = setTimeout(() => {
      autocomplete(query).then((tags) => {
        // hide tags already added as chips
        setSuggestions(tags.filter((t) => !value.includes(t.name)));
      });
    }, 250);
    return () => clearTimeout(timeout);
  }, [query, value]);

  if (!query && suggestions.length > 0) {
    setSuggestions([]);
  }

  function addTag(name) {
    const trimmed = name.trim();
    if (trimmed && !value.includes(trimmed)) {
      onChange([...value, trimmed]);
    }
    setQuery("");
    setSuggestions([]);
  }

  function removeTag(name) {
    onChange(value.filter((t) => t !== name));
  }

  function handleKeyDown(e) {
    if (e.key === "Enter" || e.key === ",") {
      e.preventDefault();
      addTag(query);
    } else if (e.key === "Backspace" && !query && value.length > 0) {
      removeTag(value[value.length - 1]); // backspace on empty input pops last chip
    }
  }

  return (
    <div className="flex flex-col gap-2">
      <div className="flex flex-wrap gap-2">
        {value.map((tag) => (
          <span
            key={tag}
            className="flex items-center gap-1 rounded-md bg-amber-100 px-2 py-0.5 text-sm font-bold text-amber-700"
          >
            #{tag}
            <button
              type="button"
              onClick={() => removeTag(tag)}
              aria-label={`Remove ${tag}`}
              className="cursor-pointer"
            >
              ×
            </button>
          </span>
        ))}
      </div>

      <div className="relative">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Add a tag..."
          className="w-full rounded-lg border border-slate-300 px-3 py-2"
        />
        {suggestions.length > 0 && (
          <ul className="absolute z-10 mt-1 w-full rounded-lg border border-slate-200 bg-white shadow-md">
            {suggestions.map((tag) => (
              <li key={tag.id}>
                <button
                  type="button"
                  onClick={() => addTag(tag.name)}
                  className="w-full px-3 py-1.5 text-left hover:bg-amber-50"
                >
                  {tag.name}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
