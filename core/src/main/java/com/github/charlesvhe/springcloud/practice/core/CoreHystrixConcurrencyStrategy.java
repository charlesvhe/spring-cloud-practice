package com.github.charlesvhe.springcloud.practice.core;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public class CoreHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CoreHystrixConcurrencyStrategy.class);

    private HystrixConcurrencyStrategy delegate;

    public CoreHystrixConcurrencyStrategy() {
        try {
            this.delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();
            if (this.delegate instanceof CoreHystrixConcurrencyStrategy) {
                // Welcome to singleton hell...
                return;
            }
            HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins.getInstance().getCommandExecutionHook();
            HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
            HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
            HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance().getPropertiesStrategy();
            logCurrentStateOfHysrixPlugins(eventNotifier, metricsPublisher, propertiesStrategy);
            HystrixPlugins.reset();
            HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
            HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
            HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
            HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
            HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
        } catch (Exception e) {
            logger.error("Failed to register Sleuth Hystrix Concurrency Strategy", e);
        }
    }

    private void logCurrentStateOfHysrixPlugins(HystrixEventNotifier eventNotifier,
                                                HystrixMetricsPublisher metricsPublisher,
                                                HystrixPropertiesStrategy propertiesStrategy) {
        if (logger.isDebugEnabled()) {
            logger.debug("Current Hystrix plugins configuration is [" + "concurrencyStrategy ["
                    + this.delegate + "]," + "eventNotifier [" + eventNotifier + "],"
                    + "metricPublisher [" + metricsPublisher + "]," + "propertiesStrategy ["
                    + propertiesStrategy + "]," + "]");
            logger.debug("Registering Sleuth Hystrix Concurrency Strategy.");
        }
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        if (callable instanceof CoreCallable) {
            return callable;
        } else {
            Callable<T> wrappedCallable = this.delegate != null ? this.delegate.wrapCallable(callable) : callable;
            if (wrappedCallable instanceof CoreCallable) {
                return wrappedCallable;
            }
            return new CoreCallable(wrappedCallable, CoreHeaderInterceptor.label.get());
        }
    }

    static class CoreCallable<T> implements Callable<T> {
        private final Callable<T> delegate;
        private final List<String> labels;

        /**
         * 传递ThreadLocal线程变量
         */
        public CoreCallable(Callable<T> callable, List<String> labels) {
            this.delegate = callable;
            this.labels = labels;
        }

        @Override
        public T call() throws Exception {
            try {
                CoreHeaderInterceptor.label.set(labels);
                return delegate.call();
            } finally {
                CoreHeaderInterceptor.label.set(null);
            }
        }
    }

}
