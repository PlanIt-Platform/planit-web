import {
    createBrowserRouter, RouterProvider,
} from 'react-router-dom'
import React from 'react'
import Home from "./pages/home/Home";
import Login from "./pages/authentication/Login";
import Register from "./pages/authentication/Register";
import UserProfile from "./pages/userProfile/UserProfile";

const router = createBrowserRouter([
    {
        "path": "/",
        "element": <Home />,
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
    }
])

export function Router() {
    return (
        <RouterProvider router={router} />
    )
}