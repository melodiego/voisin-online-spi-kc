package br.com.voisinonline.spi.client;

import br.com.voisinonline.spi.dto.UserDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UserClient {

    @GET("/voisin-online/users/{mail}")
    public Call<UserDTO> getUserByMail(@Path("mail") String mail);
}