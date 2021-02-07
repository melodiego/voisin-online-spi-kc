package br.com.voisinonline.spi.provider;

import br.com.voisinonline.spi.client.AuthenticationClient;
import br.com.voisinonline.spi.client.AuthenticationClientImpl;
import br.com.voisinonline.spi.client.UserClient;
import br.com.voisinonline.spi.client.UserClientImpl;
import okhttp3.OkHttpClient;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static br.com.voisinonline.spi.provider.VoisinStorageConfiguration.*;

public class VoisinStorageProviderFactory implements UserStorageProviderFactory<VoisinStorageProvider> {

    private static final Logger logger = Logger.getLogger(VoisinStorageProviderFactory.class);
    private static final String PROVIDER_ID = "keycloak-voisin-customer-federation";

    private VoisinStorageConfiguration VoisinStorageConfiguration;

    private AuthenticationClient authenticationClient;
    private UserClient userClient;

    private boolean dirtyConfiguration = true;

    protected static final List<ProviderConfigProperty> configMetadata = ProviderConfigurationBuilder.create()
            .property().name(VOISIN_SERVICE_URL)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Auth Service URL")
            .defaultValue(VOISIN)
            .helpText("Auth Service URL Ex.: " + VOISIN)
            .add()
            .property().name(CONNECTION_TIMEOUT)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Connection timeout in ms")
            .defaultValue(CONNECTION_TIMEOUT_DEFAULT_VALUE)
            .helpText("Connection timeout to Retrofit")
            .add()
            .property().name(READ_TIMEOUT)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Read timeout in ms")
            .defaultValue(READ_TIMEOUT_DEFAULT_VALUE)
            .helpText("Read timeout to Retrofit")
            .add()
            .build();

    @Override
    public void init(Config.Scope config) {
        this.dirtyConfiguration = true;

        logger.debugf("init - Loading %s configuration on init [%s]", PROVIDER_ID, config);
    }

    @Override
    public void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        this.dirtyConfiguration = true;

        logger.debugf("onUpdate - Reloading configuration on update - oldModel[%s] - newModel[%s]", 
            oldModel.getConfig(), newModel.getConfig());

        this.updateConfiguration(newModel);
    }

    @Override
    public VoisinStorageProvider create(KeycloakSession session, ComponentModel model) {
        logger.debugf("create - Creating component with model - session[%s] - model[%s]", session, model);
        
        updateConfiguration(model);

        logger.debugf("create - VoisinStorageConfiguration[%s] - authenticationClient[%s] - personClient[%s]", 
            VoisinStorageConfiguration, authenticationClient, userClient);

        return new VoisinStorageProvider(session, model, VoisinStorageConfiguration, authenticationClient, userClient);
    }

    @Override
    public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
        logger.debugf("onCreate - Loading configuration on create - model[%s]", model.getConfig());

        this.updateConfiguration(model);
    }

    private void updateConfiguration(ComponentModel model) {
        logger.debugf("updateConfiguration - dirtyConfiguration [%s]", dirtyConfiguration);

        if (dirtyConfiguration) {
            dirtyConfiguration = false;

            VoisinStorageConfiguration newVoisinStorageConfiguration = createVoisinStorageConfiguration(model);

            logger.debugf("updateConfiguration - old[%s] - new[%s]", 
            VoisinStorageConfiguration, newVoisinStorageConfiguration);

            this.VoisinStorageConfiguration = newVoisinStorageConfiguration;

            Retrofit retrofit = createRetrofit(VoisinStorageConfiguration);

            this.authenticationClient = new AuthenticationClientImpl(retrofit);
            this.userClient = new UserClientImpl(retrofit);

            logger.debugf("updateConfiguration - retrofit[%s] - authenticationClient[%s] - userClient[%s]",
                retrofit, authenticationClient, userClient);
        }
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        logger.debugf("getConfigProperties - Getting configProperties %s", configMetadata);
        return configMetadata;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        this.dirtyConfiguration = true;
        
        logger.debugf("validateConfiguration - Validating %s configuration on init [%s]", PROVIDER_ID, config);
    }

    private VoisinStorageConfiguration createVoisinStorageConfiguration(ComponentModel componentModel) {
        VoisinStorageConfiguration VoisinStorageConfiguration = new VoisinStorageConfiguration(
                componentModel.get(VOISIN_SERVICE_URL),
                Long.parseLong(componentModel.get(CONNECTION_TIMEOUT)),
                Long.parseLong(componentModel.get(READ_TIMEOUT)));

        logger.debugf("createVoisinStorageConfiguration - creating VoisinStorageConfiguration [%s]", VoisinStorageConfiguration);

        return VoisinStorageConfiguration;
    }

    private Retrofit createRetrofit(VoisinStorageConfiguration VoisinStorageConfiguration) {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(VoisinStorageConfiguration.getReadTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(VoisinStorageConfiguration.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(VoisinStorageConfiguration.getAuthServiceUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }
}