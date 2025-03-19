package client;

import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        try {
            facade = new ServerFacade("http://localhost:" + port + "/");
        } catch (Exception error) {
            System.out.println("Couldn't connect to the server at \"http://localhost:" + port + "/\"");
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void register() throws Exception {
        var authData = facade.register("testUser", "1234", "testEmail");
        assertTrue(authData.length() > 10);
    }


    @Test
    public void sampleTest() {
        assertTrue(true);
    }

}
