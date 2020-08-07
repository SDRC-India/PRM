package org.sdrc.pmr;

import org.sdrc.pmr.rabbitmq.CollectionChannel;
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
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableMongoRepositories(basePackages = { "in.co.sdrc.sdrcdatacollector.mongorepositories", "org.sdrc.pmr.repository","org.sdrc.usermgmt.mongodb.repository" })
@ComponentScan(basePackages = { "org.sdrc.pmr", "in.co.sdrc.sdrcdatacollector", "org.sdrc.usermgmt.core" })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@EnableResourceServer
@EnableBinding(CollectionChannel.class)
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class PmrReportingApplication extends SpringBootServletInitializer {

	  // Determines the timeout in milliseconds until a connection is established.
    private static final int CONNECT_TIMEOUT = 10000;
     
    // The timeout when requesting a connection from the connection manager.
    private static final int REQUEST_TIMEOUT = 10000;
     
    // The timeout for waiting for data
    private static final int SOCKET_TIMEOUT = 10000;
    
	public static void main(String[] args) {
		SpringApplication.run(PmrReportingApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(PmrReportingApplication.class);
	}

//	@LoadBalanced
	@Bean
	public RestTemplate restTemplate() {
		  HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
	        httpRequestFactory.setConnectionRequestTimeout(REQUEST_TIMEOUT);
	        httpRequestFactory.setConnectTimeout(CONNECT_TIMEOUT);
	        httpRequestFactory.setReadTimeout(SOCKET_TIMEOUT);
		return new RestTemplate(httpRequestFactory);
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
