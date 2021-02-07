package br.com.voisinonline.spi.provider;

import java.util.Objects;

public class VoisinStorageConfiguration {

    public static final String AUTH_SERVICE_URL = "authServiceUrl";
    public static final String AUTH_SERVICE_URL_DEFAULT = "https://authcorp-api.dev.sicredi.cloud/";
    public static final String CONNECTION_TIMEOUT = "connectionTimeOut";
    public static final String CONNECTION_TIMEOUT_DEFAULT_VALUE = "2000";
    public static final String READ_TIMEOUT = "readTimeOut";
    public static final String READ_TIMEOUT_DEFAULT_VALUE = "2000";

    private String authServiceUrl;
    private Long connectionTimeout;
    private Long readTimeout;

    public VoisinStorageConfiguration(String authServiceUrl,
                                      Long connectionTimeout,
                                      Long readTimeout) {
        this.authServiceUrl = authServiceUrl;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public String getAuthServiceUrl() {
        return authServiceUrl;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public Long getReadTimeout() {
        return readTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoisinStorageConfiguration)) return false;
        VoisinStorageConfiguration that = (VoisinStorageConfiguration) o;
        return Objects.equals(authServiceUrl, that.authServiceUrl) &&
                Objects.equals(connectionTimeout, that.connectionTimeout) &&
                Objects.equals(readTimeout, that.readTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authServiceUrl, connectionTimeout, readTimeout);
    }

    @Override
    public String toString() {
        return "VoisinStorageConfiguration{" +
                "authServiceUrl='" + authServiceUrl + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", readTimeout=" + readTimeout +
                '}';
    }
}