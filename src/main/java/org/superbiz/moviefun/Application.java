package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public DatabaseServiceCredentials serviceCredentials() {
        return new DatabaseServiceCredentials(System.getenv("VCAP_SERVICES"));
    }
    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) throws SQLException {
        HikariDataSource hikariDataSource = new HikariDataSource();
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));
        hikariDataSource.isWrapperFor(DataSource.class);
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) throws SQLException {
        HikariDataSource hikariDataSource = new HikariDataSource();
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql", "p-mysql"));
        hikariDataSource.isWrapperFor(DataSource.class);
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBeanForAlbums(DataSource albumsDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(albumsDataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("albums");
        return  localContainerEntityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBeanForMovies(DataSource moviesDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(moviesDataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.movies");
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("movies");
        return  localContainerEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManagerForAlbums(EntityManagerFactory localContainerEntityManagerFactoryBeanForAlbums) {
        PlatformTransactionManager transactionManager = new JpaTransactionManager();
        ((JpaTransactionManager) transactionManager).setEntityManagerFactory(localContainerEntityManagerFactoryBeanForAlbums);
        return transactionManager;
    }

    @Bean
    public PlatformTransactionManager transactionManagerForMovies(EntityManagerFactory localContainerEntityManagerFactoryBeanForMovies) {
        PlatformTransactionManager transactionManager = new JpaTransactionManager();
        ((JpaTransactionManager) transactionManager).setEntityManagerFactory(localContainerEntityManagerFactoryBeanForMovies);
        return transactionManager;
    }

    @Bean
    public TransactionOperations transactionOperationsForAlbums(PlatformTransactionManager transactionManagerForAlbums){
        TransactionOperations transactionOperations =  new TransactionTemplate();
        ((TransactionTemplate) transactionOperations).setTransactionManager(transactionManagerForAlbums);
        return transactionOperations;
    }

    @Bean
    public TransactionOperations transactionOperationsForMovies(PlatformTransactionManager transactionManagerForMovies){
        TransactionOperations transactionOperations =  new TransactionTemplate();
        ((TransactionTemplate) transactionOperations).setTransactionManager(transactionManagerForMovies);
        return transactionOperations;
    }
}
