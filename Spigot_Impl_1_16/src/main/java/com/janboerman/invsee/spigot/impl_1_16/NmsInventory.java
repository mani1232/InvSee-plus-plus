package com.janboerman.invsee.spigot.impl_1_16;

import com.janboerman.invsee.utils.ConcatList;
import com.janboerman.invsee.utils.Pair;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public class NmsInventory implements IInventory {

    private static final ItemStack EMPTY_STACK = ItemStack.b;

    protected final UUID spectatedPlayerUuid;
    protected final NonNullList<ItemStack> storageContents;
    protected final NonNullList<ItemStack> armourContents;
    protected final NonNullList<ItemStack> offHand;

    private int maxStack = MAX_STACK;
    private final List<HumanEntity> transaction = new ArrayList<>();
    InventoryHolder owner;

    protected NmsInventory(UUID spectatedPlayerUuid, NonNullList<ItemStack> storageContents, NonNullList<ItemStack> armourContents, NonNullList<ItemStack> offHand) {
        this.spectatedPlayerUuid = spectatedPlayerUuid;
        this.storageContents = storageContents;
        this.armourContents = armourContents;
        this.offHand = offHand;
    }

    private Pair<Integer, NonNullList<ItemStack>> decideWhichInv(int slot) {
        if (0 <= slot && slot < storageContents.size()) {
            return new Pair<>(slot, storageContents);
        } else if (slot < storageContents.size() + armourContents.size()) {
            return new Pair<>(slot - storageContents.size(), armourContents);
        } else if (slot < storageContents.size() + armourContents.size() + offHand.size()) {
            return new Pair<>(slot - storageContents.size() - armourContents.size(), offHand);
        } else {
            return null;
        }
    }

    @Override
    public int getSize() {
        int size = storageContents.size() + armourContents.size() + offHand.size();
        int remainder = size % 9;
        if (remainder != 0) {
            size += (9 - remainder);
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : armourContents) {
            if (!stack.isEmpty()) return false;
        }
        for (ItemStack stack : storageContents) {
            if (!stack.isEmpty()) return false;
        }
        for (ItemStack stack : offHand) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        var pair = decideWhichInv(slot);
        if (pair == null) return EMPTY_STACK;

        return pair.getSecond().get(pair.getFirst());
    }

    @Override
    public ItemStack splitStack(int slot, int subtractAmount) {
        var pair = decideWhichInv(slot);
        if (pair == null) return EMPTY_STACK;

        ItemStack stack = ContainerUtil.a(pair.getSecond(), pair.getFirst(), subtractAmount);
        if (!stack.isEmpty()) {
            update();
        }
        return stack;
    }

    @Override
    public ItemStack splitWithoutUpdate(int slot) {
        var pair = decideWhichInv(slot);
        if (pair == null) return EMPTY_STACK;

        slot = pair.getFirst();
        var items = pair.getSecond();

        var stack = items.get(slot);
        if (stack.isEmpty()) {
            return EMPTY_STACK;
        } else {
            items.set(slot, EMPTY_STACK);
            return stack;
        }
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        var pair = decideWhichInv(slot);
        if (pair == null) return;

        slot = pair.getFirst();
        var items = pair.getSecond();

        items.set(slot, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }

        update();
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void update() {
        //called after an item in the inventory was removed, added or updated.
        //looking at InventorySubContainer, I don't think we need to do anything here.
    }

    @Override
    public boolean a(EntityHuman entityHuman) {
        return true;
    }

    @Override
    public List<ItemStack> getContents() {
        ConcatList<ItemStack> contents = new ConcatList<>(storageContents, new ConcatList<>(armourContents, offHand));
        ConcatList<ItemStack> padded = new ConcatList<>(contents, NonNullList.a(getSize() - contents.size(), EMPTY_STACK));
        return padded;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return owner;
    }

    @Override
    public void setMaxStackSize(int maxStack) {
        this.maxStack = maxStack;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void clear() {
        storageContents.clear();
        armourContents.clear();
        offHand.clear();
    }
}
