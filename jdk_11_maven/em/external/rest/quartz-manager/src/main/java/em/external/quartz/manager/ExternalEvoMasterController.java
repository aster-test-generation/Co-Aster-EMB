package em.external.quartz.manager;

import org.evomaster.client.java.controller.AuthUtils;
import org.evomaster.client.java.controller.ExternalSutController;
import org.evomaster.client.java.controller.InstrumentedSutStarter;
import org.evomaster.client.java.controller.api.dto.SutInfoDto;
import org.evomaster.client.java.controller.api.dto.auth.AuthenticationDto;
import org.evomaster.client.java.controller.problem.ProblemInfo;
import org.evomaster.client.java.controller.problem.RestProblem;
import org.evomaster.client.java.sql.DbSpecification;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

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
        String jarLocation = "cs/rest-gui/quartz-manager/quartz-manager-parent/quartz-manager-web-showcase/target";
        if (args.length > 2) {
            jarLocation = args[2];
        }
        if(! jarLocation.endsWith(".jar")) {
            jarLocation += "/quartz-manager-sut.jar";
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
                new ExternalEvoMasterController(controllerPort, jarLocation, sutPort, timeoutSeconds, command);
        InstrumentedSutStarter starter = new InstrumentedSutStarter(controller);

        starter.start();
    }


    private final int timeoutSeconds;
    private final int sutPort;
    private final int dbPort;
    private  String jarLocation;
    private Connection sqlConnection;
    private List<DbSpecification> dbSpecification;


    public ExternalEvoMasterController() {
        this(40100, "../core/target", 12345, 120, "java");
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
        setControllerPort(controllerPort);
        setJavaCommand(command);
    }

    @Override
    public String[] getInputParameters() {
        return new String[]{
                "--server.port=" + sutPort,
                "--quartz-manager.security.accounts.in-memory.users[0].username=foo",
                "--quartz-manager.security.accounts.in-memory.users[0].password=bar",
                "--quartz-manager.security.accounts.in-memory.users[0].roles[0]=admin",
                "--quartz-manager.security.accounts.in-memory.users[1].username=foo2",
                "--quartz-manager.security.accounts.in-memory.users[1].password=bar",
                "--quartz-manager.security.accounts.in-memory.users[1].roles[0]=admin"
        };
    }

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
        return "Started QuartzManagerDemoApplication in ";
    }

    @Override
    public long getMaxAwaitForInitializationInSeconds() {
        return timeoutSeconds;
    }

    @Override
    public void preStart() {
    }

    @Override
    public void postStart() {
    }

    @Override
    public void resetStateOfSUT() {
    }

    @Override
    public void preStop() {
    }

    @Override
    public void postStop() {
    }

    @Override
    public String getPackagePrefixesToCover() {
        return "it.fabioformosa.";
    }


    @Override
    public ProblemInfo getProblemInfo() {

        return new RestProblem(
                "http://localhost:" + sutPort + "/v3/api-docs",
                null,
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
                AuthUtils.getForJsonToken(
                        "USER_1",
                        "/quartz-manager/auth/login",
                        "username=foo&password=bar",
                        "/accessToken",
                        "Bearer ",
                        "application/x-www-form-urlencoded"),
                AuthUtils.getForJsonToken(
                        "USER_2",
                        "/quartz-manager/auth/login",
                        "username=foo2&password=bar",
                        "/accessToken",
                        "Bearer ",
                        "application/x-www-form-urlencoded")
        );
    }


    @Override
    public List<DbSpecification> getDbSpecifications() {
        return null;
    }
}
