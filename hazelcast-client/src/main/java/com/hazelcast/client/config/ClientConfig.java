/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.client.config;

import com.hazelcast.client.LoadBalancer;
import com.hazelcast.config.ConfigPatternMatcher;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.QueryCacheConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SocketInterceptorConfig;
import com.hazelcast.config.matcher.MatchingPointConfigPatternMatcher;
import com.hazelcast.core.ManagedContext;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.security.Credentials;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.hazelcast.config.NearCacheConfigAccessor.initDefaultMaxSizeForOnHeapMaps;
import static com.hazelcast.util.Preconditions.checkFalse;

/**
 * Main configuration to setup a Hazelcast Client
 */
public class ClientConfig {

    private static final ILogger LOGGER = Logger.getLogger(ClientConfig.class);

    /**
     * To pass properties
     */
    private Properties properties = new Properties();

    /**
     * The Group Configuration properties like:
     * Name and Password that is used to connect to the cluster.
     */
    private GroupConfig groupConfig = new GroupConfig();

    /**
     * The Security Configuration for custom Credentials:
     * Name and Password that is used to connect to the cluster.
     */
    private ClientSecurityConfig securityConfig = new ClientSecurityConfig();

    /**
     * The Network Configuration properties like:
     * addresses to connect, smart-routing, socket-options...
     */
    private ClientNetworkConfig networkConfig = new ClientNetworkConfig();

    /**
     * Used to distribute the operations to multiple Endpoints.
     */
    private LoadBalancer loadBalancer;

    /**
     * List of listeners that Hazelcast will automatically add as a part of initialization process.
     * Currently only supports {@link com.hazelcast.core.LifecycleListener}.
     */
    private List<ListenerConfig> listenerConfigs = new LinkedList<ListenerConfig>();

    /**
     * pool-size for internal ExecutorService which handles responses etc.
     */
    private int executorPoolSize = -1;

    private String instanceName;

    private ConfigPatternMatcher configPatternMatcher = new MatchingPointConfigPatternMatcher();

    private Map<String, NearCacheConfig> nearCacheConfigMap = new ConcurrentHashMap<String, NearCacheConfig>();

    private Map<String, ClientReliableTopicConfig> reliableTopicConfigMap
            = new ConcurrentHashMap<String, ClientReliableTopicConfig>();

    private Map<String, Map<String, QueryCacheConfig>> queryCacheConfigs;

    private SerializationConfig serializationConfig = new SerializationConfig();

    private NativeMemoryConfig nativeMemoryConfig = new NativeMemoryConfig();

    private List<ProxyFactoryConfig> proxyFactoryConfigs = new LinkedList<ProxyFactoryConfig>();

    private ManagedContext managedContext;

    private ClassLoader classLoader;

    private String licenseKey;

    private ClientConnectionStrategyConfig connectionStrategyConfig = new ClientConnectionStrategyConfig();

    private ClientUserCodeDeploymentConfig userCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();

    public void setConfigPatternMatcher(ConfigPatternMatcher configPatternMatcher) {
        if (configPatternMatcher == null) {
            throw new IllegalArgumentException("ConfigPatternMatcher is not allowed to be null!");
        }
        this.configPatternMatcher = configPatternMatcher;
    }

    /**
     * Gets a named property already set or from system properties if not exists.
     *
     * @param name property name
     * @return value of the property
     */
    public String getProperty(String name) {
        String value = properties.getProperty(name);
        return value != null ? value : System.getProperty(name);
    }

    /**
     * Sets the value of a named property.
     *
     * @param name  property name
     * @param value value of the property
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    public ClientConfig setProperty(String name, String value) {
        properties.put(name, value);
        return this;
    }

    /**
     * Gets {@link java.util.Properties} object
     *
     * @return {@link java.util.Properties} object
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * sets all properties
     *
     * @param properties {@link java.util.Properties} object
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    public ClientConfig setProperties(final Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Gets {@link com.hazelcast.client.config.ClientSecurityConfig} object
     *
     * @return {@link com.hazelcast.client.config.ClientSecurityConfig}
     * @see com.hazelcast.client.config.ClientSecurityConfig
     */
    public ClientSecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    /**
     * Sets {@link com.hazelcast.client.config.ClientSecurityConfig} object
     *
     * @param securityConfig {@link com.hazelcast.client.config.ClientSecurityConfig} to be set
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @see com.hazelcast.client.config.ClientSecurityConfig
     */
    public ClientConfig setSecurityConfig(ClientSecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
        return this;
    }

    /**
     * Gets {@link com.hazelcast.client.config.ClientNetworkConfig}
     *
     * @return {@link com.hazelcast.client.config.ClientNetworkConfig}
     * @see com.hazelcast.client.config.ClientNetworkConfig
     */
    public ClientNetworkConfig getNetworkConfig() {
        return networkConfig;
    }

    /**
     * Sets {@link com.hazelcast.client.config.ClientNetworkConfig}
     *
     * @param networkConfig {@link com.hazelcast.client.config.ClientNetworkConfig} to be set
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @see com.hazelcast.client.config.ClientNetworkConfig
     */
    public ClientConfig setNetworkConfig(ClientNetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
        return this;
    }

    /**
     * Adds a ClientReliableTopicConfig.
     *
     * @param reliableTopicConfig the ClientReliableTopicConfig to add
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    public ClientConfig addReliableTopicConfig(ClientReliableTopicConfig reliableTopicConfig) {
        reliableTopicConfigMap.put(reliableTopicConfig.getName(), reliableTopicConfig);
        return this;
    }

    /**
     * Gets the ClientReliableTopicConfig for a given reliable topic name.
     *
     * @param name the name of the reliable topic
     * @return the found config. If none is found, a default configured one is returned.
     */
    public ClientReliableTopicConfig getReliableTopicConfig(String name) {
        ClientReliableTopicConfig reliableTopicConfig = lookupByPattern(reliableTopicConfigMap, name);
        if (reliableTopicConfig == null) {
            reliableTopicConfig = new ClientReliableTopicConfig(name);
            addReliableTopicConfig(reliableTopicConfig);
        }
        return reliableTopicConfig;
    }

    /**
     * please use {@link ClientConfig#addNearCacheConfig(NearCacheConfig)}
     *
     * @param name            name of the IMap / ICache that Near Cache config will be applied to
     * @param nearCacheConfig nearCacheConfig
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    @Deprecated
    public ClientConfig addNearCacheConfig(String name, NearCacheConfig nearCacheConfig) {
        nearCacheConfig.setName(name);
        return addNearCacheConfig(nearCacheConfig);
    }

    /**
     * Helper method to add a new NearCacheConfig
     *
     * @param nearCacheConfig {@link com.hazelcast.config.NearCacheConfig}
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @see com.hazelcast.config.NearCacheConfig
     */
    public ClientConfig addNearCacheConfig(NearCacheConfig nearCacheConfig) {
        nearCacheConfigMap.put(nearCacheConfig.getName(), nearCacheConfig);
        return this;
    }

    /**
     * Helper method to add a new ListenerConfig
     *
     * @param listenerConfig ListenerConfig
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @see com.hazelcast.config.ListenerConfig
     */
    public ClientConfig addListenerConfig(ListenerConfig listenerConfig) {
        getListenerConfigs().add(listenerConfig);
        return this;
    }

    /**
     * Helper method to add a new {@link ProxyFactoryConfig}
     *
     * @param proxyFactoryConfig {@link ProxyFactoryConfig}
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @see com.hazelcast.client.config.ProxyFactoryConfig
     */
    public ClientConfig addProxyFactoryConfig(ProxyFactoryConfig proxyFactoryConfig) {
        this.proxyFactoryConfigs.add(proxyFactoryConfig);
        return this;
    }

    /**
     * Gets the {@link NearCacheConfig} configured for the map / cache with name
     *
     * @param name name of the map / cache
     * @return Configured {@link NearCacheConfig}
     * @see com.hazelcast.config.NearCacheConfig
     */
    public NearCacheConfig getNearCacheConfig(String name) {
        NearCacheConfig nearCacheConfig = lookupByPattern(nearCacheConfigMap, name);
        if (nearCacheConfig == null) {
            nearCacheConfig = nearCacheConfigMap.get("default");
            if (nearCacheConfig != null) {
                // if there is a default config we have to clone it,
                // otherwise you will modify the same instances via different Near Cache names
                nearCacheConfig = new NearCacheConfig(nearCacheConfig);
            }
        }
        initDefaultMaxSizeForOnHeapMaps(nearCacheConfig);
        return nearCacheConfig;
    }

    /**
     * Map of all configured NearCacheConfig's with the name key and configuration as the value
     *
     * @return map of NearCacheConfig
     * @see com.hazelcast.config.NearCacheConfig
     */
    public Map<String, NearCacheConfig> getNearCacheConfigMap() {
        return nearCacheConfigMap;
    }

    /**
     * Sets all {@link NearCacheConfig}'s with the provided map
     *
     * @param nearCacheConfigMap map of (name, {@link NearCacheConfig})
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    public ClientConfig setNearCacheConfigMap(Map<String, NearCacheConfig> nearCacheConfigMap) {
        this.nearCacheConfigMap = nearCacheConfigMap;
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#isSmartRouting} instead
     */
    @Deprecated
    public boolean isSmartRouting() {
        return networkConfig.isSmartRouting();
    }

    /**
     * Use {@link ClientNetworkConfig#setSmartRouting} instead
     */
    @Deprecated
    public ClientConfig setSmartRouting(boolean smartRouting) {
        networkConfig.setSmartRouting(smartRouting);
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#getSocketInterceptorConfig} instead
     */
    @Deprecated
    public SocketInterceptorConfig getSocketInterceptorConfig() {
        return networkConfig.getSocketInterceptorConfig();
    }

    /**
     * Use {@link ClientNetworkConfig#setSocketInterceptorConfig} instead
     */
    @Deprecated
    public ClientConfig setSocketInterceptorConfig(SocketInterceptorConfig socketInterceptorConfig) {
        networkConfig.setSocketInterceptorConfig(socketInterceptorConfig);
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#getConnectionAttemptPeriod} instead
     */
    @Deprecated
    public int getConnectionAttemptPeriod() {
        return networkConfig.getConnectionAttemptPeriod();
    }

    /**
     * Use {@link ClientNetworkConfig#setConnectionAttemptPeriod} instead
     */
    @Deprecated
    public ClientConfig setConnectionAttemptPeriod(int connectionAttemptPeriod) {
        networkConfig.setConnectionAttemptPeriod(connectionAttemptPeriod);
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#getConnectionAttemptLimit} instead
     */
    @Deprecated
    public int getConnectionAttemptLimit() {
        return networkConfig.getConnectionAttemptLimit();
    }

    /**
     * Use {@link ClientNetworkConfig#setConnectionAttemptLimit} instead
     */
    @Deprecated
    public ClientConfig setConnectionAttemptLimit(int connectionAttemptLimit) {
        networkConfig.setConnectionAttemptLimit(connectionAttemptLimit);
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#getConnectionTimeout} instead
     */
    @Deprecated
    public int getConnectionTimeout() {
        return networkConfig.getConnectionTimeout();
    }

    /**
     * Use {@link ClientNetworkConfig#setConnectionTimeout} instead
     */
    @Deprecated
    public ClientConfig setConnectionTimeout(int connectionTimeout) {
        networkConfig.setConnectionTimeout(connectionTimeout);
        return this;
    }

    /**
     * Gets {@link com.hazelcast.security.Credentials}
     *
     * @return {@link com.hazelcast.security.Credentials}
     * @see com.hazelcast.security.Credentials
     */
    public Credentials getCredentials() {
        return securityConfig.getCredentials();
    }

    /**
     * Sets {@link com.hazelcast.security.Credentials}
     *
     * @param credentials {@link com.hazelcast.security.Credentials}
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    public ClientConfig setCredentials(Credentials credentials) {
        securityConfig.setCredentials(credentials);
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#addAddress} instead
     */
    @Deprecated
    public ClientConfig addAddress(String... addresses) {
        networkConfig.addAddress(addresses);
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#setAddresses} instead
     */
    @Deprecated
    public ClientConfig setAddresses(List<String> addresses) {
        networkConfig.setAddresses(addresses);
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#getAddresses} instead
     */
    @Deprecated
    public List<String> getAddresses() {
        return networkConfig.getAddresses();
    }

    /**
     * gets {@link GroupConfig}
     *
     * @return {@link GroupConfig}
     * @see com.hazelcast.config.GroupConfig
     */
    public GroupConfig getGroupConfig() {
        return groupConfig;
    }

    /**
     * Sets {@link GroupConfig}
     *
     * @param groupConfig {@link GroupConfig}
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    public ClientConfig setGroupConfig(GroupConfig groupConfig) {
        this.groupConfig = groupConfig;
        return this;
    }

    /**
     * Gets list of all configured {@link ListenerConfig}'s
     *
     * @return {@link ListenerConfig}
     * @see com.hazelcast.config.ListenerConfig
     */
    public List<ListenerConfig> getListenerConfigs() {
        return listenerConfigs;
    }

    /**
     * Sets all {@link ListenerConfig}'s
     *
     * @param listenerConfigs list of {@link ListenerConfig}
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @see com.hazelcast.config.ListenerConfig
     */
    public ClientConfig setListenerConfigs(List<ListenerConfig> listenerConfigs) {
        this.listenerConfigs = listenerConfigs;
        return this;
    }

    /**
     * Gets LoadBalancer
     *
     * @return LoadBalancer
     * @see com.hazelcast.client.LoadBalancer
     */
    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    /**
     * Sets the {@link LoadBalancer}
     *
     * @param loadBalancer {@link LoadBalancer}
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @see com.hazelcast.client.LoadBalancer
     */
    public ClientConfig setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#isRedoOperation} instead
     */
    @Deprecated
    public boolean isRedoOperation() {
        return networkConfig.isRedoOperation();
    }

    /**
     * Use {@link ClientNetworkConfig#setRedoOperation} instead
     */
    @Deprecated
    public ClientConfig setRedoOperation(boolean redoOperation) {
        networkConfig.setRedoOperation(redoOperation);
        return this;
    }

    /**
     * Use {@link ClientNetworkConfig#getSocketOptions} instead
     */
    @Deprecated
    public SocketOptions getSocketOptions() {
        return networkConfig.getSocketOptions();
    }

    /**
     * Use {@link ClientNetworkConfig#setSocketOptions} instead
     */
    @Deprecated
    public ClientConfig setSocketOptions(SocketOptions socketOptions) {
        networkConfig.setSocketOptions(socketOptions);
        return this;
    }

    /**
     * Gets the classLoader
     *
     * @return configured classLoader, null if not yet configured
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the classLoader which is used by serialization and listener configuration
     *
     * @param classLoader
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    public ClientConfig setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * Gets {@link ManagedContext}
     *
     * @return {@link ManagedContext}
     * @see com.hazelcast.core.ManagedContext
     */
    public ManagedContext getManagedContext() {
        return managedContext;
    }

    /**
     * Sets {@link ManagedContext}
     *
     * @param managedContext {@link ManagedContext}
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @see com.hazelcast.core.ManagedContext
     */
    public ClientConfig setManagedContext(ManagedContext managedContext) {
        this.managedContext = managedContext;
        return this;
    }

    /**
     * Pool size for internal ExecutorService which handles responses etc.
     *
     * @return int Executor pool size.
     */
    public int getExecutorPoolSize() {
        return executorPoolSize;
    }

    /**
     * Sets Client side Executor pool size.
     *
     * @param executorPoolSize pool size
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    public ClientConfig setExecutorPoolSize(int executorPoolSize) {
        this.executorPoolSize = executorPoolSize;
        return this;
    }

    /**
     * Gets list of {@link com.hazelcast.client.config.ProxyFactoryConfig}
     *
     * @return list of {@link com.hazelcast.client.config.ProxyFactoryConfig}
     * @see com.hazelcast.client.config.ProxyFactoryConfig
     */
    public List<ProxyFactoryConfig> getProxyFactoryConfigs() {
        return proxyFactoryConfigs;
    }

    /**
     * Sets the {@link ProxyFactoryConfig}
     *
     * @param proxyFactoryConfigs list to assign
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     */
    public ClientConfig setProxyFactoryConfigs(List<ProxyFactoryConfig> proxyFactoryConfigs) {
        this.proxyFactoryConfigs = proxyFactoryConfigs;
        return this;
    }

    /**
     * Gets {@link com.hazelcast.config.SerializationConfig}
     *
     * @return SerializationConfig
     * @see com.hazelcast.config.SerializationConfig
     */
    public SerializationConfig getSerializationConfig() {
        return serializationConfig;
    }

    /**
     * Sets {@link com.hazelcast.config.SerializationConfig}
     *
     * @param serializationConfig SerializationConfig
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @see com.hazelcast.config.SerializationConfig
     */
    public ClientConfig setSerializationConfig(SerializationConfig serializationConfig) {
        this.serializationConfig = serializationConfig;
        return this;
    }

    public NativeMemoryConfig getNativeMemoryConfig() {
        return nativeMemoryConfig;
    }

    public ClientConfig setNativeMemoryConfig(NativeMemoryConfig nativeMemoryConfig) {
        this.nativeMemoryConfig = nativeMemoryConfig;
        return this;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public ClientConfig setLicenseKey(final String licenseKey) {
        this.licenseKey = licenseKey;
        return this;
    }

    public ClientConfig addQueryCacheConfig(String mapName, QueryCacheConfig queryCacheConfig) {
        Map<String, Map<String, QueryCacheConfig>> queryCacheConfigsPerMap = getQueryCacheConfigs();
        String queryCacheName = queryCacheConfig.getName();
        Map<String, QueryCacheConfig> queryCacheConfigs = queryCacheConfigsPerMap.get(mapName);
        if (queryCacheConfigs != null) {
            checkFalse(queryCacheConfigs.containsKey(queryCacheName),
                    "A query cache already exists with name = [" + queryCacheName + ']');
        } else {
            queryCacheConfigs = new ConcurrentHashMap<String, QueryCacheConfig>();
            queryCacheConfigsPerMap.put(mapName, queryCacheConfigs);
        }

        queryCacheConfigs.put(queryCacheName, queryCacheConfig);
        return this;

    }

    public Map<String, Map<String, QueryCacheConfig>> getQueryCacheConfigs() {
        if (queryCacheConfigs == null) {
            queryCacheConfigs = new ConcurrentHashMap<String, Map<String, QueryCacheConfig>>();
        }
        return queryCacheConfigs;
    }

    public void setQueryCacheConfigs(Map<String, Map<String, QueryCacheConfig>> queryCacheConfigs) {
        this.queryCacheConfigs = queryCacheConfigs;
    }

    private <T> T lookupByPattern(Map<String, T> configPatterns, String itemName) {
        T candidate = configPatterns.get(itemName);
        if (candidate != null) {
            return candidate;
        }
        String configPatternKey = configPatternMatcher.matches(configPatterns.keySet(), itemName);
        if (configPatternKey != null) {
            return configPatterns.get(configPatternKey);
        }
        if (!"default".equals(itemName) && !itemName.startsWith("hz:")) {
            LOGGER.finest("No configuration found for " + itemName + ", using default config!");
        }
        return null;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public ClientConnectionStrategyConfig getConnectionStrategyConfig() {
        return connectionStrategyConfig;
    }

    public ClientConfig setConnectionStrategyConfig(ClientConnectionStrategyConfig connectionStrategyConfig) {
        this.connectionStrategyConfig = connectionStrategyConfig;
        return this;
    }

    /**
     * Get current configuration of User Code Deployment.
     *
     * @return User Code Deployment configuration
     * @since 3.9
     */
    public ClientUserCodeDeploymentConfig getUserCodeDeploymentConfig() {
        return userCodeDeploymentConfig;
    }

    /**
     * Set User Code Deployment configuration
     *
     * @param userCodeDeploymentConfig
     * @return configured {@link com.hazelcast.client.config.ClientConfig} for chaining
     * @since 3.9
     */
    public ClientConfig setUserCodeDeploymentConfig(ClientUserCodeDeploymentConfig userCodeDeploymentConfig) {
        this.userCodeDeploymentConfig = userCodeDeploymentConfig;
        return this;
    }
}
