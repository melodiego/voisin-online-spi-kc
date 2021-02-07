package br.com.voisinonline.spi.client;

import br.com.voisinonline.spi.dto.AuthenticationFormDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthenticationClient {

    @POST("/voisin-online/authentication")
    Call<Void> authentication(@Body AuthenticationFormDTO authenticationFormDTO);
}