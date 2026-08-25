import { useState, useEffect } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { setToken, clearToken, getToken } from "./api/apiCore.js";
import { getMe } from "./api/apiAuth.js";
import ProtectedRoute from "./ui/auth/ProtectedRoute.jsx";
import PublicOnlyRoute from "./ui/auth/PublicOnlyRoute.jsx";
import LandingPage from "./pages/LandingPage.jsx";
import BoardPage from "./pages/BoardPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import SignupPage from "./pages/SignupPage.jsx";
import SettingsPage from "./pages/SettingsPage.jsx";

function loadUser() {
  const token = getToken();
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    if (payload.exp && payload.exp < Date.now() / 1000) {
      clearToken();
      return null;
    }
    return {
      id: payload.id,
      name: payload.name,
      email: payload.sub,
      avatarUrl: null,
    };
  } catch {
    clearToken();
    return null;
  }
}

function App() {
  const [user, setUser] = useState(() => loadUser());
  const isLoggedIn = user !== null;

  // Session restored from the JWT is instant but carries no presigned
  // image URLs (they expire), so fetch fresh ones for the current user.
  useEffect(() => {
    if (!user) return;
    let cancelled = false;
    getMe()
      .then((data) => {
        if (!cancelled) {
          setUser((prev) =>
            prev
              ? {
                  ...prev,
                  id: data.id,
                  name: data.name,
                  email: data.email,
                  avatarUrl: data.avatarUrl ?? null,
                }
              : prev,
          );
        }
      })
      .catch(console.error);
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleAuth(data) {
    setToken(data.token);
    setUser({
      id: data.id,
      name: data.name,
      email: data.email,
      avatarUrl: data.avatarUrl ?? null,
    });
  }

  function handleLogout() {
    clearToken();
    setUser(null);
  }

  function handleUserUpdate(updated) {
    setUser((prev) => ({ ...prev, ...updated }));
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={<LandingPage isLoggedIn={isLoggedIn} user={user} onLogout={handleLogout} />}
        />
        <Route
          path="/home"
          element={
            <ProtectedRoute isLoggedIn={isLoggedIn}>
              <BoardPage isLoggedIn={isLoggedIn} user={user} onLogout={handleLogout} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <ProtectedRoute isLoggedIn={isLoggedIn}>
              <SettingsPage isLoggedIn={isLoggedIn} user={user} onLogout={handleLogout} onUserUpdate={handleUserUpdate} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/login"
          element={
            <PublicOnlyRoute isLoggedIn={isLoggedIn}>
              <LoginPage isLoggedIn={isLoggedIn} user={user} onLogin={handleAuth} />
            </PublicOnlyRoute>
          }
        />
        <Route
          path="/signup"
          element={
            <PublicOnlyRoute isLoggedIn={isLoggedIn}>
              <SignupPage isLoggedIn={isLoggedIn} user={user} onSignup={handleAuth} />
            </PublicOnlyRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
