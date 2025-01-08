package ru.joke.profiler.configuration.loaders;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.util.MillisTimePropertyParser;

import java.io.File;

final class VaultPropertiesSourceConfiguration {

    private static final String VAULT_PREFIX = "vault.";
    private static final String ADDRESS = "server_address";
    private static final String TOKEN = "token";
    private static final String READ_TIMEOUT_SECONDS = "read_timeout_seconds";
    private static final String OPEN_TIMEOUT_SECONDS = "open_timeout_seconds";
    private static final String LEASE_LIFESPAN_SECONDS = "lease_lifespan_seconds";

    private final StorageConfiguration storage;
    private final String address;
    private final RetryConfiguration retry;
    private final String token;
    private final UserPassConfiguration userPass;
    private final SslConfiguration ssl;
    private final int readTimeoutSeconds;
    private final int openTimeoutSeconds;
    private final long leaseLifespanSeconds;

    @ProfilerConfigurationPropertiesWrapper(prefix = VAULT_PREFIX, nullIfNoExplicitPropertiesProvided = true)
    private VaultPropertiesSourceConfiguration(
            @ProfilerConfigurationProperty(name = ADDRESS, defaultValue = "${VAULT_ADDR}", required = true, parser = VaultConfigurationPropertyParser.class) final String address,
            @ProfilerConfigurationProperty(name = TOKEN, defaultValue = "${VAULT_TOKEN}", parser = VaultConfigurationPropertyParser.class) final String token,
            @ProfilerConfigurationProperty(name = READ_TIMEOUT_SECONDS, defaultValue = "${VAULT_OPEN_TIMEOUT:5}", parser = VaultConfigurationPropertyParser.class) final int readTimeoutSeconds,
            @ProfilerConfigurationProperty(name = OPEN_TIMEOUT_SECONDS, defaultValue = "${VAULT_READ_TIMEOUT:5}", parser = VaultConfigurationPropertyParser.class) final int openTimeoutSeconds,
            @ProfilerConfigurationProperty(name = LEASE_LIFESPAN_SECONDS, defaultValue = "0") final long leaseLifespanSeconds,
            final RetryConfiguration retryConfiguration,
            final UserPassConfiguration userPassConfiguration,
            final SslConfiguration sslConfiguration,
            final StorageConfiguration storageConfiguration
    ) {
        this.storage = storageConfiguration;
        this.address = address;
        this.token = token;
        this.retry = retryConfiguration;
        this.userPass = userPassConfiguration;
        this.ssl = sslConfiguration;
        this.readTimeoutSeconds = readTimeoutSeconds;
        this.openTimeoutSeconds = openTimeoutSeconds;
        this.leaseLifespanSeconds = leaseLifespanSeconds;
    }

    StorageConfiguration storage() {
        return storage;
    }

    RetryConfiguration retry() {
        return retry;
    }

    UserPassConfiguration userPass() {
        return userPass;
    }

    SslConfiguration ssl() {
        return ssl;
    }

    boolean isInitialTokenProvided() {
        return token != null;
    }

    long leaseLifespanSeconds() {
        return leaseLifespanSeconds;
    }

    VaultConfig buildNativeVaultConfig(final String token) throws VaultException {
        return new VaultConfig()
                    .address(this.address)
                    .openTimeout(this.openTimeoutSeconds)
                    .readTimeout(this.readTimeoutSeconds)
                    .token(token == null ? this.token : token)
                    .sslConfig(ssl().buildVaultSslConfig())
                    .putSecretsEngineVersionForPath(storage.path() + "/", String.valueOf(storage.version()))
                .build();
    }

    @Override
    public String toString() {
        return "VaultPropertiesSourceConfiguration{"
                + "storage='" + storage
                + ", address='" + address + '\''
                + ", retry=" + retry
                + ", userPass=" + userPass
                + ", ssl=" + ssl
                + ", readTimeoutSeconds=" + readTimeoutSeconds
                + ", openTimeoutSeconds=" + openTimeoutSeconds
                + ", leaseLifespanSeconds=" + leaseLifespanSeconds
                + '}';
    }

    static class StorageConfiguration {

        private static final String STORAGE_PREFIX = "storage.";
        private static final String VERSION = "version";
        private static final String PATH = "path";

        private final int version;
        private final String path;

        @ProfilerConfigurationPropertiesWrapper(prefix = STORAGE_PREFIX)
        StorageConfiguration(
                @ProfilerConfigurationProperty(name = VERSION, defaultValue = "2") final int version,
                @ProfilerConfigurationProperty(name = PATH, required = true) final String path
        ) {
            this.version = version;
            this.path = path;
        }

        int version() {
            return version;
        }

        String path() {
            return path;
        }

        @Override
        public String toString() {
            return "StorageConfiguration{"
                    + "version=" + version
                    + ", path='" + path + '\''
                    + '}';
        }
    }

    static class RetryConfiguration {

        private static final String RETRIES_PREFIX = "retries.";
        private static final String MAX_RETRIES = "max";
        private static final String INTERVAL_MS = "interval";

        private final int maxRetries;
        private final long retryIntervalMs;

        @ProfilerConfigurationPropertiesWrapper(prefix = RETRIES_PREFIX)
        RetryConfiguration(
                @ProfilerConfigurationProperty(name = MAX_RETRIES, defaultValue = "3") final int maxRetries,
                @ProfilerConfigurationProperty(name = INTERVAL_MS, defaultValue = "5s", parser = MillisTimePropertyParser.class) final long retryIntervalMs
        ) {
            this.maxRetries = maxRetries;
            this.retryIntervalMs = retryIntervalMs;
        }

        int maxRetries() {
            return maxRetries;
        }

        long retryIntervalMs() {
            return retryIntervalMs;
        }

        @Override
        public String toString() {
            return "RetryConfiguration{"
                    + "maxRetries=" + maxRetries
                    + ", retryIntervalMs=" + retryIntervalMs
                    + '}';
        }
    }

    static class UserPassConfiguration {

        private static final String AUTH_PREFIX = "user-pass.";
        private static final String USERNAME = "username";
        private static final String PASSWORD = "password";

        private final String username;
        private final char[] password;

        @ProfilerConfigurationPropertiesWrapper(prefix = AUTH_PREFIX, nullIfNoExplicitPropertiesProvided = true)
        UserPassConfiguration(
                @ProfilerConfigurationProperty(name = USERNAME, required = true) final String username,
                @ProfilerConfigurationProperty(name = PASSWORD, required = true) final char[] password
        ) {
            this.username = username;
            this.password = password;
        }

        String username() {
            return username;
        }

        char[] password() {
            return password;
        }

        @Override
        public String toString() {
            return "UserPassConfiguration{"
                    + "username='" + username + '\''
                    + '}';
        }
    }

    static class SslConfiguration {

        private static final String SSL_PREFIX = "ssl.";

        private static final String SSL_VERIFY = "verify";

        private final boolean verify;
        private final JKS jks;
        private final Pem pem;

        @ProfilerConfigurationPropertiesWrapper(prefix = SSL_PREFIX)
        SslConfiguration(
                @ProfilerConfigurationProperty(name = SSL_VERIFY, defaultValue = "${VAULT_SSL_VERIFY:true}", parser = VaultConfigurationPropertyParser.class) final boolean verify,
                final JKS jks,
                final Pem pem
        ) {
            this.verify = verify;
            this.jks = jks;
            this.pem = pem;
        }

        boolean isCertAuthEnabled() {
            return this.verify
                    && (jks != null && jks.trustStoreFilePath != null && jks.keyStoreFilePath != null
                    || pem != null && pem.pemFilePath != null && pem.clientPemFilePath != null);
        }

        private SslConfig buildVaultSslConfig() throws VaultException {
            final SslConfig config = new SslConfig();
            if (this.jks != null) {
                this.jks.enrichSslConfig(config);
            }
            if (this.pem != null) {
                this.pem.enrichSslConfig(config);
            }

            return config
                        .verify(this.verify)
                    .build();
        }

        @Override
        public String toString() {
            return "SslConfiguration{"
                    + "verify=" + verify
                    + ", jks=" + jks
                    + ", pem=" + pem
                    + '}';
        }

        private static class JKS {

            private static final String JKS_PREFIX = "jks.";
            private static final String TRUST_STORE_FILE_PATH = "truststore_file";
            private static final String KEY_STORE_FILE_PATH = "keystore_file";
            private static final String KEY_STORE_PASSWORD = "keystore_password";

            private final String trustStoreFilePath;
            private final String keyStoreFilePath;
            private final char[] keyStorePassword;

            @ProfilerConfigurationPropertiesWrapper(prefix = JKS_PREFIX, nullIfNoExplicitPropertiesProvided = true)
            private JKS(
                    @ProfilerConfigurationProperty(name = TRUST_STORE_FILE_PATH, required = true) final String trustStoreFilePath,
                    @ProfilerConfigurationProperty(name = KEY_STORE_FILE_PATH) final String keyStoreFilePath,
                    @ProfilerConfigurationProperty(name = KEY_STORE_PASSWORD) final char[] keyStorePassword
            ) {
                this.trustStoreFilePath = trustStoreFilePath;
                this.keyStoreFilePath = keyStoreFilePath;
                this.keyStorePassword = keyStorePassword;
            }

            private void enrichSslConfig(final SslConfig config) throws VaultException {
                config.trustStoreFile(new File(this.trustStoreFilePath));

                if (this.keyStoreFilePath != null) {
                    config.keyStoreFile(new File(this.keyStoreFilePath), new String(this.keyStorePassword));
                }
            }

            @Override
            public String toString() {
                return "JKS{"
                        + "trustStoreFilePath='" + trustStoreFilePath + '\''
                        + ", keyStoreFilePath='" + keyStoreFilePath + '\''
                        + '}';
            }
        }

        private static class Pem {

            private static final String PEM_PREFIX = "pem.";
            private static final String PEM_FILE_PATH = "pem_file";
            private static final String CLIENT_PEM_FILE_PATH = "client_pem_file";
            private static final String CLIENT_KEY_PEM_FILE_PATH = "client_key_pem_file";

            private final String pemFilePath;
            private final String clientPemFilePath;
            private final String clientKeyPemFilePath;

            @ProfilerConfigurationPropertiesWrapper(prefix = PEM_PREFIX, nullIfNoExplicitPropertiesProvided = true)
            Pem(
                    @ProfilerConfigurationProperty(name = PEM_FILE_PATH) final String pemFilePath,
                    @ProfilerConfigurationProperty(name = CLIENT_PEM_FILE_PATH) final String clientPemFilePath,
                    @ProfilerConfigurationProperty(name = CLIENT_KEY_PEM_FILE_PATH) final String clientKeyPemFilePath
            ) {
                this.pemFilePath = pemFilePath;
                this.clientPemFilePath = clientPemFilePath;
                this.clientKeyPemFilePath = clientKeyPemFilePath;
            }

            private void enrichSslConfig(final SslConfig config) throws VaultException {
                if (this.pemFilePath != null) {
                    config.pemFile(new File(this.pemFilePath));
                }

                if (this.clientPemFilePath != null) {
                    config.clientPemFile(new File(this.clientPemFilePath));
                }

                if (this.clientKeyPemFilePath != null) {
                    config.clientKeyPemFile(new File(this.clientKeyPemFilePath));
                }
            }

            @Override
            public String toString() {
                return "Pem{"
                        + "pemFilePath='" + pemFilePath + '\''
                        + ", clientPemFilePath='" + clientPemFilePath + '\''
                        + ", clientKeyPemFilePath='" + clientKeyPemFilePath + '\''
                        + '}';
            }
        }
    }
}
