package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**class Main.
 */
public class Main {
    double temp;
    @SerializedName("feels_like")
    double feelsLike;
    @SerializedName("temp_min")
    double tempMin;
    @SerializedName("temp_max")
    double tempMax;
}
