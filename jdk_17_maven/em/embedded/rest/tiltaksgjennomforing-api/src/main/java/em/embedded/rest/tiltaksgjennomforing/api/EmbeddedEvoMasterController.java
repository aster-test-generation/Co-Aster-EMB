package em.embedded.rest.tiltaksgjennomforing.api;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.OAuth2Config;
import no.nav.security.mock.oauth2.token.RequestMapping;
import no.nav.security.mock.oauth2.token.RequestMappingTokenCallback;
import no.nav.tag.tiltaksgjennomforing.TiltaksgjennomforingApplication;
import org.evomaster.client.java.controller.EmbeddedSutController;
import org.evomaster.client.java.controller.InstrumentedSutStarter;
import org.evomaster.client.java.controller.api.dto.SutInfoDto;
import org.evomaster.client.java.controller.api.dto.auth.AuthenticationDto;
import org.evomaster.client.java.controller.api.dto.database.schema.DatabaseType;
import org.evomaster.client.java.controller.problem.ProblemInfo;
import org.evomaster.client.java.controller.problem.RestProblem;
import org.evomaster.client.java.sql.DbCleaner;
import org.evomaster.client.java.sql.DbSpecification;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class EmbeddedEvoMasterController extends EmbeddedSutController {

    private static final String POSTGRES_VERSION = "13.13";

    private static final String POSTGRES_PASSWORD = "password";

    private static final int POSTGRES_PORT = 5432;

    private static final GenericContainer postgresContainer = new GenericContainer("postgres:" + POSTGRES_VERSION)
            .withEnv("POSTGRES_PASSWORD", POSTGRES_PASSWORD)
            .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust") //to allow all connections without a password
            .withEnv("POSTGRES_DB", "tiltaksgjennomforing")
            .withTmpFs(Collections.singletonMap("/var/lib/postgresql/data", "rw"))
            .withExposedPorts(POSTGRES_PORT);

    private ConfigurableApplicationContext ctx;

    private Connection sqlConnection;
    private List<DbSpecification> dbSpecification;

    private MockOAuth2Server oAuth2Server;
    private final String ISSUER_ID = "azuread";
    private final String DEFAULT_AUDIENCE = "some-audience";


    public EmbeddedEvoMasterController() {
        this(40100);
    }

    public EmbeddedEvoMasterController(int port) {
        setControllerPort(port);
    }

    public static void main(String[] args) {
        int port = 40100;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        EmbeddedEvoMasterController controller = new EmbeddedEvoMasterController(port);
        InstrumentedSutStarter starter = new InstrumentedSutStarter(controller);

        starter.start();
    }

    @Override
    public boolean isSutRunning() {
        return ctx!=null && ctx.isRunning();
    }

    @Override
    public String getPackagePrefixesToCover() {
        return "no.nav.tag.tiltaksgjennomforing.";
    }

    @Override
    public List<AuthenticationDto> getInfoForAuthentication() {
        //TODO
        return null;
    }

    @Override
    public ProblemInfo getProblemInfo() {
        return new RestProblem(
                "http://localhost:" + getSutPort() + "/tiltaksgjennomforing-api/v3/api-docs",
                null
        );
    }

    @Override
    public SutInfoDto.OutputFormat getPreferredOutputFormat() {
        return SutInfoDto.OutputFormat.JAVA_JUNIT_5;
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
    public String startSut() {
        postgresContainer.start();

        String postgresURL = "jdbc:postgresql://" + postgresContainer.getHost() + ":" + postgresContainer.getMappedPort(POSTGRES_PORT) + "/tiltaksgjennomforing";

        oAuth2Server = new  MockOAuth2Server(getOAuth2Config());
        oAuth2Server.start(8081); //ephemeral gives issues in generated tests
        String wellKnownUrl = oAuth2Server.wellKnownUrl(ISSUER_ID).toString();

        //TODO should go through all the environment variables in application properties
        //TODO some of these might not be needed any more after change of profile
        System.setProperty("AZURE_APP_WELL_KNOWN_URL",wellKnownUrl);
        System.setProperty("TOKEN_X_WELL_KNOWN_URL",wellKnownUrl);
        System.setProperty("VAULT_TOKEN","VAULT_TOKEN");
        System.setProperty("KAFKA_BROKERS","KAFKA_BROKERS");
        System.setProperty("KAFKA_TRUSTSTORE_PATH","KAFKA_TRUSTSTORE_PATH");
        System.setProperty("KAFKA_CREDSTORE_PASSWORD","KAFKA_CREDSTORE_PASSWORD");
        System.setProperty("KAFKA_KEYSTORE_PATH","KAFKA_KEYSTORE_PATH");
        System.setProperty("KAFKA_CREDSTORE_PASSWORD","KAFKA_CREDSTORE_PASSWORD");
        System.setProperty("KAFKA_SCHEMA_REGISTRY","KAFKA_SCHEMA_REGISTRY");
        System.setProperty("KAFKA_SCHEMA_REGISTRY_USER","KAFKA_SCHEMA_REGISTRY_USER");
        System.setProperty("KAFKA_SCHEMA_REGISTRY_PASSWORD","KAFKA_SCHEMA_REGISTRY_PASSWORD");
        System.setProperty("AZURE_APP_TENANT_ID","AZURE_APP_TENANT_ID");
        System.setProperty("AZURE_APP_CLIENT_ID","AZURE_APP_CLIENT_ID");
        System.setProperty("AZURE_APP_CLIENT_SECRET","AZURE_APP_CLIENT_SECRET");
        System.setProperty("beslutter.ad.gruppe","99ea78dc-db77-44d0-b193-c5dc22f01e1d");

        ctx = SpringApplication.run(TiltaksgjennomforingApplication.class, new String[]{
                "--server.port=0",
                //"--spring.profiles.active=dev-fss",
                "--spring.profiles.active=dev-gcp-labs",
                "--spring.datasource.driverClassName=org.postgresql.Driver",
                "--spring.sql.init.platform=postgres",
                "--no.nav.security.jwt.issuer.aad.discoveryurl=" + wellKnownUrl,
                "--no.nav.security.jwt.issuer.tokenx.discoveryurl=" + wellKnownUrl,
                "--management.server.port=-1",
                "--server.ssl.enabled=false",
                "--spring.datasource.url=" + postgresURL,
                "--spring.datasource.username=postgres",
                "--spring.datasource.password=" + POSTGRES_PASSWORD,
                "--sentry.logging.enabled=false",
                "--sentry.environment=local",
                "--logging.level.root=OFF",
                "--logging.config=classpath:logback-spring.xml",
                "--logging.level.org.springframework=INFO",
        });


        if (sqlConnection != null) {
            try {
                sqlConnection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            sqlConnection = DriverManager.getConnection(postgresURL, "postgres", POSTGRES_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        dbSpecification = Arrays.asList(new DbSpecification(DatabaseType.POSTGRES, sqlConnection));

        return "http://localhost:" + getSutPort();
    }

    protected int getSutPort() {
        return (Integer) ((Map) ctx.getEnvironment()
                .getPropertySources().get("server.ports").getSource())
                .get("local.server.port");
    }

    @Override
    public void stopSut() {
        postgresContainer.stop();
        if(ctx != null) ctx.stop();
    }

    @Override
    public void resetStateOfSUT() {

    }

    @Override
    public List<DbSpecification> getDbSpecifications() {
        return dbSpecification;
    }
}
