package extracells.integration.mekanism;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.*;
import extracells.api.IGasStorageCell;
import extracells.inventory.HandlerItemStorageGas;
import extracells.network.GuiHandler;
import extracells.render.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class GasCellHandler implements ICellHandler {

	@Override
	public double cellIdleDrain(ItemStack is, IMEInventory handler) {
		return 0;
	}

	@Override
	public IMEInventoryHandler getCellInventory(ItemStack itemStack, ISaveProvider saveProvider, StorageChannel channel) {
		if (channel == StorageChannel.ITEMS || !(itemStack.getItem() instanceof IGasStorageCell)) {
			return null;
		}
		return new HandlerItemStorageGas(itemStack, saveProvider, ((IGasStorageCell) itemStack.getItem()).getFilter(itemStack));
	}

	/*public IMEInventoryHandler getCellInventoryPlayer(ItemStack itemStack, EntityPlayer player) {
		return new HandlerItemPlayerStorageFluid(itemStack, null, ((IFluidStorageCell) itemStack.getItem()).getFilter(itemStack), player);
	}*/

	@Override
	public int getStatusForCell(ItemStack is, IMEInventory handler) {
		if (handler == null) {
			return 0;
		}

		HandlerItemStorageGas inventory = (HandlerItemStorageGas) handler;
		if (inventory.freeBytes() == 0) {
			return 3;
		}
		if (inventory.isFormatted() || inventory.usedTypes() == inventory.totalTypes()) {
			return 2;
		}

		return 1;
	}

	@Override
	public IIcon getTopTexture_Dark() {
		return TextureManager.TERMINAL_FRONT.getTextures()[0];
	}

	@Override
	public IIcon getTopTexture_Light() {
		return TextureManager.TERMINAL_FRONT.getTextures()[2];
	}

	@Override
	public IIcon getTopTexture_Medium() {
		return TextureManager.TERMINAL_FRONT.getTextures()[1];
	}

	@Override
	public boolean isCell(ItemStack is) {
		return is != null && is.getItem() != null && is.getItem() instanceof IGasStorageCell;
	}

	@Override
	public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack is, StorageChannel chan) {
		if (chan != StorageChannel.FLUIDS) {
			return;
		}
		IStorageMonitorable monitorable = null;
		if (chest != null) {
			monitorable = ((IMEChest) chest).getMonitorable(ForgeDirection.UNKNOWN, new PlayerSource(player, chest));
		}
		if (monitorable != null) {
			GuiHandler.launchGui(GuiHandler.getGuiId(4), player, new Object[]{monitorable.getFluidInventory()});
		}
	}

}
