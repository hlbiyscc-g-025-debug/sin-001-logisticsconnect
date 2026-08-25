package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Hub {

    private final String hubId;
    private final String province;
    private final String sortingCenter;
    private final Boolean active;

    @JsonCreator
    public Hub(
            @JsonProperty("hubId") String hubId,
            @JsonProperty("province") String province,
            @JsonProperty("sortingCenter") String sortingCenter,
            @JsonProperty("active") Boolean active
    ) {
        this.hubId = hubId;
        this.province = province;
        this.sortingCenter = sortingCenter;
        this.active = active;
    }

    public String getHubId() {
        return hubId;
    }

    public String getProvince() {
        return province;
    }

    public String getSortingCenter() {
        return sortingCenter;
    }

    public Boolean getActive() {
        return active;
    }
}