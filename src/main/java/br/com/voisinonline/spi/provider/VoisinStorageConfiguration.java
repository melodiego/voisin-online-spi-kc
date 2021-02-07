package br.com.voisinonline.spi.provider;

import java.util.Objects;

public class VoisinStorageConfiguration {

    public static final String VOISIN_SERVICE_URL = "voisinServiceUrl";
//    public static final String VOISIN = "https://voisin-online-login.dev.voisin.cloud/";
    public static final String VOISIN = "http://localhost8081/voisin-online";
    public static final String CONNECTION_TIMEOUT = "connectionTimeOut";
    public static final String CONNECTION_TIMEOUT_DEFAULT_VALUE = "2000";
    public static final String READ_TIMEOUT = "readTimeOut";
    public static final String READ_TIMEOUT_DEFAULT_VALUE = "2000";

    private String voisinServiceUrl;
    private Long connectionTimeout;
    private Long readTimeout;

    public VoisinStorageConfiguration(String authServiceUrl,
                                      Long connectionTimeout,
                                      Long readTimeout) {
        this.voisinServiceUrl = authServiceUrl;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public String getVoisinServiceUrl() {
        return voisinServiceUrl;
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
        return Objects.equals(voisinServiceUrl, that.voisinServiceUrl) &&
                Objects.equals(connectionTimeout, that.connectionTimeout) &&
                Objects.equals(readTimeout, that.readTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voisinServiceUrl, connectionTimeout, readTimeout);
    }

    @Override
    public String toString() {
        return "VoisinStorageConfiguration{" +
                "voisinServiceUrl='" + voisinServiceUrl + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", readTimeout=" + readTimeout +
                '}';
    }
}