package br.com.voisinonline.spi.provider;

import br.com.voisinonline.spi.client.AuthenticationClient;
import br.com.voisinonline.spi.client.UserClient;
import br.com.voisinonline.spi.dto.AuthenticationFormDTO;
import br.com.voisinonline.spi.dto.UserDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.ReadOnlyUserModelDelegate;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.adapter.InMemoryUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import retrofit2.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoisinStorageProvider
        implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, UserQueryProvider {
    private static final Logger logger = Logger.getLogger(VoisinStorageProvider.class);

    private static final String SEARCH_PARAMETER_NAME = "keycloak.session.realm.users.query.search";

    private final KeycloakSession session;
    private final UserStorageProviderModel model;
    private final VoisinStorageConfiguration VoisinStorageConfiguration;
    private final AuthenticationClient authenticationClient;
    private final UserClient userClient;
    private final Map<String, UserModel> loadedPerson = new ConcurrentHashMap<>();

    public VoisinStorageProvider(KeycloakSession session, ComponentModel model,
                                 VoisinStorageConfiguration VoisinStorageConfiguration, AuthenticationClient authenticationClient,
                                 UserClient userClient) {
        logger.debugf("VoisinStorageProvider created!");

        this.session = session;
        this.model = new UserStorageProviderModel(model);
        this.VoisinStorageConfiguration = VoisinStorageConfiguration;
        this.authenticationClient = authenticationClient;
        this.userClient = userClient;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        try {
            String clientId = this.session.getContext().getClient().getClientId();
            logger.debugf("IsValid[realm=%s][client=%s][user=%s][credentials=%s]", realm.getId(), clientId,
                    user.getUsername(), input.getType());
            String username = user.getUsername();
            String password = input.getChallengeResponse();

            AuthenticationFormDTO authenticationFormDTO = new AuthenticationFormDTO(username, password);
            Response<Void> authenticationResponse = authenticationClient.authentication(authenticationFormDTO)
                    .execute();

            return HttpStatus.SC_NO_CONTENT == authenticationResponse.code();
        } catch (Exception exception) {
            logger.errorf(exception.getMessage(), exception);

            throw new ModelException(exception.getMessage(), exception);
        }
    }

    public void close() {
        logger.debug("Closing provider...");
    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        logger.debugf("getUserById - id [%s]", id);
        StorageId storageId = new StorageId(id);

        logger.debugf("getUserById storageId.getExternalId()[%s]", storageId.getExternalId());
        logger.debugf("getUserById storageId[%s]", storageId);

        return this.loadUserModel(storageId.getExternalId(), realm);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        logger.debugf("getUserByUsername - username [%s]", username);

        return this.loadUserModel(username, realm);
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        return 0;
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm) {
        return Collections.emptyList();
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
        return Collections.emptyList();
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {
        return searchForUser(search, realm, 0, Integer.MAX_VALUE);
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
        UserModel userModel = loadUserModel(search, realm);
        if (userModel != null) {
            return Collections.singletonList(userModel);
        }

        return Collections.emptyList();
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
        String search = params.get(SEARCH_PARAMETER_NAME);

        if (!StringUtils.isBlank(search)) {
            return this.searchForUser(search, realm, 0, Integer.MAX_VALUE);
        }

        return Collections.emptyList();
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {
        String search = params.get(SEARCH_PARAMETER_NAME);

        if (!StringUtils.isBlank(search)) {
            return this.searchForUser(search, realm, firstResult, maxResults);
        }

        return Collections.emptyList();
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
        return Collections.emptyList();
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
        return Collections.emptyList();
    }

    @Override
    public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {
        return Collections.emptyList();
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        logger.debugf("getUserByEmail - email [%s]", email);

        return null;
    }

    private UserModel loadUserModel(String username, RealmModel realm) {
        logger.debugf("loadUserModel - username [%s]", username);

        UserModel userModel = loadedPerson.computeIfAbsent(username, 
            key -> loadUserModelFromRepository(key, realm));

        if (userModel == null) return null;

        return loadedPerson.put(username, new ReadOnlyUserModelDelegate(userModel));
    }

    private UserModel loadUserModelFromRepository(String username, RealmModel realm) {
        logger.debugf("loadUserModelFromRepository - username [%s]", username);

        UserDTO userDTO = getUserByUsername(username);
        if (userDTO != null) {
            logger.debugf("loadUserModelFromRepository - userDTO [%s]", userDTO);

            InMemoryUserAdapter adapter = new InMemoryUserAdapter(session, realm, new StorageId(model.getId(), username).getId());
            adapter.addDefaults();
            UserModel userModel = adapter;

            userModel.setFirstName(userDTO.getName());
            userModel.setEmail(userDTO.getMail());
            userModel.setEnabled(userDTO.isActive());
            userModel.setUsername(userDTO.getMail());
            userModel.setEmailVerified(true);

            return new ReadOnlyUserModelDelegate(userModel);
        }

        return null;
    }

    private UserDTO getUserByUsername(String username) {
        try {
            Response<UserDTO> userDTOResponse = userClient.getUserByMail(username).execute();
            if (HttpStatus.SC_OK == userDTOResponse.code()) {
                return userDTOResponse.body();
            } else if (HttpStatus.SC_NOT_FOUND == userDTOResponse.code()) {
                logger.infof("person not found getPersonByUsername [%s][%s]", userDTOResponse.code(),
                        userDTOResponse.errorBody());

                return null;
            } else {
                logger.errorf("error getPersonByUsername [%s][%s]", userDTOResponse.code(),
                        userDTOResponse.errorBody());
            }

            throw new ModelException("error loading person: " + username);
        } catch (IOException ioException) {
            logger.errorf("VoisinStorageProvider.getUserByUsername [Error=%s]", ioException.getMessage(), ioException);

            throw new ModelException(ioException.getMessage(), ioException);
        }
    }
}