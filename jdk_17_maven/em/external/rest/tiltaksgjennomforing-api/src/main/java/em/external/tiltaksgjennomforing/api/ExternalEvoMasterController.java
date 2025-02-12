package em.external.tiltaksgjennomforing.api;

import com.nimbusds.jose.JOSEObjectType;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.OAuth2Config;
import no.nav.security.mock.oauth2.token.RequestMapping;
import no.nav.security.mock.oauth2.token.RequestMappingTokenCallback;
import org.evomaster.client.java.controller.ExternalSutController;
import org.evomaster.client.java.controller.InstrumentedSutStarter;
import org.evomaster.client.java.controller.api.dto.SutInfoDto;
import org.evomaster.client.java.controller.api.dto.auth.AuthenticationDto;
import org.evomaster.client.java.controller.api.dto.auth.HttpVerb;
import org.evomaster.client.java.controller.api.dto.auth.LoginEndpointDto;
import org.evomaster.client.java.controller.api.dto.auth.TokenHandlingDto;
import org.evomaster.client.java.controller.api.dto.database.schema.DatabaseType;
import org.evomaster.client.java.controller.problem.ProblemInfo;
import org.evomaster.client.java.controller.problem.RestProblem;
import org.evomaster.client.java.sql.DbSpecification;
import org.testcontainers.containers.GenericContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class ExternalEvoMasterController extends ExternalSutController {


    public static void main(String[] args) {

        int controllerPort = 40100;
        if (args.length > 0) {
            controllerPort = Integer.parseInt(args[0]);
        }
        int sutPort = 12345;
        if (args.length > 1) {
            sutPort = Integer.parseInt(args[1]);
        }
        String jarLocation = "cs/rest/tiltaksgjennomforing-api/target";
        if (args.length > 2) {
            jarLocation = args[2];
        }
        if(! jarLocation.endsWith(".jar")) {
            jarLocation += "/tiltaksgjennomforing-api-sut.jar";
        }

        int timeoutSeconds = 120;
        if(args.length > 3){
            timeoutSeconds = Integer.parseInt(args[3]);
        }
        String command = "java";
        if(args.length > 4){
            command = args[4];
        }


        ExternalEvoMasterController controller =
                new ExternalEvoMasterController(controllerPort, jarLocation,
                        sutPort, timeoutSeconds, command);
        InstrumentedSutStarter starter = new InstrumentedSutStarter(controller);

        starter.start();
    }

    private final int timeoutSeconds;
    private final int sutPort;
    private  String jarLocation;
    private Connection sqlConnection;

    private List<DbSpecification> dbSpecification;


    private static final String POSTGRES_VERSION = "13.13";

    private static final String POSTGRES_PASSWORD = "password";

    private static final int POSTGRES_PORT = 5432;

    private static final GenericContainer postgres = new GenericContainer("postgres:" + POSTGRES_VERSION)
            .withEnv("POSTGRES_PASSWORD", POSTGRES_PASSWORD)
            .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust") //to allow all connections without a password
            .withEnv("POSTGRES_DB", "tiltaksgjennomforing")
            .withTmpFs(Collections.singletonMap("/var/lib/postgresql/data", "rw"))
            .withExposedPorts(POSTGRES_PORT);

    private MockOAuth2Server oAuth2Server;

    private int oAuth2Port;

    private final String ISSUER_ID = "azuread";

    private final String DEFAULT_AUDIENCE = "some-audience";



    public ExternalEvoMasterController(){
        this(40100, "../core/target", 12345, 120, "java");
    }

    public ExternalEvoMasterController(String jarLocation) {
        this();
        this.jarLocation = jarLocation;
    }

    public ExternalEvoMasterController(
            int controllerPort, String jarLocation, int sutPort, int timeoutSeconds, String command
           ) {

        if(jarLocation==null || jarLocation.isEmpty()){
            throw new IllegalArgumentException("Missing jar location");
        }


        this.sutPort = sutPort;
        this.oAuth2Port = sutPort + 1;
        this.jarLocation = jarLocation;
        this.timeoutSeconds = timeoutSeconds;
        setControllerPort(controllerPort);
        setJavaCommand(command);
    }


    @Override
    public String[] getInputParameters() {

        String wellKnownUrl = oAuth2Server.wellKnownUrl(ISSUER_ID).toString();

        return new String[]{
                "--server.port=" + sutPort,
                "--spring.profiles.active=dev-gcp-labs",
                "--spring.datasource.driverClassName=org.postgresql.Driver",
                "--spring.sql.init.platform=postgres",
                "--no.nav.security.jwt.issuer.aad.discoveryurl=" + wellKnownUrl,
                "--no.nav.security.jwt.issuer.tokenx.discoveryurl=" + wellKnownUrl,
                "--management.server.port=-1",
                "--server.ssl.enabled=false",
                "--spring.datasource.url=" + dbUrl(),
                "--spring.datasource.username=postgres",
                "--spring.datasource.password=" + POSTGRES_PASSWORD,
                "--sentry.logging.enabled=false",
                "--sentry.environment=local",
                "--logging.level.root=OFF",
                "--logging.config=classpath:logback-spring.xml",
                "--logging.level.org.springframework=INFO",
        };
    }

    public String[] getJVMParameters() {
        return new String[]{
        };
    }

    private String dbUrl() {

        String host = postgres.getContainerIpAddress();
        int port = postgres.getMappedPort(5432);


        return "jdbc:postgresql://"+host+":"+port+"/tiltaksgjennomforing";
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
        return "Tomcat started on port";
    }

    @Override
    public long getMaxAwaitForInitializationInSeconds() {
        return timeoutSeconds;
    }


    private OAuth2Config getOAuth2Config(){

        List<RequestMapping> mappings = Arrays.asList(
        );

        RequestMappingTokenCallback callback = new RequestMappingTokenCallback(
                ISSUER_ID,
                mappings,
                360000
        );

        Set<RequestMappingTokenCallback> callbacks = Set.of(
                callback
        );

        OAuth2Config config = new OAuth2Config(
                true,
                null,
                null,
                false,
                new no.nav.security.mock.oauth2.token.OAuth2TokenProvider(),
                callbacks
        );

        return config;
    }

    @Override
    public void preStart() {
        postgres.start();
        oAuth2Server = new  MockOAuth2Server(getOAuth2Config());
        oAuth2Server.start(oAuth2Port);
    }

    @Override
    public void postStart() {
        closeDataBaseConnection();

        try {
            sqlConnection = DriverManager.getConnection(dbUrl(), "postgres", POSTGRES_PASSWORD);
            dbSpecification = Arrays.asList(new DbSpecification(DatabaseType.POSTGRES,sqlConnection));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resetStateOfSUT() {
    }

    @Override
    public void preStop() {
        closeDataBaseConnection();
    }

    @Override
    public void postStop() {
        postgres.stop();
        if(oAuth2Server!=null) oAuth2Server.shutdown();
    }

    private void closeDataBaseConnection() {
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
    public String getPackagePrefixesToCover() {
        return "no.nav.tag.tiltaksgjennomforing.";
    }

    @Override
    public ProblemInfo getProblemInfo() {
        return new RestProblem(
                "http://localhost:" + sutPort + "/tiltaksgjennomforing-api/v3/api-docs",
                null
        );
    }

    @Override
    public SutInfoDto.OutputFormat getPreferredOutputFormat() {
        return SutInfoDto.OutputFormat.JAVA_JUNIT_5;
    }


    @Override
    public List<AuthenticationDto> getInfoForAuthentication() {

        //TODO
        return null;
    }


    @Override
    public List<DbSpecification> getDbSpecifications() {
        return dbSpecification;
    }
}
