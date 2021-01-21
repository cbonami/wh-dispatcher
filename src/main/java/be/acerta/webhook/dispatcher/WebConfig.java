package be.acerta.webhook.dispatcher;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class WebConfig {

    /**
     * Supply X-FORWARDED-* HEADERS.
     * Replace the host, port, protocol, etc. with those of the Edge server.
     * 
     * @return
     */
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> result = new FilterRegistrationBean<>();
        result.setFilter(new ForwardedHeaderFilter());
        result.setOrder(0);
        return result;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // https://www.baeldung.com/spring-boot-actuators

    /*
     * @Bean public SecurityWebFilterChain securityWebFilterChain(
     * ServerHttpSecurity http) { return http.authorizeExchange()
     * .pathMatchers("/actuator/**").permitAll() .anyExchange().authenticated()
     * .and().build(); }
     */

}
