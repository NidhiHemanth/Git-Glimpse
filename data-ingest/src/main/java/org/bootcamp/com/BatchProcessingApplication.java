package org.bootcamp.com;

import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.config.ApplicationProperties;
import org.bootcamp.com.constants.Constants;
import org.bootcamp.com.exception.JobExecutionFailedException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
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
@Slf4j
public class BatchProcessingApplication {
    public static void main(String[] args) {
        // WebApplicationType.NONE is required otherwise SpringBoot starts web servers automatically
        // Default WebApplicationType is SERVLET
        SpringApplication application = new SpringApplicationBuilder()
                                            .sources(BatchProcessingApplication.class)
                                            .web(WebApplicationType.NONE)
                                            .build();
        ConfigurableApplicationContext applicationContext = application.run(args);

        // Beans can be fetched like this
        ApplicationProperties applicationProperties = (ApplicationProperties) applicationContext.getBean("applicationProperties");
        log.info("Application Properties : inputDir {}", applicationProperties.getInputDir());

        // Important to Control Shut down in Batch Processing applications (non-web)
        int exitCode = SpringApplication.exit(applicationContext);
        System.exit(exitCode);
    }

    @Bean
    public CommandLineRunner runBatchJob(JobLauncher jobLauncher, Job job) {
        return args -> {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString(Constants.MANIFEST, "manifest.json")
                    .addString(Constants.DATA_FILE, "GitHub_Users.json")
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            try {
                log.info("Starting the batch job...");
                jobLauncher.run(job, jobParameters);
            } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException | JobExecutionFailedException e) {
                log.error("Job failed with exception: {}", e.getMessage());
            }
        };
    }
}