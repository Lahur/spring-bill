package hr.bill.spring_bill.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.openfeign.FeignClientSpecification;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring-cloud-openfeign-core 5.0.2's {@link FeignHttpMessageConverters#initConvertersIfRequired()}
 * is not thread-safe: it publishes an empty converters list before populating it. Two concurrent
 * first calls to a Feign client can race and see the still-empty list, failing to encode the
 * request body. Eagerly resolving the converters for every Feign client's default child context
 * during singleton initialization (single-threaded) forces that one-time init to complete before
 * any real request can race it.
 */
@Configuration(proxyBeanMethods = false)
class FeignHttpMessageConvertersWorkaroundConfiguration {

    @Bean
    FeignClientSpecification feignHttpMessageConvertersWorkaroundSpecification() {
        return new FeignClientSpecification(
                "default." + FeignHttpMessageConvertersWorkaroundConfiguration.class.getName(),
                "default",
                new Class<?>[] { FeignChildConfiguration.class });
    }

    @Configuration(proxyBeanMethods = false)
    static class FeignChildConfiguration {

        @Bean
        SmartInitializingSingleton feignHttpMessageConvertersInitializer(
                ObjectProvider<FeignHttpMessageConverters> converters) {
            return () -> converters.ifAvailable(FeignHttpMessageConverters::getConverters);
        }
    }
}
