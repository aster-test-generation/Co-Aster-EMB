//MODIFIED: This code is a simple Java application that starts an embedded Tomcat server to serve the Swagger Petstore application.

package io.swagger.petstore;

import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0] + ". Using default port 8080.");
            }
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        String webappDirLocation = "src/main/webapp";
        tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());

        System.out.println("Swagger Petstore running at http://localhost:" + port);
        tomcat.start();
        tomcat.getServer().await();
    }
}