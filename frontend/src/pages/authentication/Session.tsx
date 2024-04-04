import React from "react";
import {Navigate} from "react-router-dom";

export function getSessionToken(): string | null {
    return localStorage.getItem('access_token');
}

export function isLogged(): boolean {
    const token = getSessionToken();
    return token !== null && token !== undefined && token !== 'undefined';
}

export function clearSession(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('user_id');
}

export function setSession(token: string, userId: string): void {
    localStorage.setItem('access_token', token);
    localStorage.setItem('user_id', userId);
}

export function getUserId(): string | null {
    return localStorage.getItem('user_id');
}

export function AuthRequired({ children }: { children: () => React.ReactNode }) {
    return isLogged() ? <div>{children()}</div> : <Navigate to="/planit/login" replace={true} />;
}