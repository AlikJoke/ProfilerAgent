package ru.joke.profiler.configuration.loaders;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.bettercloud.vault.response.LogicalResponse;
import com.bettercloud.vault.rest.RestResponse;
import ru.joke.profiler.configuration.InvalidConfigurationException;
import ru.joke.profiler.configuration.meta.ConfigurationParser;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

final class VaultConfigurationPropertiesLoader implements ConfigurationPropertiesLoader {

    private static final Logger logger = Logger.getLogger(ProfilingConfigurationLoader.class.getCanonicalName());

    private final VaultPropertiesSourceConfiguration configuration;
    private volatile RefreshableVaultClient refreshableVaultClient;

    VaultConfigurationPropertiesLoader(final Map<String, String> args) {
        this.configuration = ConfigurationParser.parse(VaultPropertiesSourceConfiguration.class, args);
        if (this.configuration == null) {
            logger.info("Vault configuration source is not configured and will not be used");
        } else {
            logger.info("Vault configuration source properties loaded: " + this.configuration);
        }
    }

    @Override
    public Map<String, String> load() {
        if (this.configuration == null) {
            return Collections.emptyMap();
        }

        return load(true);
    }

    private Map<String, String> load(final boolean retryOnAuthError) {

        try {
            final RefreshableVaultClient actualClient = takeActualVaultClient();
            final LogicalResponse dataResponse = actualClient.readProperties(this.configuration.storage().path());

            final RestResponse response = dataResponse.getRestResponse();
            if (response.getStatus() != 200) {
                return handleErrorResponse(
                        response.getStatus(),
                        retryOnAuthError,
                        response.getBody()
                );
            }

            return dataResponse.getData();
        } catch (VaultException ex) {
            if (ex.getHttpStatusCode() == 401) {
                return handleErrorResponse(ex.getHttpStatusCode(), retryOnAuthError, new byte[0]);
            }

            logger.log(Level.SEVERE, "Unable to load configuration from Vault server", ex);
            throw new InvalidConfigurationException(ex);
        }
    }

    private Map<String, String> handleErrorResponse(
            final int responseCode,
            final boolean retryOnAuthError,
            final byte[] responseBody
    ) {
        if (responseCode == 401 && retryOnAuthError) {
            logger.warning("Vault response code is 401, repeat request with new token");
            this.refreshableVaultClient = null;
            return load(false);
        }

        final String vaultResponse = extractVaultResponse(responseBody, responseCode);
        logger.severe(vaultResponse);

        throw new InvalidConfigurationException("Unable to load configuration from Vault server. " + vaultResponse);
    }

    private synchronized RefreshableVaultClient takeActualVaultClient() throws VaultException {
        final RefreshableVaultClient current = this.refreshableVaultClient;
        final RefreshableVaultClient actual = RefreshableVaultClient.refresh(current, this.configuration);
        return current == actual ? actual : (this.refreshableVaultClient = actual);
    }

    private static String extractVaultResponse(final byte[] responseBodyBytes, final int responseStatus) {
        final String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);
        return String.format("Vault response (status is %d): %s", responseStatus, responseBody);
    }

    static class RefreshableVaultClient {

        private static final long EXPIRATION_THRESHOLD = 5;

        private final Vault vault;
        private final AuthResponse lastTokenRequestResponse;
        private final long lastTokenRequestTime;

        RefreshableVaultClient(
                final Vault vault,
                final AuthResponse lastTokenRequestResponse,
                final long lastTokenRequestTime
        ) {
            this.vault = vault;
            this.lastTokenRequestResponse = lastTokenRequestResponse;
            this.lastTokenRequestTime = lastTokenRequestTime;
        }

        LogicalResponse readProperties(final String storagePath) throws VaultException {
            return this.vault.logical().read(storagePath);
        }

        static RefreshableVaultClient refresh(
                final RefreshableVaultClient current,
                final VaultPropertiesSourceConfiguration configuration
        ) throws VaultException {
            if (current != null && current.lastTokenRequestResponse != null) {
                final long lastTokenRequestElapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - current.lastTokenRequestTime);
                if (current.lastTokenRequestResponse.getAuthLeaseDuration() > lastTokenRequestElapsedTime + EXPIRATION_THRESHOLD) {
                    if (current.lastTokenRequestResponse.isAuthRenewable()) {
                        current.vault.auth().renewSelf(configuration.leaseLifespanSeconds());
                    } else {
                        return buildVaultClient(configuration);
                    }
                }
            }

            return current == null ? buildVaultClient(configuration) : current;
        }

        private static RefreshableVaultClient buildVaultClient(final VaultPropertiesSourceConfiguration configuration) {
            try {
                Vault vault = buildVault(configuration, null);
                if (!configuration.isInitialTokenProvided()) {
                    logger.fine("Initial token isn't provided, trying to request it by auth request");
                    final long lastTokenRequestTime = System.currentTimeMillis();
                    final AuthResponse authResponse = requestVaultToken(vault, configuration);
                    validateAuthResponse(authResponse);

                    vault = buildVault(configuration, authResponse.getAuthClientToken());

                    return new RefreshableVaultClient(vault, authResponse, lastTokenRequestTime);
                }

                return new RefreshableVaultClient(vault, null, 0);
            } catch (VaultException ex) {
                logger.log(Level.SEVERE, "Unable to create vault client", ex);
                throw new InvalidConfigurationException(ex);
            }
        }

        private static void validateAuthResponse(final AuthResponse response) {
            final RestResponse restResponse = response.getRestResponse();
            if (restResponse.getStatus() != 200) {
                throw new InvalidConfigurationException("Invalid auth data provided. " + extractVaultResponse(restResponse.getBody(), restResponse.getStatus()));
            } else {
                logger.fine("Vault token successfully received by auth request");
            }
        }

        private static AuthResponse requestVaultToken(
                final Vault vault,
                final VaultPropertiesSourceConfiguration configuration
        ) throws VaultException {
            if (configuration.ssl().isCertAuthEnabled()) {
                logger.finest("Trying to take token from Vault server by TLS auth method");
                return vault.auth().loginByCert();
            } else if (configuration.userPass() != null) {
                logger.finest("Trying to take token from Vault server by user-pass method");
                return vault.auth().loginByUserPass(
                        configuration.userPass().username(),
                        new String(configuration.userPass().password())
                );
            } else {
                throw new InvalidConfigurationException("Required properties for authentication by any of the supported methods are missing");
            }
        }

        private static Vault buildVault(
                final VaultPropertiesSourceConfiguration configuration,
                final String token
        ) throws VaultException {
            final VaultConfig config = configuration.buildNativeVaultConfig(token);

            final VaultPropertiesSourceConfiguration.RetryConfiguration retryConfiguration = configuration.retry();
            return new Vault(config).withRetries(retryConfiguration.maxRetries(), (int) retryConfiguration.retryIntervalMs());
        }
    }
}
