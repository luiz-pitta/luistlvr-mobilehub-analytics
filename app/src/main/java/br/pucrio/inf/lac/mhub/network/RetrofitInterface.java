package br.pucrio.inf.lac.mhub.network;

import br.pucrio.inf.lac.mhub.model_server.Response;
import br.pucrio.inf.lac.mhub.model_server.Sensor;
import br.pucrio.inf.lac.mhub.model_server.User;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Functions to communicate with Server using POST and GET
 * @author Luiz Guilherme Pitta
 */
public interface RetrofitInterface {

    /**
     * Analytics Provider login.
     */
    @POST("login_user/")
    Observable<Response> login(@Body User user);

    /**
     * Updates Analytics Provider information.
     */
    @POST("register_analytics")
    Observable<Response> setAnalyticsMobileHub(@Body User user);

}
