import React, {useContext} from "react";
import {PlanItContext} from "../../PlanItProvider";

export function isLogged(): boolean {
    let userId = Number(localStorage.getItem('user_id'));
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
