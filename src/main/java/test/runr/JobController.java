package test.runr;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Put;
import jakarta.inject.Inject;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;

import java.util.UUID;

@Controller("/v1/schedule/{intValue}")
public class JobController {
    private final JobScheduler jobScheduler;

    @Inject
    public JobController(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    @Put("/int")
    public void scheduleInt(int intValue) throws Exception {
        // The first time called the id gets locked and all subsequent calls use the first id in the lambda
        JobLambda lambda = () -> runItInt("int", intValue);
        lambda.run(); // Run it immediately to see what is printed
        jobScheduler.scheduleRecurrently("job" + intValue, "* * * * *", lambda);
    }

    @Put("/integer")
    public void scheduleInteger(Integer integerValue) throws Exception {
        // works fine
        JobLambda lambda = () -> runItInt("Integer", integerValue);
        lambda.run(); // Run it immediately to see what is printed
        jobScheduler.scheduleRecurrently("job" + integerValue, "*/10 * * * * *", lambda);
    }

    @Put("/int_uselong")
    public void scheduleIntUseLong(int intValue) throws Exception {
        // ERROR: org.jobrunr.JobRunrException --> org.jobrunr.JobRunrException.shouldNotHappenException
        JobLambda lambda = () -> runItLong("int", intValue);
        lambda.run(); // Run it immediately to see what is printed
        jobScheduler.scheduleRecurrently("job" + intValue, "*/10 * * * * *", lambda);
    }

    @Put("/long")
    public void scheduleInteger(long longValue) throws Exception {
        // BUG?
        // The first time called the value gets cached/locked and all subsequent calls use the id from when the method was first called.
        // Note: This is not a micronaut issue as the job's id reflects the proper value, only the lambda has an issue.
        JobLambda lambda = () -> runItLong("long", longValue);
        lambda.run(); // Run it immediately to see what is printed
        jobScheduler.scheduleRecurrently("job" + longValue, "*/10 * * * * *", lambda);
    }

    @Put("/longobj")
    public void scheduleInteger(Long longValue) throws Exception {
        // Works fine
        JobLambda lambda = () -> runItLong("longobj", longValue);
        lambda.run(); // Run it immediately to see what is printed
        jobScheduler.scheduleRecurrently("job" + longValue, "*/10 * * * * *", lambda);
    }

    @Put("/intuuid")
    public void scheduleIntUuid(int intValue) throws Exception {
        // BUG?
        // The first time called the id gets cached/locked and all subsequent calls use the id from when the method was first called
        // Strangely enough the uuid does not get cached/locked in is different per call.
        UUID uuid = UUID.randomUUID();
        JobLambda lambda = () -> runItInt("intuuid", intValue, uuid);
        lambda.run(); // Run it immediately to see what is printed
        jobScheduler.scheduleRecurrently("job" + intValue, "*/10 * * * * *", lambda);
    }

    @Put("/intobj")
    public void scheduleIntObj(int intValue) throws Exception {
        // BUG?
        // The first time called the "obj" gets cached/locked and all subsequent calls use the "obj" from when the method was first called.
        UUID uuid = UUID.randomUUID();
        MyObj obj = new MyObj(intValue, uuid);
        JobLambda lambda = () -> runItObj("intobj", obj);
        lambda.run(); // Run it immediately to see what is printed
        jobScheduler.scheduleRecurrently("job" + intValue, "*/10 * * * * *", lambda);
    }

    public static void runItInt(String type, int intValue) {
        runItInt(type, intValue, null);
    }

    public static void runItObj(String type, MyObj obj) {
        runItInt(type, obj.getId(), obj.getUuid());
    }

    public static void runItInt(String type, int intValue, UUID uuid) {
        System.out.println("RUNNING, type:" + type + " id:" + intValue + " uuid:" + uuid);
    }

    public static void runItLong(String type, long intValue) {
        System.out.println("RUNNING, type:" + type + " id:" + intValue);
    }

    public static class MyObj {
        private int id;
        private UUID uuid;

        public MyObj() {
        }

        public MyObj(int id, UUID uuid) {
            this.id = id;
            this.uuid = uuid;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

        public String toString() {
            return "id:" + id + " uuid:" + uuid;
        }
    }
}
