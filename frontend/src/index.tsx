import {createRoot} from "react-dom/client";
import React from "react";
import {Router} from "./Router";

const root = createRoot(document.getElementById('container'));
root.render(<Router/>);
