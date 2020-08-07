package org.sdrc.fani;

import org.sdrc.fani.rabbitmq.CollectionChannel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = { "org.sdrc.fani", "in.co.sdrc.sdrcdatacollector", "org.sdrc.usermgmt.core" })
@EnableMongoRepositories(basePackages = { "in.co.sdrc.sdrcdatacollector.mongorepositories",
		"org.sdrc.fani.repositories" })
@PropertySource(value = { "classpath:fani_notification.properties", "classpath:messages.properties" })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@EnableBinding(CollectionChannel.class)
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class FaniApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(FaniApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(FaniApplication.class);
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:springsecuritymessages");
		messageSource.setCacheSeconds(10); // reload messages every 10 seconds
		return messageSource; // return messageSource;
	}

	@Bean
    public CacheManager cacheManager() {
       return new EhCacheCacheManager(cacheManagerFactory().getObject());
    }

	public EhCacheManagerFactoryBean cacheManagerFactory() {
		EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
		bean.setConfigLocation(new ClassPathResource("ehcache.xml"));
		bean.setShared(true);
		return bean;
	}
}
