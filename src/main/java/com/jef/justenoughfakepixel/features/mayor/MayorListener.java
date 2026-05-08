package com.jef.justenoughfakepixel.features.mayor;

import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.features.profile.ProfileParser;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.ColorUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RegisterEvents
public class MayorListener {

    public boolean update = false;
    public List<String> mayors = Arrays.asList("Diana","Diaz","Foxy","Marina","Paul","Cole","Aatrox","Derpy","Jerry","Scorpius");

    @SubscribeEvent
    public void onChange(GuiOpenEvent event) {
        update = true;
    }

    @SubscribeEvent
    public void mayorUpdate(GuiScreenEvent.BackgroundDrawnEvent event) {
        if(!update) return;

        if(!(event.gui instanceof GuiContainer)) return;
        GuiContainer gui = (GuiContainer) event.gui;
        if(!(gui.inventorySlots instanceof ContainerChest)) return;
        ContainerChest chest = (ContainerChest) gui.inventorySlots;
        if(chest.getLowerChestInventory() == null) return;
        IInventory inv = chest.getLowerChestInventory();

        String title = ColorUtils.stripColor(inv.getDisplayName().getUnformattedText()).trim();
        if(!title.startsWith("Mayor") || !mayors.contains(title.replace("Mayor","").trim())) return;
        ItemStack stack = chest.getSlot(11).getStack();
        if(stack == null) return;
        if(!mayors.contains(stack.getDisplayName().replace("Mayor","").trim())) return;
        String current = stack.getDisplayName().replace("Mayor","").trim();
        MayorManager.updateIfPossible(null,current);
        update = false;
    }

    @SubscribeEvent
    public void electionUpdate(GuiScreenEvent.BackgroundDrawnEvent event) {
        if(!update) return;
        if(!(event.gui instanceof GuiContainer)) return;
        GuiContainer gui = (GuiContainer) event.gui;
        if(!(gui.inventorySlots instanceof ContainerChest)) return;
        ContainerChest chest = (ContainerChest) gui.inventorySlots;
        if(chest.getLowerChestInventory() == null) return;
        IInventory inv = chest.getLowerChestInventory();
        String title = ColorUtils.stripColor(inv.getDisplayName().getUnformattedText()).trim();
        if(!title.startsWith("Election, Year")) return;
        ElectionData data = parseElectionData(chest);
        if(data == null) return;

        MayorManager.updateIfPossible(data,null);
        update = false;
    }

    public ElectionData parseElectionData(ContainerChest chest) {
        int[] mayorsSlots = new int[] {9,11,13,15,17};
        HashMap<String,Integer> mayorList = new HashMap<>();

        for (int slot : mayorsSlots) {
            ItemStack stack = chest.getSlot(slot).getStack();
            if (stack == null) continue;
            String name = ColorUtils.stripColor(stack.getDisplayName()).trim();
            if (!mayors.contains(name)) continue;
            List<String> lore = ProfileParser.getLore(stack);
            for (String l : lore) {
                if (l.startsWith("Votes:")) {
                    try {
                        int votes = Integer.parseInt(l.replace("Votes:", "").trim());
                        mayorList.put(name, votes);
                    } catch (NumberFormatException ignored) {
                        JefMod.logger.info("Could not find votes for: " + name + ": " + l);
                    }
                }
            }
        }
        if(mayorList.isEmpty()) return null;
        List<Candidate> sortedCandidates = mayorList.entrySet().stream()
                .map(entry -> new Candidate(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Integer.compare(b.votes, a.votes))
                .collect(Collectors.toList());
        if (sortedCandidates.size() < 5) {
            return null;
        }
        return new ElectionData(
                sortedCandidates.get(0),
                sortedCandidates.get(1),
                sortedCandidates.get(2),
                sortedCandidates.get(3),
                sortedCandidates.get(4)
        );
    }

}
