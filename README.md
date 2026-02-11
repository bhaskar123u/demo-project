## Spring-boot udemy course notes

1. Spring boot is known for dependency injection. When we start a spring-boot project, beans are created in Application Context and stored for injections into classes later.

2. When we have a @Lazy on a field in a class as below
```java
@Component
public class OnlineOrder implements Order {
    @Lazy
    @Autowired
    Product product;
    // other fields
}
```
spring creates OnlineOrder object by adding a proxy product object for the time being. When we want to use product then spring injects the actual bean. If the product class is marked as final, spring would not be able to create a proxy and inject and the application will fail to start.

3. AOP : Aspect Oriented Programming, is known for offloading the cross-cutting concerns such as logging, updating metrics etc. so that we can focus on writing main business logic.

4. AOP key terms -> Aspect(it is the file containing advice and pointcut), Advice(it is a method that perform some cross-cutting task), Pointcut(it tells where all this advice is applicable).

5. Pointcut -> tells where advice should be applied. Types of point cuts :
   - execution(for methods matching the pointcut expression) e.g., @Before("execution(public String com.bsharan.demo_project.components.User.init())"), wildcards can be used (*(exact 1 match) ..(0 or more match))
   - within(matches all method within a class/package) e.g., @Before("within(com.bsharan.demo_project.components.User)") OR @Before("within(com.bsharan.demo_project.components..*)")
   - @within(any class have a particular annotation) e.g., @Before("@within(org.springframework.stereotype.Component)")
   - @annotation(matches any method that is annotated with given annotation) e.g., ("@annotation(org.springframework.web.bind.annotation.GetMapping)")
   - args(matches any method with particular argument) e.g., @Before("args(String, int)") OR @Before("args(com.bsharan.demo_project.components.User, Long)")
   - @args(matches any method with particular parameters and that parameter class is annotated with particular annotation)

6. We can also combine multiple point cuts using &&, ||

7. Named point cuts e.g., @Pointcut("execution(...)") public void customPointcutName(){ //empty method }. Now it can be used as @Before("customPointcutName").

8. @Before, @After, @Around(it surrounds the method start and end).

9. In case of @Around, we have to call the method explicitly, it can be done using joinPoint.proceed(). So the flow is -> PC expression matched for @Around, then advice starts executing until it reaches joinPoint.proceed()

10. How AOP works :
   - When spring application starts, it looks for all @Aspect classes
   - Parse the pointcut and store in efficient data structure for effective lookup (PointcutParser.class)
   - Look for @Component, @Service... annotation classes and for each class check for eligibility based on pointcut
   - Creates a proxy and the proxy has code to execute advice (AbstractAutoProxyCreator.class, DefaultAopProxyFactory.class, ReflectiveMethodInvocation.class).

11. @Transaction - used when we want an operation to execute under a transaction(AOP behind the scene). If applied at class level it automatically applied to all public methods and it can also be applied at a method level. Can we use this annotation on a class marked as final or a method marked as final? -> No, because CGLIB won't be able to create it's proxy.

12. When we want ACID, we have to start DB operations in a transaction. BEGIN TRANSACTION, if all success then COMMIT else ROLLBACK. Now all of this we don't need to write, it is taken care under an AOP(TransactionAspectSupport.class). There is a joinPoint which actually invokes methods.

13. Below is the hierarchy for transaction managers in spring-boot. * marked are for local transactions(happening in a single machine)
```text
                <<TransactionManager>>
                            |
                            v
                <<PlatformTransactionManager>> (getTransaction(), commit(), rollback())
                            |
                            v
                AbstractPlatformTransactionManager (default implementation for above methods)
                            |
                            v
        +---------------------------------------------+
        |               |               |             |
        v               v               v             v
*DataSourceTM      *Hibernate-TM     *JPA-TM     JTA-TM (distributed, 2PC)
        |                                 
        v                                 
    JDBC-TM(local JDBC)
```
14. Transaction Management -> Declarative approach(@Transactional), Programmatic approach

15. Declarative approach -> Based on underlying Datasource, spring-boot chooses a transaction manager itself. If we want to give our configs, it can be done as below
```java
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb");
        dataSource.setUsername("root");
        dataSource.setPassword("password");
        return dataSource;
    }

    // if we don't provide any name, the method name is the bean name
    // here bean name is -> "userTransactionManager"
    @Bean
    public PlatformTransactionManager userTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

// we can use this as below
@Component
public class UserDeclarative {
    @Transactional(transactionManager = "userTransactionManager")
    public void updateUserProgrammatic(){
        // some DB operations
    }
}
```
16. Programmatic approach -> transaction management using code. Why do we need it in first place?
```java
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class User {
    @Transactional
    public void fetchAadharCardForVerification(){
        // 1. update DB
        // 2. call external API
        // 3. update DB
    }
    // issue in this code, if the external API is taking a lot of time, the DB,
    // connection pool etc. would be locked until then under a transaction, this
    // is a case of resource contention, solution -> programmatic transaction management
    // in which we can control the flow i.e., where to begin, where to commit and rollback
}
```
17. We can create a bean as below, there are also another ways using TransactionTemplate
```java
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

@Component
public class UserProgrammatic {

    PlatformTransactionManager userTransactionManager;

    UserProgrammatic(PlatformTransactionManager userTransactionManager) {
        this.userTransactionManager = userTransactionManager;
    }

    public void fetchAadharCardForVerification() {
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = userTransactionManager.getTransaction(defaultTransactionDefinition);
        try{
            // 1. update DB
            // 2. call external API
            // 3. update DB
            // only difference -> since we have userTransactionManager here, we can control where to commit and rollback and all.
            userTransactionManager.commit(transactionStatus);
        } catch(Exception e){
            userTransactionManager.rollback(transactionStatus);
        }
    }
}
```
17. Transaction Propagation -> used while creating transactions, useful if the transaction spans multiple services. Common types under declarative approach are :
    - REQUIRED :
      - if parent txn present, use it
      - else create new
    - REQUIRED_NEW :
      - if parent txn present
          - suspend it and create a new txn, once child txn is finished, resume the parent txn.
      - else create new txn
    - SUPPORTS :
      - if parent txn present, use it 
      - else execute without any transaction
    - NOT_SUPPORTED : 
      - if parent txn present, suspend it, execute method without txn, resume parent txn
      - else execute without any transaction
    - MANDATORY :
      - if parent txn is present use it
      - else throw exception
    - NEVER :
      - if parent txn is present, throw exception
      - else execute without txn

18. In Programmatic way, we can set the transaction propagation in the transaction definition and that can be passed along to other methods.
```java
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
public class UserProgrammatic {

    PlatformTransactionManager userTransactionManager;

    UserProgrammatic(PlatformTransactionManager userTransactionManager) {
        this.userTransactionManager = userTransactionManager;
    }

    public void fetchAadharCardForVerification() {
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        defaultTransactionDefinition.setName("Testing propagation methods");
        defaultTransactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus transactionStatus = userTransactionManager.getTransaction(defaultTransactionDefinition);
        try {
            // 1. update DB
            // 2. call external API
            // 3. update DB
            // only difference -> since we have userTransactionManager here, we can control where to commit and rollback and all.
            userTransactionManager.commit(transactionStatus);
        } catch (Exception e) {
            userTransactionManager.rollback(transactionStatus);
        }
    }
}
```
19. Isolation levels - It tells how changes made by one transaction are visible to other transaction running in parallel. Depending on DB, the default isolation level changes.
```java
import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public void fetchAadharCardForVerification(){
    // DB operations
}
```
20. Springboot JPA
```text
                         ┌────────────────────────┐
                         │    Application Logic   │
                         └─────────────┬──────────┘
                                       │
                         ┌─────────────▼──────────────┐
                         │ ORM Framework (JPA - API)  │
                         │ e.g., Hibernate/OpenJPA    │
                         └─────────────┬──────────────┘
                                       │
                         ┌─────────────▼──────────────┐
                         │     JDBC (API Interface)   │
                         └─────────────┬──────────────┘
                                       │
                         ┌─────────────▼─────────────────┐
                         │ Database Driver (Actual Impl) │
                         └─────────────┬─────────────────┘
                                       │
                         ┌─────────────▼──────────────┐
                         │     Relational Database    │
                         └────────────────────────────┘
```
21. Database Driver -> It is nothing but implementation of JDBC api(s). So assume we have a class as below defined in JDBC api.
```java
public Connection getConnection(){}
```
Now all the drivers Connector/J, PostgreSQL JDBC Driver, H2 Database Engine they implement these functions.

22. JDBC without spring-boot
```java
import org.hibernate.annotations.processing.SQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public Connection getConnection() {
        try {
            // load driver in JVM
            Class.forName("org.h2.Driver");

            // establish connection with DB
            return DriverManager.getConnection("jdbc:h2:mem:userDB", "sa", "");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

public class UserDAO {

    public void createUserTable() {
        try {
            Connection connection = new DatabaseConnection().getConnection();

            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "user_name VARCHAR(100), " +
                    "age INT" +
                    ");";

            PreparedStatement preparedQuery = connection.prepareStatement(sql);
            preparedQuery.executeUpdate();

        } catch (SQLException e) {
            // handle exception
            e.printStackTrace();
        } finally {
            // close statement and DB connection
        }
    }

    public void createUser(String userName, int userAge) {
        try {
            Connection connection = new DatabaseConnection().getConnection();

            String insertQuery = "INSERT INTO users (user_name, age) VALUES (?, ?)";
            PreparedStatement preparedQuery = connection.prepareStatement(insertQuery);
            preparedQuery.setString(1, userName);
            preparedQuery.setInt(2, userAge);

            preparedQuery.executeUpdate();

        } catch (SQLException e) {
            // handle exception
            e.printStackTrace();
        } finally {
            // close preparedQuery and connection
        }
    }

    public void readUsers() {
        try {
            Connection connection = new DatabaseConnection().getConnection();

            String selectQuery = "SELECT * FROM users";
            PreparedStatement preparedQuery = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedQuery.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String name = resultSet.getString("user_name");
                int age = resultSet.getInt("age");

                System.out.println(userId + " " + name + " " + age);
            }

        } catch (SQLException e) {
            // handle exception
            e.printStackTrace();
        } finally {
            // close resultSet, preparedQuery and connection
        }
    }
}
```
Problem with this approach is a lot of boilerplate code is written again and again such as :
  - Driver class loading
  - Establish connection
  - Exception Handling
  - Closing of DB connection and other objects to prevent memory leaks
  - Manual handling of connection pool

23. JDBC with spring-boot -> Spring-boot provides JDBCTemplate class(Spring abstraction on top of JDBC using Template Design Pattern) which helps to remove all boilerplate code when we were using raw JDBC.
```text
            Your Code
               ↓
            JdbcTemplate        ← orchestrates JDBC usage
               ↓
            JDBC API            ← interfaces
               ↓
            JDBC Driver         ← implementation
               ↓
            Database
```
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

public class User {
    long userId;
    String userName;
    int age;
    // constructors + getters + setters + others
}

@Repository
public class UserRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void createTable() {
        String sql = "CREATE TABLE users (user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_name VARCHAR(100), age INT);";
        jdbcTemplate.execute(sql);
    }

    public void insertUser(String name, int age) {
        String insertQuery = "INSERT INTO users (user_name, age) VALUES (?, ?);";
        jdbcTemplate.update(insertQuery, name, age);
    }

    public List<User> getUsers() {
        String selectQuery = "SELECT * FROM users;";
        return jdbcTemplate.query(selectQuery, (rs, rowNum) -> {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setUserName(rs.getString("user_name"));
            user.setAge(rs.getInt("age"));
            return user;
        });
    }
}

@Component
public class UserService {

    @Autowired
    UserRepository userRepository;

    public void createTable() {
        userRepository.createTable();
    }

    public void insertUser(String userName, int age) {
        userRepository.insertUser(userName, age);
    }

    public List<User> getUsers() {
        List<User> users = userRepository.getUsers();
        for (User user : users) {
            System.out.println(
                    user.getUserId() + " : " +
                            user.getUserName() + " : " +
                            user.getAge()
            );
        }
        return users;
    }
}
```
All connection details can be present in application.properties
```application.properties
spring.datasource.url=jdbc:h2:file:./data/testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.hibernate.ddl-auto=update
```
JdbcTemplate takes care of all connection establishing, running insert and read query, closing DB connections etc. It throws granular level of exceptions for better debugging. Default HikariDataSource is used and it provides inbuilt HikariCP. If we want to use some different datasource, it can be used in a Config class.

24. JdbcTemplate still doesn't have automatic object mapping, caching, domain modeling, object graph management, relationship handling, hence to improve all these issues Hibernate was introduced. Why ORM? ORM allows us to work with objects. Acts as bridge between java objects and database tables. With JDBC, we still have
  - manual object mapping(resultset -> objects) : ORM -> @Entity, @Column
  - manual join handling : ORM -> @JoinColumn, @OneToMany, @ManyToOne
  - no notion of object graph/domain model
  - no change tracking
  - no caching : ORM -> @Cacheable
  - no inheritance mapping : ORM -> @Inheritance

```text
            Your Code
               ↓
         Hibernate / JPA     ← ORM + mapping + unit of work
               ↓
            JDBC APIs
               ↓
         Database Driver
               ↓
         Actual Database
```
But, JDBC does not provide
  - Automatic entity mapping
  - Relationship handling (@OneToMany, @OneToOne)
  - Object graph loading
  - Lazy loading
  - Dirty checking
  - Persistence context (1st-level cache)
  - Unit of Work
  - Cascades
  - Inheritance mapping
  - Second-level cache
  - JPQL / Criteria
  - Transparent transactions at object level

Hibernate provides
  - Automatic ResultSet → Object mapping
  - Entity state management (managed / detached)
  - Dirty checking (auto update generation)
  - Unit of Work (persistence context)
  - Identity map (no duplicate objects)
  - Relationship handling (@OneToMany, etc.)
  - Lazy loading & proxies
  - Cascading operations
  - JPQL / HQL / Criteria API
  - Database-vendor abstraction (Dialect)
  - First-level cache
  - Optional second-level cache
  - Inheritance mapping
  - Transaction integration
  - Batch processing support
```text
        Hibernate (for CRUD + domain logic)
        JdbcTemplate (for reports / batch / complex queries)
```

25. Hibernate was vendor specific and applications that used Hibernate directly became tightly coupled to it. Switching to a different ORM provider was difficult. To solve this problem, JPA (Java Persistence API) was introduced. JPA is a specification (interfaces + annotations). Hibernate is an implementation of this specification. Applications should depend on the JPA abstraction and let Hibernate act as the underlying provider. This keeps the codebase clean, portable, and maintainable. Below are some of the JPA imports which should be used:

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;
```
To use JPA in a Spring Boot application, add the spring-boot-starter-data-jpa dependency. This starter pulls in JPA APIs (specification only), Spring Data JPA (provides JpaRepository, repository proxies, method-name query derivation, pagination, sorting, specifications, etc.), a JPA provider (Hibernate by default), and transaction and ORM infrastructure.

Below is the internal architecture of JPA:
![JPA Architecture](src/main/resources/images/JPA-Architecture.png)

```text
            ┌────────────────────────────────────────────┐
            │               Your Code                    │
            │  @Service, @Controller, Business Logic     │
            └────────────────────────────────────────────┘
                                ↓
            ┌────────────────────────────────────────────┐
            │           Spring Data JPA                  │
            │  - Repository Proxies                      │
            │  - Method-name query derivation            │
            │  - Pagination / Sorting / Specs            │
            └────────────────────────────────────────────┘
                                ↓
            ┌────────────────────────────────────────────┐
            │               JPA API                      │
            │  - @Entity, @OneToMany                     │
            │  - EntityManager                           │
            │  - Persistence Context contract            │
            │  (Specification ONLY, no implementation)   │
            └────────────────────────────────────────────┘
                                ↓
            ┌────────────────────────────────────────────┐
            │         Hibernate (JPA Provider)           │
            │  - ORM engine                              │
            │  - Dirty checking                          │
            │  - SQL generation                          │
            │  - Dialect handling                        │
            │  - 1st / 2nd level cache                   │
            │  - Session / Unit of Work                  │
            └────────────────────────────────────────────┘
                                ↓
            ┌────────────────────────────────────────────┐
            │               JDBC API                     │
            │  - Connection                              │
            │  - PreparedStatement                       │
            │  - ResultSet                               │
            │  (Interfaces only)                         │
            └────────────────────────────────────────────┘
                                ↓
            ┌────────────────────────────────────────────┐
            │             JDBC Driver                    │
            │  - MySQL / Postgres / Oracle driver        │
            │  - Wire protocol                           │
            │  - Vendor-specific implementation          │
            └────────────────────────────────────────────┘
                                ↓
            ┌────────────────────────────────────────────┐
            │               Database                     │
            │  - Query parsing                           │
            │  - Execution plan                          │
            │  - Storage engine                          │
            └────────────────────────────────────────────┘
```

Spring Data JPA eliminates boilerplate DAO code. Without Spring Data JPA, developers would need to manually write repository implementations and wire EntityManager everywhere.

JPA APIs standardize ORM behavior across Java and decouple application code from a specific ORM vendor. JPA defines what an ORM should do, not how it should do it.

Hibernate implements the JPA specification and bridges object-oriented domain models with relational databases. JDBC works with rows and columns, while Hibernate allows developers to work with objects and relationships.

JDBC APIs provide a standard database access API in Java and avoid database-vendor-specific code. JDBC is the lowest common contract every Java database solution relies on.

JDBC drivers implement JDBC APIs for a given database and translate Java calls into database-specific wire protocol.

How JPA works internally: The Persistence Unit (PU) stores database configuration (driver, URL, username, password, dialect, etc.) along with entity mapping metadata. Hibernate (as the JPA provider) reads the persistence unit configuration during application startup. Using this configuration, an EntityManagerFactory (EMF) is created. Internally, this is Hibernate’s SessionFactory. Spring Boot autoconfigures and manages the lifecycle of the EMF, while Hibernate provides the actual implementation. The EntityManagerFactory produces EntityManager instances. Whenever the application needs to interact with the database for CRUD operations, it does so via an EntityManager. Each EntityManager is associated with a Persistence Context, which:

* holds managed entity instances
* provides first-level (L1) caching
* performs dirty checking and change tracking

Below is the complete flow, assuming we hit a findById(1) method:
```text
        findById(1)
            → Repository Proxy
                -> Runtime-generated implementation of the @Repository interface
                -> Holds a reference to the correct EntityManager
                -> Proxy is created once at application startup
            → EntityManager.find(User.class, 1)
            → Hibernate Session (Hibernate’s implementation of EntityManager)
                -> Session.find()
            → Persistence Context
                -> Check 1st-level cache
                -> If entity exists, return it immediately
            → SQL generation phase
                -> (For non-PK queries: JPQL/HQL parsing)
                -> SQL AST creation
                -> Dialect applied to generate vendor-specific SQL
            → JDBC API
                -> PreparedStatement ps = connection.prepareStatement(sql)
                -> ps.setLong(1, 1)
                -> ResultSet rs = ps.executeQuery()
            → JDBC Driver
                -> Converts JDBC calls to database-specific wire protocol
            → Database
            ← ResultSet
            ← Hibernate hydrates entity
                -> User(id=1, name="Bhaskar Sharan")
            ← Entity returned to caller
```
For every @Repository interface, Spring Data JPA creates a JpaRepositoryFactory during application startup. This factory is responsible for creating repository implementations, wiring the EntityManager, and generating query logic. Spring then generates a proxy class at runtime (for example, UserRepository$$Proxy). The proxy implements UserRepository and JpaRepository and intercepts all method calls. Proxies are injected with the EntityManager, query metadata, and method mappings. Spring Data inspects every method in UserRepository and builds metadata for each method.
```text
Method            Category
--------------------------
findById          CRUD (from JpaRepository)
save              CRUD
findByEmail       Derived Query
existsByEmail     Derived Exists Query
```
For methods like findByEmail, Spring Data parses the method name (find → SELECT, ByEmail → WHERE email = ?) and internally builds a JPQL query like:
```sql
select u from User u where u.email = :email
```
All derived queries and method metadata are cached, so parsing does not happen on every invocation. These repository proxies provide:
* persistence logic delegation
* transaction participation
* query derivation from method names
* exception translation
* AOP integration

Hibernate then parses the query, builds an abstract syntax tree, applies the dialect, generates SQL, and hands execution over to the JDBC APIs. The dialect is used by Hibernate during SQL generation, not during JDBC execution. Hibernate generates SQL based on entity metadata, and the dialect adapts this SQL to be database-vendor specific. JDBC simply executes the final SQL.

26. Persistence Unit -> Concept defined by JPA, it is everything Hibernate needs to know to build the ORM engine. Once it is built PU is not actively involved then. Logical grouping of all entity who shares same config(means who all are stored in same DB). PU = configuration + metadata + runtime infrastructure (Database / infrastructure information + ORM / entity mapping information). If we are not using spring-boot we would create a persistence.xml file which holds all config related information. If we have 1 DB, we can use application.properties

![Persistence.xml](src/main/resources/images/Persistence-Unit.png)
```text
        JVM process starts (class loads)
             ↓
        Spring Boot startup (SpringApplication.run(...), context initialisation)
             ↓
        application.properties is loaded (spring reads spring.datasource.*, spring.jpa.*)
             ↓
        JpaProperties / DataSourceProperties are created
             ↓
        DataSource is auto-configured (HikariCP, JDBC driver)
             ↓
        Hibernate configuration (dialect, naming strategies, entity scanning, cache config)
             ↓
        Persistence Unit is initialized (hibernate reads entity meta-data, builds mapping models, finalizes ORM configuration)
             ↓
        EntityManagerFactory is created (Hibernate SessionFactory internally)
             ↓
        Application is ready to serve requests (repositories are wired, EM can now be created per request/transaction)
```
The Persistence Unit is not created before Hibernate configuration and not after EMF creation —
it is realized during Hibernate bootstrapping and finalized by creating the EntityManagerFactory. So conceptually: Persistence Unit = Hibernate metadata + mappings + dialect + EMF. PU ≠ Database. You can have: Multiple PUs → same database, Multiple PUs → different databases
Example:
  - PU1 → MySQL (schema A)
  - PU2 → MySQL (schema B)
  - PU3 → PostgreSQL
All inside one JVM.

27. SINGLE PU – SINGLE DATABASE : One service → one database

```application.properties
spring.datasource.url=jdbc:postgresql://user-db:5432/userdb
spring.datasource.username=user_app
spring.datasource.password=********
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

What happens
* One implicit Persistence Unit is created
* One EntityManagerFactory is created
* One JpaTransactionManager is created
* All repositories use the same EM and TM

MULTIPLE PU – MULTIPLE DATABASES (NO DISTRIBUTED TRANSACTIONS) : One service → multiple databases, transactions do NOT span databases. Example : Reporting service, Orders DB (MySQL), Analytics DB (Postgres)
```text
            +------+   +------+   +------+   +----------+
            | PU1  | → | EMF1 | → | TM1  | → |   H2 DB  |
            +------+   +------+   +------+   +----------+
            
            +------+   +------+   +------+   +-----------+
            | PU2  | → | EMF2 | → | TM2  | → | MySQL DB  |
            +------+   +------+   +------+   +-----------+
```
```application.properties
# Orders DB
spring.datasource.orders.url=jdbc:mysql://orders-db:3306/orders
spring.datasource.orders.username=orders_app
spring.datasource.orders.password=********
spring.datasource.orders.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.orders.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.orders.hibernate.ddl-auto=validate


# Analytics DB
spring.datasource.analytics.url=jdbc:postgresql://analytics-db:5432/analytics
spring.datasource.analytics.username=analytics_app
spring.datasource.analytics.password=********
spring.datasource.analytics.driver-class-name=org.postgresql.Driver

spring.jpa.analytics.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.analytics.hibernate.ddl-auto=validate
```
```java
@Configuration
@EnableJpaRepositories(
        basePackages = "com.app.orders",
        entityManagerFactoryRef = "ordersEmf",
        transactionManagerRef = "ordersTm"
)
public class OrdersJpaConfig {
}

@Configuration
@EnableJpaRepositories(
        basePackages = "com.app.analytics",
        entityManagerFactoryRef = "analyticsEmf",
        transactionManagerRef = "analyticsTm"
)
public class AnalyticsJpaConfig {
}
```
For each PU, you must also define: DataSource bean, EntityManagerFactory bean, TransactionManager bean. Example (orders):
```java
@Bean
public LocalContainerEntityManagerFactoryBean ordersEmf() {  }

@Bean
public PlatformTransactionManager ordersTm() {  }
```
Same for analytics.

What happens
* PU1 → Orders entities → Orders EMF → Orders TM → MySQL
* PU2 → Analytics entities → Analytics EMF → Analytics TM → Postgres
* Transactions are isolated per DB

MULTIPLE PU – SAME DATABASE (DIFFERENT SCHEMAS) : Same DB instance, different schemas / bounded contexts. Example : Auth schema + Billing schema in same Postgres DB
```application.properties
spring.datasource.url=jdbc:postgresql://main-db:5432/appdb
spring.datasource.username=app_user
spring.datasource.password=********
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate

spring.jpa.properties.hibernate.default_schema=auth
```

Second PU config (Java-based, common in practice)
```java
@Bean
public LocalContainerEntityManagerFactoryBean billingEmf() {
    Map<String, Object> props = new HashMap<>();
    props.put("hibernate.default_schema", "billing");
    //...
}
```
What happens
* PU1 → auth schema entities
* PU2 → billing schema entities
* Same physical DB, different logical ownership

MULTIPLE PU + SINGLE JTA TRANSACTION MANAGER (DISTRIBUTED TX) : Strong consistency across multiple databases, Legacy enterprise systems (banking, payments). Example : Oracle (accounts) + MySQL (ledger)
```text
            +------+   +------+   +--------------+
            | PU1  | → | EMF1 | → | XA DataSource| --┐
            +------+   +------+   +--------------+   |
                                                       |
                                                       v
                                                  +------------------+
                                                  |   JTA TM (2PC)   |
                                                  +------------------+
                                                       ^
                                                       |
            +------+   +------+   +--------------+   |
            | PU2  | → | EMF2 | → | XA DataSource| --┘
            +------+   +------+   +--------------+
                             |
                             +--> H2 DB, MySQL DB
```
```application.properties
spring.jta.enabled=true

# Oracle XA
spring.datasource.oracle.xa.data-source-class-name=oracle.jdbc.xa.client.OracleXADataSource
spring.datasource.oracle.xa.properties.url=jdbc:oracle:thin:@oracle-db:1521/ORCL
spring.datasource.oracle.xa.properties.user=acct_user
spring.datasource.oracle.xa.properties.password=********

# MySQL XA
spring.datasource.mysql.xa.data-source-class-name=com.mysql.cj.jdbc.MysqlXADataSource
spring.datasource.mysql.xa.properties.url=jdbc:mysql://mysql-db:3306/ledger
spring.datasource.mysql.xa.properties.user=ledger_user
spring.datasource.mysql.xa.properties.password=********
```
JPA properties
```properties
spring.jpa.properties.hibernate.transaction.jta.platform=org.hibernate.engine.transaction.jta.platform.internal.NarayanaJtaPlatform
spring.jpa.hibernate.ddl-auto=validate
```
What happens
* PU1 → EMF1 → XA Oracle
* PU2 → EMF2 → XA MySQL
* ONE JTA TransactionManager
* 2-phase commit at runtime

MICROSERVICES (MODERN DEFAULT) : Each service has its own application.properties
Order Service
```properties
spring.datasource.url=jdbc:mysql://orders-db:3306/orders
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```
Payment Service
```properties
spring.datasource.url=jdbc:postgresql://payments-db:5432/payments
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```
What happens
* Each service has exactly one PU
* No JTA
* No distributed transactions
* Consistency handled via events / messaging

28. Entity Life Cycle. It is managed in PC. When we issue a command such as save() or delete(), at first it is stored in managed persistence. At some point when commit(flush()) command is issued then only the entities gets save in DB.
![Entity-Lifecycle](src/main/resources/images/Entity-Lifecycle.png)

29. EntityManagerFactory -> It does expensive boot-time work:
    - scans all entities (@Entity classes)
    - builds ORM metadata (mapping, annotations, table info, joins…)
    - builds metamodel
    - initializes caches
    - validates DDL mapping (optional)
    - sets up connection pools (via DataSource)
    - initializes Hibernate SessionFactory
    - initializes dialect
    - initializes batch settings, query parser, 2nd level cache providers
    
If we try to create EM directly, for each request we would be scanning all entity, dialect init, cache init. It would take a lot of time. EMF amortizes heavy bootstrap costs to application startup once and lets you create cheap EM instances on demand. EM are then created per request/transaction and are lightweight units of work. They are not partitioned by entity type; each EM can manage all entities defined in the persistence unit. This work may take hundreds of milliseconds to seconds for large models. We inject repositories instead of EMF because repositories provide a clean domain-level data access abstraction. EMF and EM are low-level persistence APIs designed for bootstrapping and unit-of-work management. Repositories add higher-level capabilities such as query derivation, pagination, projections, exception translation, transaction integration, and testability, which would make the service layer complex and tightly coupled to persistence if we used EMF directly.
```text
                                200 entities
                                     ↓
                                    1 PU
                                     ↓
                                   1 EMF
                                     ↓
                [Request 1] → EM1 → can manage all 200 entities
                [Request 2] → EM2 → can manage all 200 entities
                [Request 3] → EM3 → can manage all 200 entities
```
EntityManager represents :
  - a persistence context
  - a unit of work
  - connected to a transaction boundary

It manages :
  - 1st level cache
  - dirty checking
  - flush/commit
  - lifecycle of entities (managed/detached)
  - lazy loading proxies

And it is created per :
  - request
  - transaction
  - manually

Code comparison
```java
@Autowired
EntityManagerFactory emf;

public User getUser(Long id) {
    EntityManager em = emf.createEntityManager();
    em.getTransaction().begin();
    User user = em.find(User.class, id);
    em.getTransaction().commit();
    em.close();
    return user;
}/*
Every operation requires:
    - transaction start/commit
    - Try/Catch rollback
    - EM closing
    - Exception translation
    - Flush handling
    - Mapping logic
This becomes unmaintainable in a big system. */
// Using Repository
@Service
public class UserService {
    @Autowired UserRepository userRepo;

    public User getUser(Long id) {
        return userRepo.findById(id).orElseThrow(Exception);
    }
}/*
Plus you get:
    - Transactions managed automatically
    - Queries abstracted
    - EM lifecycle hidden
    - Exception translation handled
    - Pagination and projections
    - Query derivation
    - Testability
 */
```
30. First Level Caching - Done at persistence context layer. Can be seen in cases when we save the user and then fetches it again. Not in all cases we immediately write to DB, in that case whatever we save/update/read is served from PC only i.e., Level 1 cache. Internally hashmap is used for keeping data in PC.

31. Second Level Caching (L2 caching)

![Second-Level-Caching](src/main/resources/images/Second-Level-Caching.png)

To use this, we have to add dependency in pom.xml(ehcache, hibernate-jcache, cache-api), enable it via application.properties and then in entity like below
```application.properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
spring.jpa.properties.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider
logging.level.org.hibernate.cache.spi=DEBUG
```
```java
import jakarta.persistence.Entity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "userDetailsCache")
public class UserDetails { }
```
- ehcache -> provides core implementation for second level caching
- hibernate-jcache -> hibernate specific caching logic, like when we use @Cache and CacheConcurrencyStrategy, so specific logic needs to be executed
- cache-api -> provides interface for Jcache, hibernate interact with these APIs.
```text
                                 Hibernate
                                     ↓
                              Hibernate JCache
                                     ↓
                            JCache api interfaces (exposes all APIs)
                            ↓         ↓         ↓
                          Ehcache  Caffeine  Hazelcast (API implementations)
```
33. Region -> Helps in logical grouping of cached data, provide granular level management of cached data. Each region can have different config for :
    - Eviction policy (LIFO/FIFO etc.)
    - TTL
    - Cache size
    - Concurrency strategy

All these information can be put into ehcache.xml

![ehcache](src/main/resources/images/ehcache.png)

34. CacheConcurrencyStrategy guides how operations like insert/update/delete impact the cache data. Types of concurrency strategies :
    - CacheConcurrencyStrategy.READ_ONLY (good for static data which is never updated)
    - CacheConcurrencyStrategy.READ_WRITE (shared lock during read, exclusive lock during write, updates cache at the same time)
    - CacheConcurrencyStrategy.NONSTRICT_READ_WRITE (No lock during read, for write it put a lock, update DB, if txn is successful then it invalidates tha cache, release the lock, most suited for Read heavy systems, might serve stale data)
    - CacheConcurrencyStrategy.TRANSACTIONAL (acquire both read and write lock, updates cache after successful commit, any read during lock goes to DB, any write waits in queue)

CacheConcurrencyStrategy.READ_WRITE

![CacheConcurrencyStrategy.READ_WRITE](src/main/resources/images/CacheConcurrency-READ-WRITE.png)

35. JPA Annotations
    - @Entity // we should not mark entity classes as final because most ORMs like Hibernate rely on runtime proxy classes for operations like lazy loading to loads the actual data.
    - @Table
    - @Column
    - @Id -> for PK
    - @Embeddable, @EmbeddedId -> for composite key
    - @IdClass -> for composite key
    - @GeneratedValue
    - @SequenceGenerator -> it can pregenerate some N values and is cached at hibernate end, no DB call, directly next value can be fetched and used.

```java
import jakarta.persistence.*;
import java.io.Serializable;
@Table(name = "User",
        schema = "Onboarding",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "email"}) // composite key constraints, both combined they should be unique
        },
        indexes = {
                @Index(name = "index_email", columnList = "email") // index on single column, we can also have composite index
        })
@Entity
public class User {
    @Id // unique, non-null, primary-key, applicable for single column
    // @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment, applicable for single column, IDENTITY is DB specific, we can also use sequence.
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unique_user_seq")
    @SequenceGenerator(name = "unique_user_seq", sequenceName = "db_seq_name", initialValue = 100, allocationSize = 5) // when this will start, db will fetch 5 values from db_seq_name and will store in hibernate, only when the 6th entry is made next set of sequence are fetched. Sequence is an atomic counter, no table is involved. But if something crashes, it looses all prefetch values
    private long id;
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;
    private String firstName;
    private String lastName;
}
// for composite key, we can use annotations as below
public class ProductDetailsCK implements Serializable {
    private long id;
    private String name;

    public ProductDetailsCK() {}

    @Override
    public int hashCode() {
        // self implementation
    }

    @Override
    public boolean equals(Object obj) {
        // self implementation
    }
}
// Approach 1 -> Product table has composite key
@Entity
@IdClass(ProductDetailsCK.class)
public class Product {
    @Id
    private long id;
    @Id
    private String productName;
    private String makerName;
}

// Approach 2 -> mark ProductDetailsCK as @Embeddable then
@Entity
@IdClass(ProductDetailsCK.class)
public class Product {
    
    @EmbeddedId
    private ProductDetailsCK productDetailsCK;
    private String makerName;
}
```
36. Entity Mapping -> We have several entity in our application and sometimes there exist some relationship between them. These relationship is hold at database layer e.g., one upi id can be linked to one bank only. OneToOne or OneToMany are few examples of such relationship. Apart from this, we should also make sure that how these entity exist, any update in parent entity must also cascade to child entity. Without cascade type any operation on parent do not affect child entities. All of these are managed by annotations in data JPA. OneToOne mapping looks like below :
```java
import jakarta.persistence.*;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne(cascade = CascadeType.ALL)
    private PassportDetails passportDetails;
    private String email;
}
@Entity
public class PassportDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String email;
    private String authorisedTo;
    private String issuerCountry;
    private Date issuedOn;
    private Date validTill;
}
```
What happens internally, in the table User, it creates a foreign key which is PK of another table. How internally table will look like is User --> {id,passport_details_id(FK),email} and PassportDetails --> {id, email, authorisedTo, issuerCountry, issuedOn, validTill}. But if we need more control, we can use @JoinColumn.
```java
import jakarta.persistence.JoinColumn;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "passport_id", referencedColumnName = "id")
    private PassportDetails passportDetails;
}
```
In this case the table for user will look like User --> {id,passport_id(FK)}. In case of composite key, we need to map all columns as shown below :
```java
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;

@Embeddable
public class PassportDetailsCK implements Serializable {
    private long id;
    private String email;
    private String authorisedTo;

    public ProductDetailsCK() {
    }

    @Override
    public int hashCode() {
        // self implementation
    }

    @Override
    public boolean equals(Object obj) {
        // self implementation
    }
}
@Entity
public class PassportDetails {
    @EmbeddedId
    private PassportDetailsCK passportDetailsCK;
    private String issuerCountry;
    private Date issuedOn;
    private Date validTill;
}
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumns({
            @JoinColumn(name = "passport_id", referencedColumnName = "id"), // these are all FK in User table
            @JoinColumn(name = "passport_email", referencedColumnName = "email"),
            @JoinColumn(name = "passport_authorisedTo", referencedColumnName = "authorisedTo")
    })
    private PassportDetails passportDetails;
}
```
In this case the table for user will look like User --> {id,passport_id(FK),passport_email(FK),passport_authorisedTo(FK)}.

37. Cascade types :
    - CascadeType.PERSIST : Inserting parent automatically insert the child entities, only effecting during first time insert.
    - CascadeType.MERGE : Updating parent entity updates child entity too. We can pass multiple cascadeType as -> @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    - CascadeType.REMOVE : Deleting parent entity deletes the child entity also.
    - CascadeType.REFRESH : If we use this parent as well as child entity are loaded directly from DB, not PC is involved.
    - CascadeType.ALL : Capability of all types.


38. Lazy and Eager loading : Child entities are loaded either at the same time, or on use basis. We can configure this as well. Eager loading is default for @OneToOne and @ManyToOne. Generally it does a left join on parent entity and extract the child entity details. Eager loading is default for @OneToMany and @ManyToMany. We can add required config for this as shown below :
```java
@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
public PassportDetails passportDetails;
```
39. 
