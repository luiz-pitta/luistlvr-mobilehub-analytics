package br.pucrio.inf.lac.mhub.network;


import br.pucrio.inf.lac.mhub.model_server.Response;
import br.pucrio.inf.lac.mhub.model_server.Sensor;
import br.pucrio.inf.lac.mhub.model_server.User;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitInterface {

    @POST("login_user/")
    Observable<Response> login(@Body User user);

    @POST("register_location")
    Observable<Response> setLocationMobileHub(@Body User user);

    @POST("register_analytics")
    Observable<Response> setAnalyticsMobileHub(@Body User user);

    @POST("get_sensor_registered")
    Observable<Response> getSensorRegistered(@Body Sensor sensor);

    @POST("set_sensor_parameters")
    Observable<Response> setSensorParameters(@Body Sensor sensor);

    @POST("remove_sensor_mobileHub")
    Observable<Response> removeSensorMobileHub(@Body Sensor sensor);

    @POST("convert_sensor_data")
    Observable<Response> convertSensorData(@Body Sensor sensor);

}
