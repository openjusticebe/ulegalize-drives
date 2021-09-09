package com.ulegalize.config;


import lombok.extern.slf4j.Slf4j;

//@Configuration
@Slf4j
public class DataSourceConfig {

//    @Value("${app.datasource.driverClassName}")
//    String driverClassName;
//    @Value("${app.datasource.url}")
//    String url;
//    @Value("${app.datasource.username}")
//    String username;
//    @Value("${app.datasource.password}")
//    String password;


//    @Bean
//    @Primary
//    @Profile("dev")
//    public DataSource devGetDataSource() {
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder.driverClassName(driverClassName);
//        dataSourceBuilder.url(url);
//        dataSourceBuilder.username(username);
//        dataSourceBuilder.password(password);
//        return dataSourceBuilder.build();
//    }
//
//    @Bean
//    @Primary
//    @Profile("prod")
//    public DataSource prodGetDataSource() {
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder.driverClassName(driverClassName);
//        dataSourceBuilder.url(url);
//        dataSourceBuilder.username(username);
//        dataSourceBuilder.password(password);
//        return dataSourceBuilder.build();
//    }

//    @Bean
//    public RestTemplate getRestTemplate() {
//        return new RestTemplate();
//    }

}