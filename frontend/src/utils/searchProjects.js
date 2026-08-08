export function parseQuery(query) {
  const tokens = query.trim().split(/\s+/).filter(Boolean);
  const tagNames = [];
  const nameTokens = [];
  for (const token of tokens) {
    if (token.startsWith("#") && token.length > 1) {
      tagNames.push(token.slice(1).toLowerCase());
    } else {
      nameTokens.push(token);
    }
  }
  return { nameQuery: nameTokens.join(" ").toLowerCase(), tagNames };
}

function projectHasTag(project, searchedTag) {
  return project.tags.some((tag) => tag.toLowerCase() === searchedTag);
}

export function filterProjects(projects, query) {
  const { nameQuery, tagNames } = parseQuery(query);
  if (!nameQuery && tagNames.length === 0) return projects;

  return projects.filter((project) => {
    const nameMatches =
      !nameQuery || project.patternName.toLowerCase().includes(nameQuery);
    const allTagsPresent = tagNames.every((searchedTag) =>
      projectHasTag(project, searchedTag),
    );
    return nameMatches && allTagsPresent;
  });
}
