package be.acerta.webhook.dispatcher;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

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


    // https://www.novatec-gmbh.de/en/blog/including-hal-browser-spring-boot-without-using-spring-data-rest/
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/browser/**")) {
            registry.addResourceHandler("/browser/**").addResourceLocations("classpath:/META-INF/resources/webjars/hal-browser/ad9b865/");
        }
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
