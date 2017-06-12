package org.springframework.cloud.netflix.ribbon;

import com.netflix.loadbalancer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 修正spring cloud不支持ribbon全局设置 https://github.com/spring-cloud/spring-cloud-netflix/issues/1741
 * Created by charles on 2017/5/22.
 */
public class DefaultPropertiesFactory extends PropertiesFactory {
    @Autowired
    private Environment environment;
    private Map<Class, String> defaultClassToProperty = new HashMap<>();

    public DefaultPropertiesFactory() {
        super();
        defaultClassToProperty.put(ILoadBalancer.class, "NFLoadBalancerClassName");
        defaultClassToProperty.put(IPing.class, "NFLoadBalancerPingClassName");
        defaultClassToProperty.put(IRule.class, "NFLoadBalancerRuleClassName");
        defaultClassToProperty.put(ServerList.class, "NIWSServerListClassName");
        defaultClassToProperty.put(ServerListFilter.class, "NIWSServerListFilterClassName");
    }

    @Override
    public String getClassName(Class clazz, String name) {
        String className = super.getClassName(clazz, name);

        if (!StringUtils.hasText(className) && this.defaultClassToProperty.containsKey(clazz)) {
            String classNameProperty = this.defaultClassToProperty.get(clazz);
            className = this.environment.getProperty(SpringClientFactory.NAMESPACE + "." + classNameProperty);
        }
        return className;
    }
}
