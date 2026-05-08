package com.jef.justenoughfakepixel.features.mayor;

public class MayorManager {

    public static final long UPDATE_INTERVAL = 300000;
    public static long lastElectionUpdate = 0;
    public static long lastMayorUpdate = 0;
    public static boolean canUpdate(boolean mayor){
        return (mayor ? System.currentTimeMillis() - UPDATE_INTERVAL > lastMayorUpdate :
                System.currentTimeMillis() - UPDATE_INTERVAL > lastElectionUpdate);
    }

    public static void updateIfPossible(ElectionData data,String mayor){
        if(data != null && !data.isNull() && canUpdate(false)) {
            lastElectionUpdate = System.currentTimeMillis();
            MayorAPIHandler.sendElectionData(data);
        }
        if(mayor != null && !mayor.isEmpty() && canUpdate(true)) {
            lastMayorUpdate = System.currentTimeMillis();
            MayorAPIHandler.sendMayorData(mayor);
        }
    }
}
