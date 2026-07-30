import type { ReactNode } from "react";

type Props = {
    children: ReactNode;
};

export default function AuthLayout({ children }: Props) {
    return (
        <div className="auth-layout">
            <div className="auth-card">
                {children}
            </div>
        </div>
    );
}