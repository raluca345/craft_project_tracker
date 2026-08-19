import { useState } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { setToken, clearToken, getToken } from "./api/apiCore.js";
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
      avatarKey: payload.avatarKey ?? null,
    };
  } catch {
    clearToken();
    return null;
  }
}

function App() {
  const [user, setUser] = useState(() => loadUser());
  const isLoggedIn = user !== null;

  function handleAuth(data) {
    setToken(data.token);
    setUser({
      id: data.id,
      name: data.name,
      email: data.email,
      avatarKey: data.avatarKey ?? null,
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
            isLoggedIn ? (
              <BoardPage isLoggedIn={isLoggedIn} user={user} onLogout={handleLogout} />
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
        <Route
          path="/settings"
          element={
            isLoggedIn ? (
              <SettingsPage isLoggedIn={isLoggedIn} user={user} onLogout={handleLogout} onUserUpdate={handleUserUpdate} />
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
        <Route
          path="/login"
          element={
            isLoggedIn ? (
              <Navigate to="/home" replace />
            ) : (
              <LoginPage isLoggedIn={isLoggedIn} user={user} onLogin={handleAuth} />
            )
          }
        />
        <Route
          path="/signup"
          element={
            isLoggedIn ? (
              <Navigate to="/home" replace />
            ) : (
              <SignupPage isLoggedIn={isLoggedIn} user={user} onSignup={handleAuth} />
            )
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
