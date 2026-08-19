import { useRef, useState } from "react";
import { uploadAvatar } from "../api/apiAuth";
import { getErrorMessage } from "../commons/errors";
import Avatar from "../ui/auth/Avatar.jsx";
import AppHeader from "../ui/layout/AppHeader.jsx";
import Footer from "../ui/layout/Footer.jsx";
import ErrorBox from "../ui/feedback/ErrorBox.jsx";

export default function SettingsPage({
  isLoggedIn,
  user,
  onLogout,
  onUserUpdate,
}) {
  const [error, setError] = useState(null);
  const [uploading, setUploading] = useState(false);
  const fileRef = useRef(null);

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
      onUserUpdate({ avatarKey: data.avatarKey });
    } catch (err) {
      console.error(err);
      setError(getErrorMessage(err));
    } finally {
      setUploading(false);
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
            <dl className="space-y-3 rounded-lg border border-slate-200 bg-white p-4 text-sm">
              <div className="flex justify-between">
                <dt className="text-slate-500">Name</dt>
                <dd className="font-medium text-slate-800">{user.name}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-slate-500">Email</dt>
                <dd className="font-medium text-slate-800">{user.email}</dd>
              </div>
            </dl>
          </section>
        </div>
      </main>
      <Footer />
    </div>
  );
}
