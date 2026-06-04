package io.hamlook.aetheria.repo.data;

public class OtherData {

    public long priceFetchInterval;
    public long priceUploadInterval;

    public OtherData(){
        priceUploadInterval = 30000;
        priceFetchInterval = 1800000;
    }
}
