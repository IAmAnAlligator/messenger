import { Navigate } from "react-router-dom";
import type { ReactElement } from "react";

export default function ProtectedRoute({
    children
}: {
    children: ReactElement;
}) {

    const token = localStorage.getItem("accessToken");

    if (!token) {
        return <Navigate to="/" replace />;
    }

    return children;
}