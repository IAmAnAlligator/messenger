import {
    createContext,
    useContext,
    useEffect,
    useRef,
    useState,
} from "react";

import { api } from "../api/client";
import {
    connectSocket,
    disconnectSocket,
} from "../services/chatSocket";

type User = {
    id: number;
    username: string;
};

type CurrentUserResponse = {
    id: number;
    username: string;
    role: string;
};

type AuthContextType = {
    user: User | null;
    loading: boolean;
    login: (access: string) => Promise<void>;
    logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({
    children,
}: {
    children: React.ReactNode;
}) {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    const initialized = useRef(false);

    async function fetchMe() {
        try {
            const token = localStorage.getItem("accessToken");

            if (!token) {
                setUser(null);
                return;
            }

            const res = await api.get<CurrentUserResponse>("/users/me");

            setUser({
                id: res.data.id,
                username: res.data.username,
            });

            disconnectSocket();
            connectSocket(token);

        } catch {
            setUser(null);
            localStorage.removeItem("accessToken");

            disconnectSocket();

        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (initialized.current) {
            return;
        }

        initialized.current = true;
        fetchMe();
    }, []);

    async function login(access: string) {
        localStorage.setItem("accessToken", access);
        setLoading(true);

        await fetchMe();
    }

    async function logout() {
        disconnectSocket();
        localStorage.removeItem("accessToken");

        setUser(null);
        setLoading(false);

        try {
            await api.post("/auth/logout");
        } catch {
            // ignore
        }
    }

    return (
        <AuthContext.Provider
            value={{
                user,
                loading,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const ctx = useContext(AuthContext);

    if (!ctx) {
        throw new Error("useAuth must be used inside AuthProvider");
    }

    return ctx;
}