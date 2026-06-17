import { useState } from "react";
import { login, register } from "../services/authService";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function AuthPage() {

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();
    const { login: setAuth } = useAuth();

    async function handleLogin() {

            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");

        try {
            const auth = await login({
                username,
                password
            });

            setAuth(auth.accessToken, auth.refreshToken);

            localStorage.setItem("accessToken", auth.accessToken);
            localStorage.setItem("refreshToken", auth.refreshToken);

            navigate("/chats");

            alert("Login success");
        } catch (e) {
            alert("Login error");
        }
    }

    async function handleRegister() {

            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");

        try {
            await register({
                username,
                password
            });

            alert("Register success (now you can login)");
        } catch (e) {
            alert("Register error");
        }
    }

    return (
        <div style={{ maxWidth: 300, margin: "50px auto" }}>

            <h2>Auth</h2>

            <input
                placeholder="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                style={{ display: "block", width: "100%", marginBottom: 10 }}
            />

            <input
                type="password"
                placeholder="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ display: "block", width: "100%", marginBottom: 10 }}
            />

            <button
                onClick={handleLogin}
                style={{ width: "100%", marginBottom: 10 }}
            >
                Login
            </button>

            <button
                onClick={handleRegister}
                style={{ width: "100%" }}
            >
                Register
            </button>

        </div>
    );
}