import React from "react";
import {Navigate} from "react-router-dom";

export function getSessionToken(): string | null {
    return localStorage.getItem('access_token');
}

export function isLogged(): boolean {
    const token = localStorage.getItem('access_token');
    return token !== null;
}

export function clearSession(): void {
    localStorage.removeItem('access_token');
}

export function setSession(token: string): void {
    localStorage.setItem('access_token', token);
}

export function AuthRequired({ children }: { children: React.ReactElement }) {
    if (isLogged()) {
        return children;
    }
    return <Navigate to="/planit/login" replace={true} />;
}