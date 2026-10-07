package hr.bill.spring_bill.config;

import feign.MethodMetadata;
import feign.QueryMap;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * {@link org.springframework.cloud.openfeign.support.SpringMvcContract} only recognizes
 * {@code @SpringQueryMap}; a parameter annotated with Feign's own {@link QueryMap} would otherwise
 * be treated as the request body. Registering this processor makes the Spring contract honor
 * {@link QueryMap} the same way.
 */
@Component
class FeignQueryMapParameterProcessor implements AnnotatedParameterProcessor {

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return QueryMap.class;
    }

    @Override
    public boolean processArgument(AnnotatedParameterContext context, Annotation annotation, Method method) {
        MethodMetadata metadata = context.getMethodMetadata();
        if (metadata.queryMapIndex() == null) {
            metadata.queryMapIndex(context.getParameterIndex());
        }
        return true;
    }
}
