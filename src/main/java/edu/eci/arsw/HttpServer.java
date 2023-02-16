package edu.eci.arsw;

import edu.eci.arsw.repository.Cache;
import edu.eci.arsw.service.HttpConnection;
import edu.eci.arsw.webapps.webServices.normalService.RestService;
import edu.eci.arsw.webapps.webServices.sparkService.RestServiceSpark;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a mini web server, you can access it in localhost:35000
 */
public class HttpServer {

    private Map<String, RestService> services = new HashMap<>();

    private static HttpServer _instance = new HttpServer(); //se carga la clase

    private HttpServer() { }

    private static Map<String, RestServiceSpark> servicesSpark = new HashMap<>();

    public static String staticFilesLocation;

    public static HttpServer getInstance() { // implementacion singleton
        return _instance;
    }


    /**
     * @param args
     * @throws IOException
     */
public  void run(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while (running) {

            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean flagForPath = true;
            String path = "/?name=test";
            while ((inputLine = in.readLine()) != null) {
                if (flagForPath) {
                    flagForPath = false;
                    path = inputLine.split(" ")[1];
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            URL basePathForm = new URL("http://www.localhost:35000" + path); //http://www.localhost:35000/?t=indiana
            String baseQueryForm = basePathForm.getQuery();

            String apiResponse = "";
            if (baseQueryForm != null) {
                if (Cache.hasQuery(baseQueryForm)) {
                    apiResponse = Cache.getQuery(baseQueryForm);
                } else {
                    apiResponse = HttpConnection.getAPIInfo(baseQueryForm);
                    Cache.saveQuery(baseQueryForm,apiResponse );
                }

            }

            outputLine="";
            if (basePathForm.getPath().contains("/apps/")) { // localhost:35000/apps/hello
                outputLine = executeService(basePathForm.getPath().substring(5)); // /apps/hello toma solo hello
            } else if (basePathForm.getPath().contains("/spark/")) {
                outputLine = executeServiceSpark(basePathForm.getPath().substring(6));
            } else {
                outputLine = htmlWithForms(apiResponse);
            }


            out.println(outputLine);

            out.close();
            in.close();
            clientSocket.close();
        }

        serverSocket.close();
    }


    public String executeService(String serviceName) {
        RestService rs = services.get(serviceName);
        String header = rs.getHeader();
        String body = rs.getResponse(); //body viene leido de disco
        return header + body;
    }

    public String executeServiceSpark(String serviceName) {
        RestServiceSpark rs = servicesSpark.get(serviceName);
        String response = rs.getResponse("","");
        return response;
    }

    public void addService(String key, RestService service) {
    services.put(key, service);
    }

    public static void get(String key, RestServiceSpark service) {
        servicesSpark.put(key,service);
    }

    public static void staticFiles(String location) {
        staticFilesLocation = location;
    }



    public static String jsonSimple(String jsonResponse) {
        return  "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json \r\n" +
                "\r\n"
                +
                jsonResponse;
    }


    /**
     * @param apiResponse JSON string response of the API
     * @return a String representing the html response
     */
    public static String htmlWithForms(String apiResponse) {
        System.out.println("RESPOINSEEE :" + apiResponse);

        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html" +
                "\r\n"
                +
                "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\"\n" +
                "          content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
                "    <title>Movie´s information</title>\n" +
                "</head>\n" +
                "<body style=\"margin:0;padding: 0;\">\n" +
                "\n" +
                "<main class=\"main\" style=\"background: rgb(179,153,212);background: linear-gradient(90deg, rgba(179,153,212,1) 0%, rgba(88,166,187,1) 47%, rgba(71,214,244,1) 100%);\">\n" +
                "\n" +
                "    <form action=\"localhost:35000\" onsubmit=\"return false\" class=\"form\" style=\"  display: flex;justify-content: center;align-items: center;background-color:rgba(255, 255, 255, 0.6);border-radius: 15px;width: 35%;height: 20%;margin-top: 10px; \">\n" +
                "        <div style=\"  \" >\n" +
                "            <p>Movie Name</p>\n" +
                "            <div>\n" +
                "            <label for=\"t\"></label>\n" +
                "                <input type=\"text\" name=\"t\" id=\"t\">\n" +
                "            </div>\n" +
                "            <input type=\"button\" value=\"Get information\" onclick=\"loadGetMsg()\">\n" +
                "        </div>\n" +
                "    </form>\n" +
                "    <div id=\"getrespmsg\" style=\" \" >\n" + apiResponse +
                "\n" +
                "    </div>\n" +
                "</main>" +
                "\n" +
                "<script>\n" +
                "    function loadGetMsg() {\n" +
                "        let nameVar = document.getElementById(\"t\").value;\n" +
                "        const xhttp = new XMLHttpRequest();\n"  +
                "        xhttp.onload = function() {\n" +
                "            document.getElementById(\"getrespmsg\").innerHTML= \n"  +
                "                this.responseText;\n" +
                "        }\n" +
                "        xhttp.open(\"GET\", \"/?t=\"+nameVar);\n" +
                "        xhttp.send();\n" +
                "    }\n" +
                "</script>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

    }


}