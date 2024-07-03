import {
    createBrowserRouter, Outlet, RouterProvider,
} from 'react-router-dom'
import React from 'react'
import Home from "./components/home/Home";
import Login from "./components/authentication/login/Login";
import Register from "./components/authentication/register/Register";
import SearchEvents from "./components/event/search/SearchEvents";
import {NavBar} from "./components/NavBar/NavBar";
import {PlanItProvider} from "./PlanItProvider";
import AccountDetails from "./components/accountDetails/AccountDetails";
import {RequireAuth} from "./AuthContainer";
import GetEvent from "./components/event/get/GetEvent";
import MyEvents from "./components/myEvents/MyEvents";
import {initializeApp} from "firebase/app";
import {getFirestore} from "firebase/firestore";
import {Calendar} from "./components/calendar/Calendar";
import NearMe from "./components/nearMe/NearMe";

const app = initializeApp({
    apiKey: "AIzaSyCz8gn12VwIXJzs8F7Y1ZFU8JlSNmI3AIM",
    authDomain: "planit-chat-4d37d.firebaseapp.com",
    projectId: "planit-chat-4d37d",
    storageBucket: "planit-chat-4d37d.appspot.com",
    messagingSenderId: "708606212293",
    appId: "1:708606212293:web:3555f12f048848cff358ac"
})

export const db = getFirestore(app);

const router = createBrowserRouter([
    {
        "path": "/",
        "element": (
            <NavbarWrapper />
        ),
        children: [
            {
                "path": "/",
                "element": <Home />,
            },
            {
                "path": "/planit/calendar",
                "element": <Calendar />,
            },
            {
                "path": "/planit/events",
                "element":(
                    <RequireAuth>
                        <SearchEvents />
                    </RequireAuth>
                )
            },
            {
                "path": "/planit/event/:id",
                "element":(
                    <RequireAuth>
                        <GetEvent />
                    </RequireAuth>
                )
            },
            {
                "path": "/planit/login",
                "element": <Login />,
            },
            {
                "path": "/planit/register",
                "element": <Register />,
            },
            {
                "path": "/planit/me",
                "element": (
                    <RequireAuth>
                        <AccountDetails />
                    </RequireAuth>
                )
            },
            {
                "path": "/planit/user/events",
                "element": (
                    <RequireAuth>
                        <MyEvents />
                    </RequireAuth>
                )
            },
            {
                "path": "/planit/nearme",
                "element":
                    <RequireAuth>
                        <NearMe />
                    </RequireAuth>
            }
        ]
    },
])

function NavbarWrapper(){
    return (
        <div>
            <NavBar />
            <Outlet/>
        </div>
    )
}

export function Router() {
    return (
        <>
            <PlanItProvider>
            <RouterProvider router={router} />
            </PlanItProvider>
        </>
    )
}