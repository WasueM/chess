package services;

record LoginResult(String username, String authToken) {}

record RegisterResult(String username, String authToken) { }

record LogoutResult(String username) {}