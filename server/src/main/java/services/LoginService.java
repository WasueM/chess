package services;
import java.util.UUID;

public class LoginService {
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
