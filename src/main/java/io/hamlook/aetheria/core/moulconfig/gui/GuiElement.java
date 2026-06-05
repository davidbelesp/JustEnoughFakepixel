// SPDX-License-Identifier: LGPL-3.0-only
// Derived from MoulConfig (https://github.com/NotEnoughUpdates/MoulConfig)

package io.hamlook.aetheria.core.moulconfig.gui;

import net.minecraft.client.gui.Gui;

public abstract class GuiElement extends Gui {

    public abstract void render();

    public abstract boolean mouseInput(int mouseX, int mouseY);

    public abstract boolean keyboardInput();
}