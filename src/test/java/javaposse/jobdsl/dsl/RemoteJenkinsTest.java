package javaposse.jobdsl.dsl;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;
import org.junit.Test;

import java.net.URI;
import java.util.Map;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertTrue;

public class RemoteJenkinsTest {

    @Test
    public void testRemoteJobCreation() throws Exception {
        JenkinsServer jenkinsServer = new JenkinsServer(new URI("http://jenkins.localrig:9000/jenkins"));
        Map<String, com.offbytwo.jenkins.model.Job> jobs = jenkinsServer.getJobs();
        assertTrue(jobs.size() > 0);

        FolderJob folder = jenkinsServer.getFolderJob(jenkinsServer.getJob("healthcheck")).get();
        jenkinsServer.getJobs(folder);

        String jobXml = new JobDslWriter().writeJobXml("job('generated') { steps { shell 'sleep 5; echo  ' + PARAM } }",
                singletonMap("PARAM", "PASSIT"));
        String jobName = "generated";
        JobWithDetails job = jenkinsServer.getJob(folder, jobName);
        if (job != null) {
            jenkinsServer.deleteJob(folder, jobName);
        }
        jenkinsServer.createJob(folder, jobName, jobXml, true);
        JobWithDetails newJob = jenkinsServer.getJob(folder, jobName);

        ExtractHeader location = newJob.getClient().post(newJob.getUrl() + "build", null, ExtractHeader.class);
        QueueReference build = new QueueReference(location.getLocation());
        //QueueReference build = job.build(Collections.singletonMap("PARAM", "hah"), false);
        QueueItem queueItem = jenkinsServer.getQueueItem(build);
        while (queueItem.getExecutable() == null) {
            sleep(500);
            queueItem = jenkinsServer.getQueueItem(build);
        }
        System.out.println("Building");
        job = jenkinsServer.getJob(folder, jobName);
        Build lastBuild = job.getLastBuild();
        boolean isBuilding = lastBuild.details().isBuilding();
        while (isBuilding) {
            System.out.println("Is building...(" + lastBuild.getNumber() + ")");
            Thread.sleep(200);
            isBuilding = lastBuild.details().isBuilding();
        }
        System.out.println(lastBuild.details().getResult());
    }
}
