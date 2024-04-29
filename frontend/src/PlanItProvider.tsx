import React, { createContext, useState } from 'react';

type ContextType = {
    userToken: string | undefined;
    setUserToken: (v: string | undefined) => void;
    userId: number | undefined;
    setUserId: (v: number | undefined) => void;
    eventsSearched: any[];
    setEventsSearched: (events: any[]) => void
}

export const PlanItContext = createContext<ContextType>({
    userToken: undefined,
    setUserToken: () => {},
    userId: undefined,
    setUserId: () => {},
    eventsSearched: undefined,
    setEventsSearched: () => {}
});

export function PlanItProvider({ children }) {
    const [userToken, setUserToken] = useState<string | undefined>(localStorage.getItem('access_token'));
    const [userId, setUserId] = useState<number | undefined>(Number(localStorage.getItem('user_id')));
    const[eventsSearched, setEventsSearched] = useState([]);

    return (
        <PlanItContext.Provider value={{ userToken, setUserToken, userId, setUserId, eventsSearched, setEventsSearched }}>
    {children}
    </PlanItContext.Provider>
);
}
