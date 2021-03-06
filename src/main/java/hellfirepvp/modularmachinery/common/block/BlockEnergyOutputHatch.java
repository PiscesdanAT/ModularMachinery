/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.util.EnergyDisplayUtil;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.prop.EnergyHatchSize;
import hellfirepvp.modularmachinery.common.tiles.TileEnergyOutputHatch;
import hellfirepvp.modularmachinery.common.tiles.base.TileEnergyHatch;
import hellfirepvp.modularmachinery.common.util.RedstoneHelper;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockEnergyOutputHatch
 * Created by HellFirePvP
 * Date: 08.07.2017 / 10:52
 */
public class BlockEnergyOutputHatch extends BlockMachineComponent implements BlockCustomName, BlockVariants {

    private static final PropertyEnum<EnergyHatchSize> BUS_TYPE = PropertyEnum.create("size", EnergyHatchSize.class);

    public BlockEnergyOutputHatch() {
        super(Material.IRON);
        setHardness(2F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (EnergyHatchSize size : EnergyHatchSize.values()) {
            items.add(new ItemStack(this, 1, size.ordinal()));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        EnergyHatchSize size = EnergyHatchSize.values()[MathHelper.clamp(stack.getMetadata(), 0, EnergyHatchSize.values().length - 1)];
        if (EnergyDisplayUtil.displayFETooltip) {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.storage", size.maxEnergy));
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.out.transfer", size.transferLimit));
            tooltip.add("");
        }
        if (Mods.IC2.isPresent() && EnergyDisplayUtil.displayIC2EUTooltip) {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.ic2.out.voltage",
                    TextFormatting.BLUE + I18n.format(size.getUnlocalizedEnergyDescriptor())));
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.ic2.out.transfer",
                    TextFormatting.BLUE + String.valueOf(size.getIC2EnergyTransmission()),
                    TextFormatting.BLUE + I18n.format("tooltip.energyhatch.ic2.powerrate")));
            tooltip.add("");
        }
        if (Mods.GREGTECH.isPresent() && EnergyDisplayUtil.displayGTEUTooltip) {
            addGTTooltip(tooltip, size);
            tooltip.add("");
        }
    }

    @Optional.Method(modid = "gregtech")
    private void addGTTooltip(List<String> tooltip, EnergyHatchSize size) {
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.gregtech.voltage.out",
                String.valueOf(size.getGTEnergyTransferVoltage()),
                size.getUnlocalizedGTEnergyTier()));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.gregtech.amperage",
                String.valueOf(size.getGtAmperage())));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.gregtech.storage",
                String.valueOf(EnergyDisplayUtil.EnergyType.GT_EU.formatEnergyForDisplay(size.maxEnergy))));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if(te != null && te instanceof TileEnergyHatch) {
                playerIn.openGui(ModularMachinery.MODID, CommonProxy.GuiType.ENERGY_INVENTORY.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BUS_TYPE, EnergyHatchSize.values()[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BUS_TYPE).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BUS_TYPE);
    }

    @Override
    public Iterable<IBlockState> getValidStates() {
        List<IBlockState> ret = new LinkedList<>();
        for (EnergyHatchSize type : EnergyHatchSize.values()) {
            ret.add(getDefaultState().withProperty(BUS_TYPE, type));
        }
        return ret;
    }

    @Override
    public String getBlockStateName(IBlockState state) {
        return state.getValue(BUS_TYPE).getName();
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        return RedstoneHelper.getRedstoneLevel(worldIn.getTileEntity(pos));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEnergyOutputHatch(state.getValue(BUS_TYPE));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    @Override
    public String getIdentifierForMeta(int meta) {
        return getStateFromMeta(meta).getValue(BUS_TYPE).getName();
    }

}