import React, {useContext} from "react";
import {Navigate} from "react-router-dom";
import {PlanItContext} from "../../PlanItProvider";

export function isLogged(userId: number | undefined): boolean {
    return userId !== undefined && userId !== null && userId > 0;
}

export function clearSession(setUserId: (v: number | undefined) => void): void {
    localStorage.removeItem('user_id');
    setUserId(undefined);
}

export function setSession(userId: string, setUserId: (v: number | undefined) => void): void {
    localStorage.setItem('user_id', userId);
    setUserId(Number(userId));
}

export function getUserId(): number | undefined {
    const { userId } = useContext(PlanItContext);
    return userId;
}
