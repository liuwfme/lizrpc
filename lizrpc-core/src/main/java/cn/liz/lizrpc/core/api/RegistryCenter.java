package cn.liz.lizrpc.core.api;

import cn.liz.lizrpc.core.registry.ChangedListener;

import java.util.List;

public interface RegistryCenter {

    void start();

    void stop();

    // provider
    void register(String service, String instance);

    void unregister(String service, String instance);

    // consumer
    List<String> fetchAll(String service);

    void subscribe(String service, ChangedListener listener);

    class StaticRegistryCenter implements RegistryCenter {

        List<String> providers;

        public StaticRegistryCenter(List<String> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(String service, String instance) {

        }

        @Override
        public void unregister(String service, String instance) {

        }

        @Override
        public List<String> fetchAll(String service) {
            return providers;
        }

        @Override
        public void subscribe(String service, ChangedListener listener) {

        }
    }
}