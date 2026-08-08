import { FaMagnifyingGlass } from "react-icons/fa6";
import { useState } from "react";

export default function SearchBar({ onSearch }) {
  const [query, setQuery] = useState("");

  function handleSubmit(e) {
    e.preventDefault();
    onSearch(query);
  }

  return (
    <span className="flex justify-end">
      <span className="relative">
        <FaMagnifyingGlass className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" />
        <form onSubmit={handleSubmit}>
          <input
            type="search"
            id="search"
            placeholder="Search"
            className="border border-slate-200 rounded-xl pl-8 pr-2.5 mr-5 py-1 focus:border-fuchsia-400 focus:outline-none"
            onChange={(e) => setQuery(e.target.value)}
          />
        </form>
      </span>
    </span>
  );
}
