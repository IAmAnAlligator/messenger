type Props = {
    username: string;
    password: string;
    onUsernameChange: (value: string) => void;
    onPasswordChange: (value: string) => void;
    onLogin: () => void;
    onRegister: () => void;
};

export default function AuthForm({
    username,
    password,
    onUsernameChange,
    onPasswordChange,
    onLogin,
    onRegister
}: Props) {
    return (
        <>
            <h2>Auth</h2>

            <input
                placeholder="Username"
                value={username}
                onChange={(e) => onUsernameChange(e.target.value)}
            />

            <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => onPasswordChange(e.target.value)}
            />

            <button onClick={onLogin}>
                Login
            </button>

            <button onClick={onRegister}>
                Register
            </button>
        </>
    );
}