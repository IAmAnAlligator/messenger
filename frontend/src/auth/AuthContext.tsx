import {
    createContext,
    useContext,
    useEffect,
    useState
} from "react";

import { api } from "../api/client";

type User = {
    username: string;
} | null;

type AuthContextType = {
    user: User;
    login: (access: string, refresh: string) => void;
    logout: () => void;
};

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {

    const [user, setUser] = useState<User>(null);

    // 👉 восстановление при reload
    useEffect(() => {
        const token = localStorage.getItem("accessToken");

        if (token) {
            setUser({ username: "user" }); // можно позже декодировать JWT
        }
    }, []);

    function login(access: string, refresh: string) {

        localStorage.setItem("accessToken", access);
        localStorage.setItem("refreshToken", refresh);

        setUser({ username: "user" });
    }

    function logout() {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        setUser(null);
    }

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
}