package org.bootcamp.com;

import org.bootcamp.com.exception.JobExecutionFailedException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/*
 @SpringBootApplication - it a combination of the three annotations with their default attributes
                                @Configuration
                                @EnableAutoConfiguration
                                @ComponentScan

 CommandLineRunner - used to execute some code before the application starts up
                     This method will be executed after the application context is loaded and
                     right before the Spring Application run method is completed.
 */

@SpringBootApplication
public class BatchProcessingApplication {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;

    public static void main(String[] args) {
        SpringApplication.run(BatchProcessingApplication.class);
    }

    @Bean
    public CommandLineRunner runBatchJob() {
        return args -> {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            try {
                System.out.println("Starting the batch job...");
                jobLauncher.run(job, jobParameters);
            } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException | JobExecutionFailedException e) {
                System.err.println("Job failed with exception: " + e.getMessage());
            }
        };
    }
}