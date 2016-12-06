package javaposse.jobdsl.dsl;

import buildit.ci.test.RemoteJenkinsPipelineClient;
import com.offbytwo.jenkins.model.BuildWithDetails;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static com.offbytwo.jenkins.model.BuildResult.SUCCESS;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertEquals;

public class RigJenkinsTest {

    private RemoteJenkinsPipelineClient client;

    @Before
    public void setUp() throws Exception {
        client = new RemoteJenkinsPipelineClient("http://jenkins.localrig:9000/jenkins", System.out);
    }

    @Test
    public void testRemoteJobCreation() throws Exception {
        final BuildWithDetails details = client.executePipeline("node { sh 'echo YAY' }",
                singletonMap("PARAM", "YAY"));
        System.out.println(details.getConsoleOutputText());
        assertEquals(SUCCESS, details.getResult());
    }

    @Test
    public void testNexusStagingOperations() throws Exception {
        assertSuccessfulBuild("NexusRPMTest_Staging.groovy");
    }

    @Test
    public void testNexusReleaseOperations() throws Exception {
        assertSuccessfulBuild("NexusRPMTest_Release.groovy");
    }

    private void assertSuccessfulBuild(String pipeline) throws IOException {
        BuildWithDetails details = client.executePipeline(Paths.get("./src/test/pipeline/" + pipeline));
        System.out.println(details.getConsoleOutputText());
        assertEquals(SUCCESS, details.getResult());
    }

    @Test
    public void testDockerBuild() throws Exception {
        assertSuccessfulBuild("DockerBuildTest.groovy");
    }
}