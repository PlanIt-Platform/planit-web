import {
    createBrowserRouter, Outlet, RouterProvider,
} from 'react-router-dom'
import React from 'react'
import Home from "./components/home/Home";
import Login from "./components/authentication/Login";
import Register from "./components/authentication/Register";
import SearchEvents from "./components/searchEvents/SearchEvents";
import {NavBar} from "./components/NavBar/NavBar";
import {PlanItProvider} from "./PlanItProvider";
import AccountDetails from "./components/accountDetails/AccountDetails";
import {RequireAuth} from "./AuthContainer";
import GetEvent from "./components/getEvent/GetEvent";

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