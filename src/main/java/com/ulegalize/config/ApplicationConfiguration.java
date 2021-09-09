package com.ulegalize.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public HttpFirewall allowUrlEncodedPercentHttpFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowUrlEncodedPercent(true);
    firewall.setAllowUrlEncodedSlash(true);
    return firewall;
  }
}
