package org.bootcamp.com.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.constants.Constants;
import org.bootcamp.com.exception.DataValidationException;
import org.bootcamp.com.exception.InvalidJobParametersException;
import org.bootcamp.com.exception.JobExecutionFailedException;
import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.model.Manifest;
import org.bootcamp.com.repository.GithubRepository;
import org.bootcamp.com.util.GithubItemMapper;
import org.bootcamp.com.util.ManifestUtil;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/*
  @Configuration - creates a bean based on configuration file variables
 */

@Configuration
@Slf4j
public class SpringBatchConfig {
    @Value("${ref.inputDir}")
    private String inputFileDir;
    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private GithubRepository githubRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    @StepScope
    public JsonItemReader<GithubUser> reader(@Value(Constants.STEP_PARAMETER_DATA_FILE) String fileName) {
        final String fullFilePath = Paths.get(applicationProperties.getInputDir(), fileName).toString();
        log.info("INSIDE reader with inputFile {}", fullFilePath);
        return new JsonItemReaderBuilder<GithubUser>()
                .resource(new ClassPathResource(fullFilePath))
                .jsonObjectReader(new JacksonJsonObjectReader<>(GithubUser.class))
                .name("githubJsonItemReader")
                .build();
    }

    @Bean
    public GithubItemMapper processor() {
        log.info("INSIDE GithubItemProcessor processor");
        return new GithubItemMapper();
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
        log.info("INSIDE writer");
        return new RepositoryItemWriterBuilder<GithubUser>()
                .repository(githubRepository)                       // Can be Service too
                .methodName("save")
                .build();
    }

    @Bean
    public Step step1(JsonItemReader<GithubUser> reader) {
        return new StepBuilder("json-step", jobRepository)
                .<GithubUser, GithubUser>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor())
//                .taskExecutor(taskExecutor())                         // TODO: fix taskExecutor impl
                .writer(writer())
                .faultTolerant()
                .processorNonTransactional()
                .skip(DataValidationException.class)                    // If database does not support utf8mb4 characters, some users will be skipped
                .skipLimit(10)
                .listener(recordCountListener())
                .build();
    }

    @Bean(name = "job")
    public Job runJob(Step step1, JobParametersValidator validator) {
        return new JobBuilder("importGithubUsers", jobRepository)
                .validator(validator)
                .start(step1)
                .listener(listener())
                .build();
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                final JobParameters jobParameters = jobExecution.getJobParameters();
                final ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
                jobExecutionContext.put(Constants.MANIFEST, jobParameters.getString(Constants.MANIFEST));
                jobExecutionContext.put(Constants.DATA_FILE, jobParameters.getString(Constants.DATA_FILE));
                log.info("BEFORE, JOB PARAMETERS : {}", jobParameters);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                // cleanup
                jobExecution.getExecutionContext().remove(Constants.MANIFEST);
                jobExecution.getExecutionContext().remove(Constants.DATA_FILE);

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

    @Bean
    public StepExecutionListener recordCountListener() {
        return new StepExecutionListener() {
            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                final Manifest manifest = loadManifest(stepExecution.getJobParameters().getString(Constants.MANIFEST));

                if(manifest.getRecordCount() != stepExecution.getReadCount()) {
                    log.error("Step Record Count {} does not match Manifest {}", stepExecution.getReadCount(), manifest.getRecordCount());
                    throw new JobExecutionFailedException("Step Record Count does not match Manifest");
                }

                return stepExecution.getExitStatus();
            }
        };
    }

    // 1. Validate that inputFile & manifestFile exists as mandatory parameters
    // 2. Check if checkSum of the inputFile & in manifestFile matches
    @Bean
    public JobParametersValidator validator(JobParametersValidator checkSumValidator) {
        final CompositeJobParametersValidator compositeJobParametersValidator = new CompositeJobParametersValidator();

        final DefaultJobParametersValidator mandatoryFieldCheck = new DefaultJobParametersValidator();
        mandatoryFieldCheck.setRequiredKeys(new String[]{Constants.DATA_FILE, Constants.MANIFEST});

        // Spring Dependency Injection - checkSumValidator bean is being injected to this method
        //                               and its result is being passed as input
        compositeJobParametersValidator.setValidators(List.of(mandatoryFieldCheck, checkSumValidator));
        return compositeJobParametersValidator;
    }

    // This bean definition can also be replaced with since no customize validator setup is being done
    //      @Autowired Validator beanValidator;
    @Bean
    public Validator beanValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Bean
    public JobParametersValidator checkSumValidator(Validator beanValidator) {
        // Java 8 Feature - Lambda expression to implement a functional interface with only one method
        //                  Here, we are implementing the validate() method in JobParametersValidator
        return jobParameters -> {
            if(jobParameters == null) {
                log.error("JOB PARAMETERS are null !!");
            }

            final Manifest manifest = loadManifest(jobParameters.getString(Constants.MANIFEST));

            // Validator is part of Spring’s general validation framework
            // It is used to check if a Java object (a "bean") is valid i.e all its properties pass constraints
            if(!beanValidator.validate(manifest).isEmpty()) {
                throw new DataValidationException("Manifest JSON File bean has invalid parameters");
            }

            final String calculatedCheckSum = ManifestUtil.calculateCheckSum(Paths.get(applicationProperties.getInputDir(), jobParameters.getString(Constants.DATA_FILE)));

            if(!calculatedCheckSum.equals(manifest.getChecksum())) {
                throw new InvalidJobParametersException("Calculated Checksum of the Data does not match given checksum");
            }
        };
    }

    private Manifest loadManifest(String manifestFile) throws InvalidJobParametersException {
        try {
            final Path manifestFilePath = Paths.get(applicationProperties.getInputDir(), manifestFile);
            log.info("\tLOADING MANIFEST FILE {}", manifestFilePath);
            return objectMapper.readValue(new ClassPathResource(manifestFilePath.toString()).getInputStream(), Manifest.class);
        } catch (Exception e) {
            log.error("Error while reading manifest file into Manifest.class");
            e.printStackTrace();
            throw new InvalidJobParametersException(e);
        }
    }
}