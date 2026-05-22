package io.hamlook.aetheria.features.misc.itemList;

import io.hamlook.aetheria.utils.RomanNumeralParser;

import java.util.ArrayList;
import java.util.List;

public class ItemFamily {

    public final String familyId;
    public String displayName;
    public String cleanDisplayName;
    public String cleanDisplayNameLower;
    public final FamilyType type;
    public final List<SkyblockItem> members = new ArrayList<>();

    public enum FamilyType { ENCHANTMENT, PET, ACCESSORY, NONE }

    public ItemFamily(String familyId, String displayName, FamilyType type) {
        this.familyId    = familyId;
        this.type        = type;
        updateDisplayName(cleanEnchantLevel(displayName));
    }

    public String cleanEnchantLevel(String str) {
        String[] words = str.split(" ");
        if(words.length < 2) return str;
        String enchantWord = words[words.length - 1];
        if(RomanNumeralParser.isValid(enchantWord)){
            StringBuilder builder = new StringBuilder();
            for(String s : words){
                if(s.equals(enchantWord)) continue;
                builder.append(s).append(" ");
            }
            return builder.toString();
        };
        return str;
    }
    public void updateDisplayName(String newName) {
        this.displayName = newName;
        this.cleanDisplayName = newName != null ? newName.replaceAll("§.", "") : "";
        this.cleanDisplayNameLower = this.cleanDisplayName.toLowerCase();
    }

    public SkyblockItem representative() {
        return members.isEmpty() ? null : members.get(0);
    }

    public boolean hasDropdown() {
        return members.size() > 1;
    }
}