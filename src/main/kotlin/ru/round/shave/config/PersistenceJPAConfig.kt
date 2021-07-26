package ru.round.shave.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.datasource.init.DataSourceInitializer
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.*
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource


@Configuration
@EnableTransactionManagement
open class PersistenceJPAConfig {

    @Bean
    open fun entityManagerFactory() = LocalContainerEntityManagerFactoryBean().apply {
        dataSource = dataSource()
        setPackagesToScan("ru.round.shave.entity")
        val vendorAdapter = HibernateJpaVendorAdapter()
        jpaVendorAdapter = vendorAdapter
        setJpaProperties(additionalProperties())
    }

    @Bean
    open fun dataSource() = DriverManagerDataSource().apply {
        setDriverClassName("com.mysql.cj.jdbc.Driver")
        url =
            "jdbc:mysql://localhost:3306/round_shave?allowPublicKeyRetrieval=true&autoReconnect=true&useSSL=false&useUnicode=true&character_set_server=utf8mb4&" +
                    "useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
        username = "root"
        password = "1"
    }

    /**
     * Used to prepopulate database
     */
    //@Bean
    open fun dataSourceInitializer(@Qualifier("dataSource") dataSource: DataSource): DataSourceInitializer {
        val resourceDatabasePopulator = ResourceDatabasePopulator()
        resourceDatabasePopulator.addScript(ClassPathResource("/data.sql"))
        val dataSourceInitializer = DataSourceInitializer()
        dataSourceInitializer.setDataSource(dataSource)
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator)
        return dataSourceInitializer
    }

    @Bean
    open fun transactionManager(emf: EntityManagerFactory) = JpaTransactionManager().apply {
        entityManagerFactory = emf
    }

    @Bean
    open fun exceptionTranslation(): PersistenceExceptionTranslationPostProcessor {
        return PersistenceExceptionTranslationPostProcessor()
    }

    private fun additionalProperties() = Properties().apply {
        setProperty("hibernate.hbm2ddl.auto", "update")
        setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect")
        setProperty("hibernate.dialect.storage_engine", "innodb")
        setProperty("hibernate.show_sql", "false")
    }
}
