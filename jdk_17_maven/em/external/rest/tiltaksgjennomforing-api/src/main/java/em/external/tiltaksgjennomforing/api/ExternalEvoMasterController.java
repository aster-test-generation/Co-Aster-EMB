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


    private final String ISSUER_ID = "aad";
    private final String BESLUTTER_AD_GROUP = "99ea78dc-db77-44d0-b193-c5dc22f01e1d";
    private final String TOKEN_PARAM = "NAVident";
    private static final String NAV1 = "Q987654";


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
        String wellKnownUrlTokenX = oAuth2Server.wellKnownUrl("tokenx").toString();

        return new String[]{
                "--server.port=" + sutPort,
                "--spring.profiles.active=dev-gcp-labs",
                "--spring.datasource.driverClassName=org.postgresql.Driver",
                "--spring.sql.init.platform=postgres",
                "--no.nav.security.jwt.issuer.aad.discoveryurl=" + wellKnownUrl,
                "--no.nav.security.jwt.issuer.tokenx.discoveryurl=" + wellKnownUrlTokenX,
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
        String wellKnownUrl = oAuth2Server.wellKnownUrl(ISSUER_ID).toString();
        String wellKnownUrlTokenX = oAuth2Server.wellKnownUrl("tokenx").toString();

        return new String[]{
                "-DAZURE_APP_WELL_KNOWN_URL=" + wellKnownUrl,
                "-DTOKEN_X_WELL_KNOWN_URL=" + wellKnownUrlTokenX,
                "-DVAULT_TOKEN=VAULT_TOKEN",
                "-DKAFKA_BROKERS=KAFKA_BROKERS",
                "-DKAFKA_TRUSTSTORE_PATH=KAFKA_TRUSTSTORE_PATH",
                "-DKAFKA_CREDSTORE_PASSWORD=KAFKA_CREDSTORE_PASSWORD",
                "-DKAFKA_KEYSTORE_PATH=KAFKA_KEYSTORE_PATH",
                "-DKAFKA_CREDSTORE_PASSWORD=KAFKA_CREDSTORE_PASSWORD",
                "-DKAFKA_SCHEMA_REGISTRY=KAFKA_SCHEMA_REGISTRY",
                "-DKAFKA_SCHEMA_REGISTRY_USER=KAFKA_SCHEMA_REGISTRY_USER",
                "-DKAFKA_SCHEMA_REGISTRY_PASSWORD=KAFKA_SCHEMA_REGISTRY_PASSWORD",
                "-DAZURE_APP_TENANT_ID=AZURE_APP_TENANT_ID",
                "-DAZURE_APP_CLIENT_ID=aad",
                "-DAZURE_APP_CLIENT_SECRET=secret",
                "-Dbeslutter.ad.gruppe=" + BESLUTTER_AD_GROUP
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

    private RequestMapping getRequestMapping(String id, List<String> groups, String name) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("groups",groups);
        claims.put("name",name);
        claims.put("NAVident", id);
        claims.put("sub","sub");
        claims.put("aud",Arrays.asList("fake-aad"));
        claims.put("tid",ISSUER_ID);
        claims.put("azp",id);
        claims.put("acr","Level4");
        claims.put("nonce","myNonce");

        RequestMapping rm = new RequestMapping("NAVident",id, claims, JOSEObjectType.JWT.getType());

        return rm;
    }

    private OAuth2Config getOAuth2Config(){

        List<RequestMapping> mappings = Arrays.asList(
                getRequestMapping(NAV1, Arrays.asList(BESLUTTER_AD_GROUP),"Mock McMockface")
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

    private AuthenticationDto getAuthenticationDto(String label, String oauth2Url){

        AuthenticationDto dto = new AuthenticationDto(label);
        LoginEndpointDto x = new LoginEndpointDto();
        dto.loginEndpointAuth = x;

        x.externalEndpointURL = oauth2Url;
        x.payloadRaw = TOKEN_PARAM+"="+label+"&grant_type=client_credentials&code=foo&client_id=foo&client_secret=secret";
        x.verb = HttpVerb.POST;
        x.contentType = "application/x-www-form-urlencoded";
        x.expectCookies = false;

        TokenHandlingDto token = new TokenHandlingDto();
        token.headerPrefix = "Bearer ";
        token.httpHeaderName = "Authorization";
        token.extractFromField = "/access_token";
        x.token = token;

        return dto;
    }

    @Override
    public List<AuthenticationDto> getInfoForAuthentication() {
//        NAVident=Q987654&grant_type=client_credentials&code=foo&client_id=foo&client_secret=secret
        String url = oAuth2Server.baseUrl() + ISSUER_ID + "/token";
        return Arrays.asList(
                getAuthenticationDto(NAV1,url)
        );
    }


    @Override
    public List<DbSpecification> getDbSpecifications() {
        return dbSpecification;
    }
}
