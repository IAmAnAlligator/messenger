import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login as loginRequest, register } from "../../services/authService";
import { useAuth } from "../../contexts/AuthContext.tsx";
import AuthLayout from "../layouts/AuthLayout.tsx";
import AuthForm from "../components/auth/AuthForm.tsx";

export default function AuthPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const navigate = useNavigate();
    const { login } = useAuth();

    async function handleLogin() {
        try {
            const auth = await loginRequest({ username, password });

            await login(auth.accessToken);

            navigate("/chats", { replace: true });
        } catch {
            alert("Login error");
        }
    }

    async function handleRegister() {
        try {
            await register({ username, password });

            alert("Register success");
        } catch {
            alert("Register error");
        }
    }

    return (
        <AuthLayout>
            <AuthForm
                username={username}
                password={password}
                onUsernameChange={setUsername}
                onPasswordChange={setPassword}
                onLogin={handleLogin}
                onRegister={handleRegister}
            />
        </AuthLayout>
    );
}