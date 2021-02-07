package br.com.voisinonline.spi.client;

import br.com.voisinonline.spi.dto.ErrorMessageDTO;
import br.com.voisinonline.spi.dto.UserDTO;
import com.google.gson.Gson;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import com.google.gson.Gson;

public class UserClientImpl implements UserClient {
    private static final Logger logger = Logger.getLogger(UserClientImpl.class);

    private final Retrofit retrofit;

    public UserClientImpl(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public Call<UserDTO> getUserByMail(String mail) {
        return this.getPerson(mail);
    }

    private Call<UserDTO> getPerson(String mail) {
        logger.debugf("GetUserByMail Person with authcorp-api");
        Call<UserDTO> userClientApi = userClient().getUserByMail(mail);

        userClientApi.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if(response.isSuccessful()) {
                    logger.debugf("GetUserByMail API is Successful. mail[%s]", mail);
                } else {
                    try {
                        logger.errorf("GetUserByMail API is not Successful - errorBody [%s]", response.errorBody().string());
                        ErrorMessageDTO message = new Gson().fromJson(response.errorBody().charStream(), ErrorMessageDTO.class);
                        logger.errorf("GetUserByMail API is not Successful - errorMessage [%s]", message);
                    } catch (Exception exception) {
                        logger.errorf("GetUserByMail is not Successful [%s]", exception.getMessage(), exception);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable throwable) {
                logger.errorf("GetUserByMail [Error Message=%s]", throwable.getMessage(), throwable);
            }
        });

        return userClientApi;
    }

    private UserClient userClient() {
        return retrofit.create(UserClient.class);
    }
}