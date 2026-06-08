package io.hamlook.aetheria.repo;

import io.hamlook.aetheria.repo.data.OtherData;

public class OtherDataAPI {

    public static long getPriceFetchInterval() {
        OtherData data = RepoHandler.get(ATHRRepo.KEY_OTHER, OtherData.class, new OtherData());
        return data.priceFetchInterval;
    }

    public static long getPriceUploadInterval() {
        OtherData data = RepoHandler.get(ATHRRepo.KEY_OTHER, OtherData.class, new OtherData());
        return data.priceUploadInterval;
    }

}
