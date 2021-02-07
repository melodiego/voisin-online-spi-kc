package br.com.voisinonline.spi.provider;

import io.sicredi.authcorp.spi.client.AuthenticationClient;
import io.sicredi.authcorp.spi.client.PersonClient;
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

public class VoisinStorageProviderFactory implements UserStorageProviderFactory<SicStorageProvider> {

    private static final Logger logger = Logger.getLogger(SicStorageProviderFactory.class);
    private static final String PROVIDER_ID = "keycloak-sicredi-customer-federation";

    private SicStorageConfiguration sicStorageConfiguration;

    private AuthenticationClient authenticationClient;
    private PersonClient personClient;

    private boolean dirtyConfiguration = true;

    protected static final List<ProviderConfigProperty> configMetadata = ProviderConfigurationBuilder.create()
            .property().name(SicStorageConfiguration.AUTH_SERVICE_URL)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Auth Service URL")
            .defaultValue(SicStorageConfiguration.AUTH_SERVICE_URL_DEFAULT)
            .helpText("Auth Service URL Ex.: " + SicStorageConfiguration.AUTH_SERVICE_URL_DEFAULT)
            .add()
            .property().name(SicStorageConfiguration.CREDENTIAL_TYPE_NAME)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Credential Type Name")
            .defaultValue(SicStorageConfiguration.CREDENTIAL_TYPE_NAME_DEFAULT)
            .helpText("CredentialType  Name")
            .add()
            .property().name(SicStorageConfiguration.CONNECTION_TIMEOUT)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Connection timeout in ms")
            .defaultValue(SicStorageConfiguration.CONNECTION_TIMEOUT_DEFAULT_VALUE)
            .helpText("Connection timeout to Retrofit")
            .add()
            .property().name(SicStorageConfiguration.READ_TIMEOUT)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Read timeout in ms")
            .defaultValue(SicStorageConfiguration.READ_TIMEOUT_DEFAULT_VALUE)
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
    public SicStorageProvider create(KeycloakSession session, ComponentModel model) {
        logger.debugf("create - Creating component with model - session[%s] - model[%s]", session, model);
        
        updateConfiguration(model);

        logger.debugf("create - sicStorageConfiguration[%s] - authenticationClient[%s] - personClient[%s]", 
            sicStorageConfiguration, authenticationClient, personClient);

        return new SicStorageProvider(session, model, sicStorageConfiguration, authenticationClient, personClient);
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

            SicStorageConfiguration newSicStorageConfiguration = createSicStorageConfiguration(model);

            logger.debugf("updateConfiguration - old[%s] - new[%s]", 
            sicStorageConfiguration, newSicStorageConfiguration);

            this.sicStorageConfiguration = newSicStorageConfiguration;

            Retrofit retrofit = createRetrofit(sicStorageConfiguration);

            this.authenticationClient = new AuthenticationClient(retrofit);
            this.personClient = new PersonClient(retrofit);

            logger.debugf("updateConfiguration - retrofit[%s] - authenticationClient[%s] - personClient[%s]", 
                retrofit, authenticationClient, personClient);
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

    private SicStorageConfiguration createSicStorageConfiguration(ComponentModel componentModel) {
        SicStorageConfiguration sicStorageConfiguration = new SicStorageConfiguration(
                componentModel.get(SicStorageConfiguration.AUTH_SERVICE_URL),
                componentModel.get(SicStorageConfiguration.CREDENTIAL_TYPE_NAME),
                Long.parseLong(componentModel.get(SicStorageConfiguration.CONNECTION_TIMEOUT)),
                Long.parseLong(componentModel.get(SicStorageConfiguration.READ_TIMEOUT)));

        logger.debugf("createSicStorageConfiguration - creating sicStorageConfiguration [%s]", sicStorageConfiguration);

        return sicStorageConfiguration;
    }

    private Retrofit createRetrofit(SicStorageConfiguration sicStorageConfiguration) {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(sicStorageConfiguration.getReadTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(sicStorageConfiguration.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(sicStorageConfiguration.getAuthServiceUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

}