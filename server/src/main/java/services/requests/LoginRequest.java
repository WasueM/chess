package services.requests;

public record LoginRequest(
        String username,
        String password){
}