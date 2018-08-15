package safetybread.hanium.sangeun.safetybread.Models;

import io.realm.RealmObject;

/**
 * Created by sangeun on 2018-08-12.
 */

public class ServiceArea extends RealmObject {
    public String routeType;
    public boolean hasStore = false;
    public String startServiceAt;
    public String endServiceAt;
    public boolean hasElectricCarCharge = false;
    public String signatureFood;
    public boolean hasFeedingRoom = false;
    public String areaName;
    public boolean hasToilet = false;
    public boolean hasPharmacy = false;
    public boolean hasMaintenance = false;
    public boolean hasCafeteria = false;
    public double latitude;
    public double longitude;
    public boolean hasLPGCharge = false;
    public boolean hasGasolineCharge = false;
    public String routeName;
    public String tel;
    public boolean hasRestPlace = false;
    public String markerId;

    @Override
    public String toString() {
        return "ServiceArea{" +
                "routeType='" + routeType + '\'' +
                ", hasStore=" + hasStore +
                ", startServiceAt='" + startServiceAt + '\'' +
                ", endServiceAt='" + endServiceAt + '\'' +
                ", hasElectricCarCharge=" + hasElectricCarCharge +
                ", signatureFood='" + signatureFood + '\'' +
                ", hasFeedingRoom=" + hasFeedingRoom +
                ", areaName='" + areaName + '\'' +
                ", hasToilet=" + hasToilet +
                ", hasPharmacy=" + hasPharmacy +
                ", hasMaintenance=" + hasMaintenance +
                ", hasCafeteria=" + hasCafeteria +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", hasLPGCharge=" + hasLPGCharge +
                ", hasGasolineCharge=" + hasGasolineCharge +
                ", routeName='" + routeName + '\'' +
                ", tel='" + tel + '\'' +
                ", hasRestPlace=" + hasRestPlace +
                '}';
    }
}
