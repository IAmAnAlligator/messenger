import { useState } from "react";

type Props = {
    username: string;
    email: string;
    password: string;

    onUsernameChange: (value: string) => void;
    onEmailChange: (value: string) => void;
    onPasswordChange: (value: string) => void;

    onLogin: () => void;
    onRegister: () => void;
};

export default function AuthForm({
    username,
    email,
    password,
    onUsernameChange,
    onEmailChange,
    onPasswordChange,
    onLogin,
    onRegister,
}: Props) {
    const [isRegister, setIsRegister] = useState(false);

    function handleModeChange(registerMode: boolean) {
        setIsRegister(registerMode);
    }

    return (
        <>
            <h2>
                {isRegister ? "Register" : "Login"}
            </h2>

            {isRegister && (
                <input
                    placeholder="Username"
                    value={username}
                    onChange={(e) => onUsernameChange(e.target.value)}
                />
            )}

            <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => onEmailChange(e.target.value)}
            />

            <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => onPasswordChange(e.target.value)}
            />

            {isRegister ? (
                <button onClick={onRegister}>
                    Register
                </button>
            ) : (
                <button onClick={onLogin}>
                    Login
                </button>
            )}

            <button
                type="button"
                onClick={() => handleModeChange(!isRegister)}
            >
                {isRegister
                    ? "Already have an account? Login"
                    : "Don't have an account? Register"}
            </button>
        </>
    );
}