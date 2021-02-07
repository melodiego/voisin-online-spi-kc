package br.com.voisinonline.spi.client;

import br.com.voisinonline.spi.dto.AuthenticationFormDTO;
import br.com.voisinonline.spi.dto.ErrorMessageDTO;
import com.google.gson.Gson;
import org.jboss.logging.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AuthenticationClientImpl implements AuthenticationClient {
    private static final Logger logger = Logger.getLogger(AuthenticationClientImpl.class);

    private final Retrofit retrofit;
    private final AuthenticationClient authenticationClient;

    public AuthenticationClientImpl(final Retrofit retrofit) {
        this.retrofit = retrofit;
        this.authenticationClient = authenticationClient();
    }

    @Override
    public Call<Void> authentication(final AuthenticationFormDTO authentication) {
        return this.executeAuthentication(authentication);
    }

    private Call<Void> executeAuthentication(final AuthenticationFormDTO authentication) {
        logger.debugf("Authentication Person with authcorp-api");
        Call<Void> authenticationApi = authenticationClient.authentication(authentication);

        authenticationApi.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    logger.debugf("Authentication API is Successful. UserId[%s]", authentication.getUserId());
                } else {
                    try {
                        logger.errorf("Authentication API is not Successful - errorBody [%s]", response.errorBody().string());
                        ErrorMessageDTO message = new Gson().fromJson(response.errorBody().charStream(), ErrorMessageDTO.class);
                        logger.errorf("Authentication API is not Successful - errorMessage [%s]", message);
                    } catch (Exception exception) {
                        logger.errorf("Authentication is not Successful [%s]", exception.getMessage(), exception);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                logger.errorf("[Error Message=%s]", throwable.getMessage(), throwable);
            }
        });

        return authenticationApi;
    }

    private AuthenticationClient authenticationClient() {
        return retrofit.create(AuthenticationClient.class);
    }
}
