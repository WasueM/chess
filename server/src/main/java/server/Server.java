package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        //Spark.staticFiles.externalLocation("src/main/resources/web");

        // Register your endpoints and handle exceptions here.
        createEndpoints();

        //This line initializes the server and can be removed once you have a functioning endpoint 
        //Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void createEndpoints() {
        Spark.delete("/db", (request, response) -> {
            return "HOWDY DAWGS";
        });
        Spark.post("/user", (request, response) -> {
            return "HOWDY DAWGS";
        });
        Spark.post("/session", (request, response) -> {
            return "HOWDY DAWGS";
        });
        Spark.delete("/session", (request, response) -> {
            return "HOWDY DAWGS";
        });
        Spark.get("/game", (request, response) -> {
            return "HOWDY DAWGS";
        });
        Spark.post("/game", (request, response) -> {
            return "HOWDY DAWGS";
        });
        Spark.put("/game", (request, response) -> {
            return "HOWDY DAWGS";
        });
    }
}

//public class MyServer {
//    public static void main(String[] args) {
//        try {
//            createRoutes();
//        } catch(ArrayIndexOutOfBoundsException | NumberFormatException ex) {
//            System.err.println("Specify the port number as a command line paramter");
//        }
//    }
//    private void createRoutes() {
//        Spark.get("/hi", (request, response) -> {
//            return "HOWDY DAWGS";
//            // request.params(":name");
//        });
//    }
//}
