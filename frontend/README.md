# Gomoku Game Frontend Application

## Overview

This document provides an overview of the internal software organization of the frontend application for a Gomoku game. Gomoku is a two-player strategy board game also known as Five in a Row.

## Table of Contents

1. [Project Structure](#project-structure)
2. [Components](#components)
3. [State Management](#state-management)
4. [User Interface](#user-interface)
5. [Game Logic](#game-logic)
6. [Communication with Backend](#communication-with-backend)

## 1. Project Structure

The project follows a modular structure to enhance maintainability and scalability. The main directories include:

- `src`: Contains the source code of the application.
    - `authentication`: Contains the React components and pages used in the application to authorize the user;
    - `components`: Contains the React components and pages used in the application;
    - `services`: Contains the services used in the application. This layer is responsible for the communication with the API;
        - `custom`: Contains custom-made utility functions to make requests to the api.
    - `style`: Contains the css code to style the various components.

## 2. Components

### 2.1 About

- Responsible for rendering the makers of the API.

### 2.2 Box

- Responsible for rendering the various squares of the game board.

### 2.3 Game

- Manages the game state (current player, board configuration, etc.).
- Listens for cell click events and updates the game state accordingly. 
- Communicates with the backend for moves and game status.

### 2.4 Game Configuration

- Manages the initial configuration of the game, made by the user.

### 2.5 Games

- Responsible for rendering the various available games.

### 2.6 Home

- Responsible for rendering the home page.

### 2.7 Leaderboard

- Responsible for rendering the leaderboard page.
- Communicates with the backend for the current leaders in score starting from an index and returning the number of users specified by the user.

### 2.8 Matchmake

- Manages the matchmaking of two users according to the chosen game configuration .
- Communicates with the backend for the lookout of available lobbies, according to the configuration.

### 2.9 Play

- Manages the game state (current player, board configuration, etc.).
- Listens for cell click events and updates the game state accordingly.
- Communicates with the backend for moves and game status.

### 2.10 Box

- Responsible for rendering the details of a user.

## 3. User Interface

The user interface is designed to be intuitive and responsive.

### 3.1 Styling

- Styled using CSS for modularity and maintainability.

## 4. Authentication and Session Management

The user authentication is done in the `Login` or `Register` pages.

The `Session` interface was implemented to store the session information in the application.
The user session is stored in the browser's local storage.

---

This document provides an overview of the frontend application's internal organization for the Gomoku game.
