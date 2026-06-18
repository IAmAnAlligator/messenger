import { useState } from "react";
import { login as loginRequest, register } from "../services/authService";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

/**
 * 🔐 Страница авторизации
 *
 * RESPONSIBILITY:
 * - собрать username/password
 * - вызвать API
 * - передать accessToken в AuthContext
 * - перейти в защищённую зону
 *
 * ❌ НЕ должна:
 * - напрямую управлять localStorage
 * - дублировать auth state
 */
export default function AuthPage() {

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const navigate = useNavigate();

    /**
     * 🔐 login из AuthContext (НЕ authService)
     * это важно — единая точка управления auth
     */
    const { login: setAuth } = useAuth();

    // =========================
    // 🔑 LOGIN
    // =========================
    async function handleLogin() {
        try {
            /**
             * 🌐 запрос на backend
             */
            const auth = await loginRequest({
                username,
                password
            });

            /**
             * 🔐 сохраняем токен через AuthContext
             * (внутри он вызовет fetchMe и обновит user)
             */
            await setAuth(auth.accessToken);

            /**
             * 🚀 переход в защищённую часть
             */
            navigate("/chats", { replace: true });

            alert("Login success");

        } catch (e) {
            alert("Login error");
        }
    }

    // =========================
    // 📝 REGISTER
    // =========================
    async function handleRegister() {
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

            {/* 👤 username */}
            <input
                placeholder="username"
                value={username}
                onChange={(e) =>
                    setUsername(e.target.value)
                }
                style={{
                    display: "block",
                    width: "100%",
                    marginBottom: 10
                }}
            />

            {/* 🔒 password */}
            <input
                type="password"
                placeholder="password"
                value={password}
                onChange={(e) =>
                    setPassword(e.target.value)
                }
                style={{
                    display: "block",
                    width: "100%",
                    marginBottom: 10
                }}
            />

            {/* 🔑 login */}
            <button
                onClick={handleLogin}
                style={{
                    width: "100%",
                    marginBottom: 10
                }}
            >
                Login
            </button>

            {/* 📝 register */}
            <button
                onClick={handleRegister}
                style={{
                    width: "100%"
                }}
            >
                Register
            </button>

        </div>
    );
}