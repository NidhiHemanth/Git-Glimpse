package org.bootcamp.com.config;

import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.exception.JobExecutionFailedException;
import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.repository.GithubRepository;
import org.bootcamp.com.util.GithubItemProcessor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class SpringBatchConfig {
    @Value("${ref.inputDir}")
    private String jsonInputFile;
    @Autowired
    private GithubRepository githubRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public JsonItemReader<GithubUser> reader() {
        log.info("INPUT FILE {}", jsonInputFile);
        return new JsonItemReaderBuilder<GithubUser>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(GithubUser.class))
                .resource(new ClassPathResource(jsonInputFile))
                .name("githubJsonItemReader")
                .build();
    }

    @Bean
    public GithubItemProcessor processor() {
        log.info("INSIDE GithubItemProcessor processor");
        return new GithubItemProcessor();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutorBuilder()
                .concurrencyLimit(10)
                .threadNamePrefix("batch-executor-")
                .build();
    }

    @Bean
    public RepositoryItemWriter<GithubUser> writer() {
        return new RepositoryItemWriterBuilder<GithubUser>()
                .repository(githubRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("json-step", jobRepository)
                .<GithubUser, GithubUser>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
//                .taskExecutor(taskExecutor())                         // TODO: fix taskExecutor impl
                .writer(writer())
                .build();
    }

    @Bean
    public Job runJob() {
        return new JobBuilder("importGithubUsers", jobRepository)
                .start(step1())
                .listener(listener())
                .build();
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobExecutionListener() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    log.info("Batch Job completed successfully.");
                    System.exit(0);  // Trigger application shutdown gracefully
                } else {
                    log.error("Job failed with status: {}", jobExecution.getStatus());
                    throw new JobExecutionFailedException("Job failed with status: " + jobExecution.getStatus());
                }
            }
        };
    }
}
