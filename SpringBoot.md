# SpringBoot Concepts

- [roadmap.sh](https://roadmap.sh/spring-boot)
- [Coding Shuttle - SpringBoot concepts](https://www.codingshuttle.com/spring-boot-handbook/spring-boot-tutorial-a-comprehensive-guide-for-beginners/)
- [in28minutes](https://github.com/in28minutes/SpringBootForBeginners)
- [SpringBoot Doc](https://docs.google.com/document/d/1q0FCsHbkg1O8NNrc2ooF_9bGGfuWJ8pm8lbnE7nZvvQ/edit?tab=t.0)

### 1. Java Foundations

- Core programming
- OOP (Classes, Inheritance, Polymorphism)
- Collections (List, Map, Set)
- Exception Handling
- Java 8+ Features (Streams, Lambdas, Optionals)
- Functional Interfaces	Common in Spring APIs
- Java 17+ Features (Records, Pattern Matching)

### 2. Build Tools

- Maven or Gradle to manage dependencies and build projects

### 3. Spring Framework / Spring Core

- Servlet & JSP (JavaServer Pages) - before Spring Framework
- Inversion of Control (IoC)
- Dependency Injection (@Autowired, @Component)	- key to decoupling 
- Bean Lifecycle & Scopes for managing object instances 
- ApplicationContext - Spring’s central container 
- Configuration (@Configuration, @Bean)	Manual bean setup
- AOP (Aspect-Oriented Programming)

### 4. Spring MVC (A module within Spring Framework)

- Controllers (@Controller, @RestController)
- Routing (@GetMapping, @RequestMapping)
- Request/Response Handling (@RequestBody, @PathVariable, @RequestParam)
- Validation (@Valid, @NotNull)
- Exception Handling (@ControllerAdvice, @ExceptionHandler)

### 5. Spring Boot

- Auto-configuration 
- Starters (spring-boot-starter-*)	with Predefined dependencies 
- Configuration Files 
- Profiles (dev, prod) and Dynamic Configuration for environment-specific settings
- Embedded servlet containers : Tomcat, Jetty and Undertow
- Executable JARs – No need for external Tomcat deployment
- DevTools, Actuator – Hot reload, monitoring, health endpoints

### 6. Spring Data

- Layer	Topics
- JDBC (Java-level DB access)
- Spring JDBC (JdbcTemplate)	- Simplifies JDBC
- JPA (Java Persistence API)	- ORM specification
- Hibernate	JPA implementation
- Spring Data JPA	            - Interface-based data access (JpaRepository)
- Writing Custom Queries
- Pagination and Sorting
- Transactions (@Transactional)	Data consistency

### 7. Spring Security

- JWT (JSON Web Tokens)	Token-based auth
- Role-based access	Authorization control
- Password encoding	Secure user passwords
- CORS & CSRF to Protect front-end integrations

### 8. Spring Cloud

### 9. Microservices Architecture

- What is Microservices	- Shift from monoliths
- Service Design
- WebFlux (Reactive systems, WebClient)
- Database per service
- Inter-service communication - REST, Feign
- Eureka Service Discovery
- Spring Cloud Gateway	API Gateway
- Spring Cloud Config - Central config management
- Circuit Breakers (Resilience4J) - Fault tolerance
- Load Balancing (Ribbon or Gateway) - Distribute requests

### 10. Observability & Monitoring

- Spring Boot Actuator - App health & metrics
- Logs (Logback, SLF4J) - Debugging
- Distributed Tracing (Sleuth, Zipkin) - Trace request flow
- Alerts & Monitoring (Prometheus, Grafana) - Detect failures

### 11. DevOps & Deployment

- Docker - Package apps into containers
- Docker Compose - Run services locally
- CI/CD (GitHub Actions, Jenkins) - Automate testing and deployment
- Deployment Targets - Deploy to Heroku, AWS, Azure, etc.

## Reference Links

- [Spring Framework](https://www.baeldung.com/spring-intro)
- [Spring Boot](https://www.baeldung.com/spring-boot)
- [Core Features | Official Documentation](https://docs.spring.io/spring-boot/reference/features/index.html)
- GraphQL as an Alternative to REST
