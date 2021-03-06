package cofh.thermal.core.item;

import cofh.core.energy.EnergyStorageCoFH;
import cofh.core.energy.IEnergyContainerItem;
import cofh.core.item.BlockItemAugmentable;
import cofh.core.util.helpers.AugmentDataHelper;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.util.constants.NBTTags.*;
import static cofh.core.util.helpers.AugmentableHelper.getAttributeFromAugmentMax;
import static cofh.core.util.helpers.AugmentableHelper.getPropertyWithDefault;
import static cofh.core.util.helpers.StringHelper.*;
import static cofh.thermal.core.tileentity.storage.EnergyCellTile.*;

public class BlockItemEnergyCell extends BlockItemAugmentable implements IEnergyContainerItem {

    public BlockItemEnergyCell(Block blockIn, Properties builder) {

        super(blockIn, builder);

        setEnchantability(5);
    }

    @Override
    protected void tooltipDelegate(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        boolean creative = isCreative(stack);
        if (getMaxEnergyStored(stack) > 0) {
            tooltip.add(creative
                    ? getTextComponent("info.cofh.infinite_source")
                    : getTextComponent(localize("info.cofh.energy") + ": " + getScaledNumber(getEnergyStored(stack)) + " / " + getScaledNumber(getMaxEnergyStored(stack)) + " RF"));
        }
        addEnergyTooltip(stack, worldIn, tooltip, flagIn, getExtract(stack), getReceive(stack), creative);
    }

    protected void setAttributesFromAugment(ItemStack container, CompoundNBT augmentData) {

        CompoundNBT subTag = container.getChildTag(TAG_PROPERTIES);
        if (subTag == null) {
            return;
        }
        getAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_BASE_MOD);
        getAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_ENERGY_STORAGE);
        getAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_ENERGY_XFER);
    }

    //    @Override
    //    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
    //
    //        return new EnergyContainerItemWrapper(stack, this);
    //    }

    // region IEnergyContainerItem
    @Override
    public CompoundNBT getOrCreateEnergyTag(ItemStack container) {

        CompoundNBT blockTag = container.getOrCreateChildTag(TAG_BLOCK_ENTITY);
        if (!blockTag.contains(TAG_ENERGY_MAX)) {
            new EnergyStorageCoFH(BASE_CAPACITY, BASE_RECV, BASE_SEND).writeWithParams(container.getOrCreateChildTag(TAG_BLOCK_ENTITY));
        }
        return container.getOrCreateChildTag(TAG_BLOCK_ENTITY);
    }

    @Override
    public int getExtract(ItemStack container) {

        CompoundNBT tag = getOrCreateEnergyTag(container);
        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_ENERGY_STORAGE, 1.0F);
        return Math.round(tag.getInt(TAG_ENERGY_SEND) * mod * base);
    }

    @Override
    public int getReceive(ItemStack container) {

        CompoundNBT tag = getOrCreateEnergyTag(container);
        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_ENERGY_STORAGE, 1.0F);
        return Math.round(tag.getInt(TAG_ENERGY_RECV) * mod * base);
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {

        CompoundNBT tag = getOrCreateEnergyTag(container);
        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_ENERGY_STORAGE, 1.0F);
        return getMaxStored(container, Math.round(tag.getInt(TAG_ENERGY_MAX) * mod * base));
    }
    // endregion

    // region IAugmentableItem
    @Override
    public void updateAugmentState(ItemStack container, List<ItemStack> augments) {

        container.getOrCreateTag().put(TAG_PROPERTIES, new CompoundNBT());
        for (ItemStack augment : augments) {
            CompoundNBT augmentData = AugmentDataHelper.getAugmentData(augment);
            if (augmentData == null) {
                continue;
            }
            setAttributesFromAugment(container, augmentData);
        }
        int energyExcess = getEnergyStored(container) - getMaxEnergyStored(container);
        if (energyExcess > 0) {
            setEnergyStored(container, getMaxEnergyStored(container));
        }
    }
    // endregion
}
