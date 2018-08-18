/*
 * The MIT License
 *
 * Copyright 2016 kbongort.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lightsim;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kbongort
 */
public class Server implements HttpHandler, ExecListener {

    HttpServer httpServer;
    LeanExec lightExec;
    ArrayList<LightController> controllers;

    public Server(LeanExec lightExec, ArrayList<LightController> controllers) {
        this.lightExec = lightExec;
        this.controllers = controllers;
        lightExec.addListener(this);
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
            httpServer.createContext("/control", this);
            httpServer.setExecutor(null);  // creates a default executor
        } catch (IOException e) {
            Console.log("Failed to start HTTP server. :( " + e);
        }
    }

    public void start() {
        httpServer.start();
    }

    @Override public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String[] pathComponents = requestPath.split("/");
        String lastPathComponent = pathComponents[pathComponents.length - 1];

        switch (lastPathComponent) {
            case "start":
                lightExec.start();
                break;
            case "stop":
                lightExec.stop();
                break;
            case "pause":
                lightExec.pause();
                break;
            case "schedule":
                lightExec.setIsScheduled(true);
                break;
            case "unschedule":
                lightExec.setIsScheduled(false);
                break;
            case "status":
                // Don't do anything; just respond with the normal status data.
                break;
            case "program":
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                String programName = query.get("name");
                if (null != programName) {
                    for (LightController controller : controllers) {
                        if (controller.name().equals(programName)) {
                            lightExec.setController(controller);
                            break;
                        }
                    }
                }
                break;
        }

        Headers requestHeaders = exchange.getRequestHeaders();
        String origin = requestHeaders.get("Origin").get(0);
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add("Access-Control-Allow-Origin", origin);

        String response = getStatusJson();
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    String getStatusJson() {
        boolean isRunning = lightExec.isRunning();
        boolean isPaused = lightExec.isPaused();
        boolean isScheduled = lightExec.isScheduled();
        Clock.Event nextSunEvent = lightExec.nextSunEvent();
        String currentProgram = lightExec.getControllerName();
        Date currentTime = new Date();

        String statusJson = "{" +
            "\"running\":" + (isRunning ? "true" : "false") +
            ",\"paused\":" + (isPaused ? "true" : "false") +
            ",\"scheduled\":" + (isScheduled ? "true" : "false") +
            ",\"currentProgram\":\"" + currentProgram + "\"" +
            ",\"currentTime\":\"" + currentTime.toString() + "\"" +
            ",\"controllers\":" + getControllersJson();
        if (nextSunEvent != null) {
            statusJson +=
                ",\"nextEventType\": \"" + nextSunEvent.type.toString() + "\"" +
                ",\"nextEventTime\": \"" + nextSunEvent.date.toString() + "\"";
        }
        statusJson += "}";
        return statusJson;
    }

    String getControllersJson() {
        String controllersJson = "[";
        int numControllers = controllers.size();
        for (int i = 0; i < numControllers; i++) {
            LightController controller = controllers.get(i);
            if (i > 0) {
                controllersJson += ",";
            }
            controllersJson += "\"" + controller.name() + "\"";
        }
        controllersJson += "]";
        return controllersJson;
    }

    Map<String, String> parseQuery(URI uri) {
        String query = uri.getQuery();
        String[] pairs = query.split("&");
        HashMap<String, String> queryMap = new HashMap();
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            queryMap.put(parts[0], parts[1]);
        }
        return queryMap;
    }

    @Override
    public void execStateChanged(boolean running, boolean paused, LightController controller) {

    }

    @Override
    public void newFrameReady() {}
}
