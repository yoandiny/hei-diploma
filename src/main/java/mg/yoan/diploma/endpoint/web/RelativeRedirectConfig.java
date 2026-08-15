package mg.yoan.diploma.endpoint.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.RelativeRedirectFilter;

@Configuration
public class RelativeRedirectConfig {

  @Bean
  public FilterRegistrationBean<RelativeRedirectFilter> relativeRedirectFilter() {
    FilterRegistrationBean<RelativeRedirectFilter> registration =
        new FilterRegistrationBean<>(new RelativeRedirectFilter());
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
