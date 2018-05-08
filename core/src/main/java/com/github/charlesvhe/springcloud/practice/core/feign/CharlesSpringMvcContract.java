package com.github.charlesvhe.springcloud.practice.core.feign;

import feign.MethodMetadata;
import feign.RequestTemplate;
import io.swagger.annotations.ApiImplicitParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharlesSpringMvcContract extends SpringMvcContract {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharlesSpringMvcContract.class);

    private Pattern pattern = Pattern.compile("(\\{[^}]+\\})");
    // MethodMetadata RequestTemplate 都是final无法优雅扩展 只能通过反射强行修改
    private Field requestTemplateUrl = ReflectionUtils.findField(RequestTemplate.class, "url");

    {
        requestTemplateUrl.setAccessible(true);
    }

    public CharlesSpringMvcContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors, ConversionService conversionService) {
        super(annotatedParameterProcessors, conversionService);
    }

    @Override
    public MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
        MethodMetadata methodMetadata = super.parseAndValidateMetadata(targetType, method);

        String rawUrl = methodMetadata.template().url();
        String url = rawUrl;
        List<String> pathVariableList = new ArrayList<>();
        // 处理path value含有正则问题
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            String pathVariable = matcher.group();
            int endIndex = pathVariable.indexOf(":");
            if (endIndex != -1) {
                String rawPathVariable = pathVariable.substring(1, endIndex);
                pathVariableList.add(rawPathVariable);
                url = url.replace(pathVariable, "{" + rawPathVariable + "}");
            } else {
                String rawPathVariable = pathVariable.substring(1, pathVariable.length() - 1);
                pathVariableList.add(rawPathVariable);
            }
        }

        // 处理path value不存在方法参数问题
        for (String pathVariable : pathVariableList) {
            if (!hasPathVariable(methodMetadata, pathVariable)) {
                url = url.replace("{" + pathVariable + "}", defaultValue(method, pathVariable));
                ReflectionUtils.setField(requestTemplateUrl, methodMetadata.template(), new StringBuilder(url));
            }
        }

        LOGGER.info("{} >>> {}", rawUrl, url);

        return methodMetadata;
    }

    private String defaultValue(Method method, String pathVariable) {
        Set<ApiImplicitParam> apiImplicitParams = AnnotatedElementUtils.findAllMergedAnnotations(method, ApiImplicitParam.class);
        for (ApiImplicitParam apiImplicitParam : apiImplicitParams) {
            if (pathVariable.equals(apiImplicitParam.name())) {
                return apiImplicitParam.allowableValues().split(",")[0].trim();
            }
        }
        throw new IllegalArgumentException("no default value for " + pathVariable);
    }

    private boolean hasPathVariable(MethodMetadata methodMetadata, String pathVariable) {
        for (Collection<String> names : methodMetadata.indexToName().values()) {
            if (names.contains(pathVariable)) {
                return true;
            }
        }
        return false;
    }
}
