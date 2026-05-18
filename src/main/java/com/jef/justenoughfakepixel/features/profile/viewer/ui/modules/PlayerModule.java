package com.jef.justenoughfakepixel.features.profile.viewer.ui.modules;

import com.jef.justenoughfakepixel.utils.render.PlayerRenderer;

public class PlayerModule {

    public static void draw(int scaledX,int scaledY,int playerScale,String username,int mouseX,int mouseY){
        PlayerRenderer.renderPlayer(username,scaledX, scaledY, playerScale, mouseX, mouseY,false);
    }



}
