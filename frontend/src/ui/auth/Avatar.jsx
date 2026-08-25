const AVATAR_COLORS = [
  "bg-rose-300",
  "bg-orange-300",
  "bg-teal-300",
  "bg-sky-300",
  "bg-fuchsia-300",
];

function avatarColor(userId) {
  const hash = [...String(userId)].reduce(
    (total, char) => total + char.charCodeAt(0),
    0,
  );
  return AVATAR_COLORS[hash % AVATAR_COLORS.length];
}

function getInitials(name) {
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}

export default function Avatar({ user, size = "w-10 h-10" }) {
  if (user.avatarUrl) {
    return (
      <img
        src={user.avatarUrl}
        alt={user.name}
        className={`${size} rounded-full object-cover`}
        title={user.name}
      />
    );
  }

  return (
    <div
      className={`${size} ${avatarColor(user.id)} flex items-center justify-center rounded-full font-bold text-white`}
      title={user.name}
    >
      {getInitials(user.name)}
    </div>
  );
}
