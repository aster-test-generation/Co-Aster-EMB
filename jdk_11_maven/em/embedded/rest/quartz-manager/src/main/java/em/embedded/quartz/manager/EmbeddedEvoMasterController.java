package em.embedded.quartz.manager;


import it.fabioformosa.QuartzManagerDemoApplication;
import org.evomaster.client.java.controller.AuthUtils;
import org.evomaster.client.java.controller.EmbeddedSutController;
import org.evomaster.client.java.controller.InstrumentedSutStarter;
import org.evomaster.client.java.controller.api.dto.SutInfoDto;
import org.evomaster.client.java.controller.api.dto.auth.AuthenticationDto;
import org.evomaster.client.java.controller.problem.ProblemInfo;
import org.evomaster.client.java.controller.problem.RestProblem;
import org.evomaster.client.java.sql.DbSpecification;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class used to start/stop the SUT. This will be controller by the EvoMaster process
 */
public class EmbeddedEvoMasterController extends EmbeddedSutController {

    public static void main(String[] args) {

        int port = 40100;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        EmbeddedEvoMasterController controller = new EmbeddedEvoMasterController(port);
        InstrumentedSutStarter starter = new InstrumentedSutStarter(controller);

        starter.start();
    }


    private ConfigurableApplicationContext ctx;


    public EmbeddedEvoMasterController() {
        this(40100);
    }

    public EmbeddedEvoMasterController(int port) {
        setControllerPort(port);
    }

    @Override
    public String startSut() {

        ctx = SpringApplication.run(QuartzManagerDemoApplication.class, new String[]{
                "--server.port=0",
                "--quartz-manager.security.accounts.in-memory.users[0].username=foo",
                "--quartz-manager.security.accounts.in-memory.users[0].password=bar",
                "--quartz-manager.security.accounts.in-memory.users[0].roles[0]=admin",
                "--quartz-manager.security.accounts.in-memory.users[1].username=foo2",
                "--quartz-manager.security.accounts.in-memory.users[1].password=bar",
                "--quartz-manager.security.accounts.in-memory.users[1].roles[0]=admin",
        });

        return "http://localhost:" + getSutPort();
    }

    protected int getSutPort() {
        return (Integer) ((Map) ctx.getEnvironment()
                .getPropertySources().get("server.ports").getSource())
                .get("local.server.port");
    }


    @Override
    public boolean isSutRunning() {
        return ctx != null && ctx.isRunning();
    }

    @Override
    public void stopSut() {
        ctx.stop();
    }

    @Override
    public String getPackagePrefixesToCover() {
        return "it.fabioformosa.";
    }

    @Override
    public void resetStateOfSUT() {
    }

    @Override
    public ProblemInfo getProblemInfo() {

        return new RestProblem(
                "http://localhost:" + getSutPort() + "/v3/api-docs",
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
