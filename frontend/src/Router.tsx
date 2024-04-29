import {
    createBrowserRouter, Outlet, RouterProvider,
} from 'react-router-dom'
import React from 'react'
import Home from "./pages/home/Home";
import Login from "./pages/authentication/Login";
import Register from "./pages/authentication/Register";
import UserProfile from "./pages/userProfile/UserProfile";
import SearchEvents from "./pages/searchEvents/SearchEvents";
import {NavBar} from "./components/NavBar";
import {PlanItProvider} from "./PlanItProvider";
import AccountDetails from "./pages/accountDetails/AccountDetails";

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
                "element": <SearchEvents />
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
                "path": "/planit/user/:id",
                "element": <UserProfile />,
            },
            {
                "path": "/planit/me",
                "element": <AccountDetails />,
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