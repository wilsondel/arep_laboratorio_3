package edu.eci.arsw.webapps;

import edu.eci.arsw.HttpServer;
import static edu.eci.arsw.HttpServer.*;
import edu.eci.arsw.webapps.webServices.normalService.CSSService;
import edu.eci.arsw.webapps.webServices.normalService.HTMLService;
import edu.eci.arsw.webapps.webServices.normalService.ImageService;
import edu.eci.arsw.webapps.webServices.normalService.JSService;
import edu.eci.arsw.webapps.webServices.sparkService.HTMLSparkService;
import edu.eci.arsw.webapps.webServices.sparkService.HTMLSparkTestService;

import java.io.IOException;

public class FirstApp {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.getInstance();
        // Implementación agregando servicios
        server.addService("/hello",new HTMLService());
        server.addService("/style",new CSSService());
        server.addService("/javascript",new JSService());
        server.addService("/image",new ImageService());

        // Implementación usando funciones lambda similar a Spark
        staticFiles("/public");
        get("/spark", (request,response) -> new HTMLSparkService().getResponse(request, response) );
        get("/mytest", (request,response) -> new HTMLSparkTestService().getResponse(request, response) );
        server.run(args);
    }

}
