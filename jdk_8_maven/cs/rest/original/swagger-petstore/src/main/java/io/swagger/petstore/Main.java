//MODIFIED: This code is a simple Java application that starts an embedded Tomcat server to serve the Swagger Petstore application.

package io.swagger.petstore;

import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

public class Main {
    Tomcat tomcat;

    public void startServer(int port) throws Exception {
        tomcat = new Tomcat();

        String tmpDir = System.getProperty("java.io.tmpdir");
        tomcat.setBaseDir(tmpDir);

        tomcat.setPort(port);
        tomcat.getConnector();
        URL webappUrl = Main.class.getClassLoader().getResource("webapp");

        String webappDirLocation;

        // Handle the case where the webapp is inside a JAR
        if (webappUrl.getProtocol().equals("jar")) {
            // Extract the JAR file path and the entry path
            String jarPath = webappUrl.getPath().substring(5, webappUrl.getPath().indexOf("!"));
            String entryPath = webappUrl.getPath().substring(webappUrl.getPath().indexOf("!") + 2);

            // Create a temporary directory to extract the webapp resources
            File tempDir = Files.createTempDirectory("webapp").toFile();
            tempDir.deleteOnExit();

            // Extract the JAR entry to the temporary directory
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(new File(jarPath))) {
                java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith(entryPath) && !entry.isDirectory()) {
                        File file = new File(tempDir, entry.getName().substring(entryPath.length()));
                        file.getParentFile().mkdirs();
                        try (java.io.InputStream is = jar.getInputStream(entry);
                             java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                            while (is.available() > 0) {
                                fos.write(is.read());
                            }
                        }
                    }
                }
            }

            webappDirLocation = tempDir.getAbsolutePath();
        } else {
            // Handle the case where the webapp is in the filesystem
            webappDirLocation = new File(webappUrl.toURI()).getAbsolutePath();
        }
        tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());

        System.out.println("Swagger Petstore running at http://localhost:" + port);
        tomcat.start();
    }

    public Tomcat getTomcat() {
        return tomcat;
    }


    public static void main(String[] args) throws Exception {
        int port = 8080;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0] + ". Using default port 8080.");
            }
        }

        Main app = new Main();
        app.startServer(port);

    }
}