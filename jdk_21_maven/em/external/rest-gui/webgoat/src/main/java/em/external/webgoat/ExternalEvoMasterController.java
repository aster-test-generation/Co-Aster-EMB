package em.external.webgoat;


import org.evomaster.client.java.controller.AuthUtils;
import org.evomaster.client.java.controller.ExternalSutController;
import org.evomaster.client.java.controller.InstrumentedSutStarter;
import org.evomaster.client.java.controller.api.dto.auth.AuthenticationDto;
import org.evomaster.client.java.controller.api.dto.SutInfoDto;
import org.evomaster.client.java.controller.api.dto.database.schema.DatabaseType;
import org.evomaster.client.java.sql.DbSpecification;
import org.evomaster.client.java.controller.problem.ProblemInfo;
import org.evomaster.client.java.controller.problem.RestProblem;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.h2.tools.Server;

public class ExternalEvoMasterController extends ExternalSutController {

    private static final int DEFAULT_CONTROLLER_PORT = 40100;

    private static final int DEFAULT_SUT_PORT = 12345;


    public static void main(String[] args) {

        int controllerPort = DEFAULT_CONTROLLER_PORT;
        if (args.length > 0) {
            controllerPort = Integer.parseInt(args[0]);
        }
        int sutPort = DEFAULT_SUT_PORT;
        if (args.length > 1) {
            sutPort = Integer.parseInt(args[1]);
        }
        String jarLocation = "cs/rest-gui/webgoat/target";
        if (args.length > 2) {
            jarLocation = args[2];
        }
        if (!jarLocation.endsWith(".jar")) {
            jarLocation += "/webgoat-sut.jar";
        }

        int timeoutSeconds = 120;
        if (args.length > 3) {
            timeoutSeconds = Integer.parseInt(args[3]);
        }

        String command = "java";
        if (args.length > 4) {
            command = args[4];
        }

        ExternalEvoMasterController controller =
                new ExternalEvoMasterController(controllerPort, jarLocation, sutPort, timeoutSeconds, command);
        controller.setNeedsJdk17Options(true);

        InstrumentedSutStarter starter = new InstrumentedSutStarter(controller);

        starter.start();
    }


    private final int timeoutSeconds;

    private final int sutPort;
    private final int dbPort;
    private final String tmpDir;
    private Server h2;


    private String jarLocation;
    private Connection sqlConnection;
    private List<DbSpecification> dbSpecification;

    public ExternalEvoMasterController() {
        this(DEFAULT_CONTROLLER_PORT, "../target/webgoat-sut.jar", DEFAULT_SUT_PORT, 120, "java");
    }

    public ExternalEvoMasterController(String jarLocation) {
        this();
        this.jarLocation = jarLocation;
    }

    public ExternalEvoMasterController(int controllerPort, String jarLocation, int sutPort, int timeoutSeconds, String command) {
        this.sutPort = sutPort;
        this.dbPort = sutPort + 1;
        this.jarLocation = jarLocation;
        this.timeoutSeconds = timeoutSeconds;
        String base = Paths.get(jarLocation).toAbsolutePath().getParent().normalize().toString();
        tmpDir = base + "/temp/tmp_webgoat/temp_" + dbPort;

        setControllerPort(controllerPort);
        setJavaCommand(command);
    }

    @Override
    public String[] getInputParameters() {
        return new String[]{
                "--webgoat.port=" + sutPort,
                "--webwolf.port=" + (sutPort + 2),
                "--spring.datasource.url=" + dbUrl() + ";DB_CLOSE_DELAY=-1;",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "--spring.datasource.username=sa",
                "--spring.datasource.password",
                "--spring.jmx.enabled=false",
                "--spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=none",
                "--spring.sql.init.mode=never",
                "--webgoat.server.directory=" + tmpDir,
                "--webgoat.user.directory=" + tmpDir
        };
    }

    private String dbUrl( ) {

        String url = "jdbc";
        url += ":h2:tcp://localhost:" + dbPort + "/mem:testdb_" + dbPort + ";INIT=CREATE SCHEMA IF NOT EXISTS CONTAINER;";

        return url;
    }

    @Override
    public String[] getJVMParameters() {
        return new String[]{
        };
    }


    @Override
    public String getBaseURL() {
        return "http://localhost:" + sutPort;
    }

    @Override
    public String getPathToExecutableJar() {
        return jarLocation;
    }

    @Override
    public String getLogMessageOfInitializedServer() {
        return "Please browse to ";
    }

    @Override
    public long getMaxAwaitForInitializationInSeconds() {
        return timeoutSeconds;
    }

    @Override
    public void preStart() {
        try {
            //starting H2
            h2 = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "" + dbPort);
            h2.start();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void postStart() {
        closeDatabaseConnection();

        try {
            Class.forName("org.h2.Driver");
            sqlConnection = DriverManager.getConnection(dbUrl(), "sa", "");
            dbSpecification = Arrays.asList(new DbSpecification(DatabaseType.H2,sqlConnection)
                    .withInitSqlOnResourcePath("/data.sql")
                    .withSchemas("CONTAINER")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void preStop() {
        closeDatabaseConnection();
    }


    private void closeDatabaseConnection() {
        if (sqlConnection != null) {
            try {
                sqlConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            sqlConnection = null;
        }
    }

    @Override
    public void postStop() {
        if (h2 != null) {
            h2.stop();
        }
    }

    @Override
    public String getPackagePrefixesToCover() {
        return "org.owasp.webgoat.";
    }

    @Override
    public ProblemInfo getProblemInfo() {
        return new RestProblem(
                "http://localhost:" + sutPort + "/WebGoat/v3/api-docs",
                null
        );
    }

    @Override
    public SutInfoDto.OutputFormat getPreferredOutputFormat() {
        return SutInfoDto.OutputFormat.JAVA_JUNIT_5;
    }

    @Override
    public List<AuthenticationDto> getInfoForAuthentication() {

        return Arrays.asList(
                AuthUtils.getForDefaultSpringFormLogin("user1", "testuser", "testuser", "/WebGoat/login"),
                AuthUtils.getForDefaultSpringFormLogin("user2", "testuser2", "testuser", "/WebGoat/login"));
    }



    @Override
    public void resetStateOfSUT() {
    }

    @Override
    public List<DbSpecification> getDbSpecifications() {
        return dbSpecification;
    }

}
