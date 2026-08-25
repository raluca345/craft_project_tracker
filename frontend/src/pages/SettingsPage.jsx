import { useRef, useState } from "react";
import { uploadAvatar, rename, changeEmail } from "../api/apiAuth";
import { setToken } from "../api/apiCore";
import { getErrorMessage } from "../commons/errors";
import Avatar from "../ui/auth/Avatar.jsx";
import AppHeader from "../ui/layout/AppHeader.jsx";
import Footer from "../ui/layout/Footer.jsx";
import ErrorBox from "../ui/feedback/ErrorBox.jsx";

const INPUT_STYLE =
  "rounded-lg border border-slate-300 px-3 py-1.5 text-sm focus:border-fuchsia-400 focus:outline-none";

export default function SettingsPage({
  isLoggedIn,
  user,
  onLogout,
  onUserUpdate,
}) {
  const [error, setError] = useState(null);
  const [uploading, setUploading] = useState(false);
  const fileRef = useRef(null);

  const [editingName, setEditingName] = useState(false);
  const [nameValue, setNameValue] = useState(user.name);
  const [savingName, setSavingName] = useState(false);

  const [editingEmail, setEditingEmail] = useState(false);
  const [emailValue, setEmailValue] = useState(user.email);
  const [savingEmail, setSavingEmail] = useState(false);

  function handlePickFile() {
    fileRef.current?.click();
  }

  async function handleFileChange(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    e.target.value = "";

    setError(null);
    setUploading(true);
    try {
      const data = await uploadAvatar(file);
      onUserUpdate({ avatarKey: data.avatarKey, avatarUrl: data.avatarUrl });
    } catch (err) {
      console.error(err);
      setError(getErrorMessage(err));
    } finally {
      setUploading(false);
    }
  }

  async function handleSaveName() {
    const trimmed = nameValue.trim();
    if (!trimmed || trimmed === user.name) {
      setEditingName(false);
      setNameValue(user.name);
      return;
    }
    setSavingName(true);
    setError(null);
    try {
      const data = await rename(trimmed);
      onUserUpdate({ name: data.name });
      setEditingName(false);
    } catch (err) {
      console.error(err);
      setError(getErrorMessage(err));
    } finally {
      setSavingName(false);
    }
  }

  async function handleSaveEmail() {
    const trimmed = emailValue.trim();
    if (!trimmed || trimmed === user.email) {
      setEditingEmail(false);
      setEmailValue(user.email);
      return;
    }
    setSavingEmail(true);
    setError(null);
    try {
      const data = await changeEmail(trimmed);
      setToken(data.token);
      onUserUpdate({ email: data.email, token: data.token });
      setEditingEmail(false);
    } catch (err) {
      console.error(err);
      setError(getErrorMessage(err));
    } finally {
      setSavingEmail(false);
    }
  }

  return (
    <div className="min-h-screen flex flex-col">
      <AppHeader isLoggedIn={isLoggedIn} user={user} onLogout={onLogout} />
      <main className="flex-1 px-6">
        <div className="mx-auto max-w-lg py-12">
          <h1 className="mb-8 text-2xl font-bold text-slate-800">Settings</h1>
          <ErrorBox message={error} onDismiss={() => setError(null)} />

          <section className="mb-10">
            <div className="flex items-center gap-6">
              <button
                type="button"
                onClick={handlePickFile}
                className="group relative cursor-pointer"
                disabled={uploading}
              >
                <Avatar user={user} size="h-20 w-20" />
                <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 text-xs font-medium text-white opacity-0 transition-opacity group-hover:opacity-100">
                  {uploading ? "..." : "Change"}
                </div>
              </button>
              <input
                ref={fileRef}
                type="file"
                accept="image/png,image/jpeg,image/webp"
                className="hidden"
                onChange={handleFileChange}
              />
              <div>
                <p className="text-xs text-slate-400">
                  PNG, JPEG or WebP. Max 10 MB.
                </p>
              </div>
            </div>
          </section>

          <section>
            <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
              Account
            </h2>
            <div className="rounded-lg border border-slate-200 bg-white p-4 text-sm">
              <div className="flex items-center justify-between gap-4 py-2">
                <span className="w-20 shrink-0 text-slate-400">Name</span>
                {editingName ? (
                  <div className="flex flex-1 items-center gap-2">
                    <input
                      type="text"
                      value={nameValue}
                      onChange={(e) => setNameValue(e.target.value)}
                      className={INPUT_STYLE}
                      autoFocus
                      onKeyDown={(e) => {
                        if (e.key === "Enter") handleSaveName();
                        if (e.key === "Escape") {
                          setEditingName(false);
                          setNameValue(user.name);
                        }
                      }}
                    />
                    <button
                      type="button"
                      onClick={handleSaveName}
                      disabled={savingName}
                      className="rounded-md bg-fuchsia-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-fuchsia-600 disabled:opacity-50"
                    >
                      Save
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setEditingName(false);
                        setNameValue(user.name);
                      }}
                      className="rounded-md px-3 py-1.5 text-xs text-slate-500 hover:bg-slate-100"
                    >
                      Cancel
                    </button>
                  </div>
                ) : (
                  <div className="flex flex-1 items-center justify-between">
                    <span className="font-medium text-slate-800">
                      {user.name}
                    </span>
                    <button
                      type="button"
                      onClick={() => setEditingName(true)}
                      className="text-xs text-slate-400 hover:text-fuchsia-500"
                    >
                      Edit
                    </button>
                  </div>
                )}
              </div>

              <div className="border-t border-slate-100" />

              <div className="flex items-center justify-between gap-4 py-2">
                <span className="w-20 shrink-0 text-slate-400">Email</span>
                {editingEmail ? (
                  <div className="flex flex-1 items-center gap-2">
                    <input
                      type="email"
                      value={emailValue}
                      onChange={(e) => setEmailValue(e.target.value)}
                      className={INPUT_STYLE}
                      autoFocus
                      onKeyDown={(e) => {
                        if (e.key === "Enter") handleSaveEmail();
                        if (e.key === "Escape") {
                          setEditingEmail(false);
                          setEmailValue(user.email);
                        }
                      }}
                    />
                    <button
                      type="button"
                      onClick={handleSaveEmail}
                      disabled={savingEmail}
                      className="rounded-md bg-fuchsia-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-fuchsia-600 disabled:opacity-50"
                    >
                      Save
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setEditingEmail(false);
                        setEmailValue(user.email);
                      }}
                      className="rounded-md px-3 py-1.5 text-xs text-slate-500 hover:bg-slate-100"
                    >
                      Cancel
                    </button>
                  </div>
                ) : (
                  <div className="flex flex-1 items-center justify-between">
                    <span className="font-medium text-slate-800">
                      {user.email}
                    </span>
                    <button
                      type="button"
                      onClick={() => setEditingEmail(true)}
                      className="text-xs text-slate-400 hover:text-fuchsia-500"
                    >
                      Edit
                    </button>
                  </div>
                )}
              </div>
            </div>
          </section>
        </div>
      </main>
      <Footer />
    </div>
  );
}
