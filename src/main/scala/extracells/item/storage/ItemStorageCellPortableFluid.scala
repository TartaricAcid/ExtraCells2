package extracells.item.storage

import java.util

import appeng.api.AEApi
import appeng.api.config.{AccessRestriction, FuzzyMode}
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.{IMEInventoryHandler, StorageChannel}
import extracells.api.{ECApi, IHandlerFluidStorage, IPortableFluidStorageCell}
import extracells.inventory.{ECFluidFilterInventory, InventoryPlain}
import extracells.item.{ItemECBase, ItemFluid, PowerItem}
import extracells.models.ModelManager
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.{EnumRarity, Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.translation.I18n
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand}
import net.minecraft.world.World
import net.minecraftforge.fluids.{Fluid, FluidRegistry}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object ItemStorageCellPortableFluid extends ItemECBase with IPortableFluidStorageCell with PowerItem {

  override val MAX_POWER: Double = 20000

  def THIS = this

  setMaxStackSize(1)
  setMaxDamage(0)


  @SuppressWarnings(Array("rawtypes", "unchecked")) override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: util.List[String], par4: Boolean) {
    val list2 = list.asInstanceOf[util.List[String]]
    val handler: IMEInventoryHandler[IAEFluidStack] = AEApi.instance.registries.cell.getCellInventory(itemStack, null, StorageChannel.FLUIDS).asInstanceOf[IMEInventoryHandler[IAEFluidStack]]

    if (!(handler.isInstanceOf[IHandlerFluidStorage])) {
      return
    }
    val cellHandler: IHandlerFluidStorage = handler.asInstanceOf[IHandlerFluidStorage]
    val partitioned: Boolean = cellHandler.isFormatted
    val usedBytes: Long = cellHandler.usedBytes
    val aeCurrentPower: Double = getAECurrentPower(itemStack)
    list2.add(String.format(I18n.translateToLocal("extracells.tooltip.storage.fluid.bytes"), (usedBytes / 250).asInstanceOf[AnyRef], (cellHandler.totalBytes / 250).asInstanceOf[AnyRef]))
    list2.add(String.format(I18n.translateToLocal("extracells.tooltip.storage.fluid.types"), cellHandler.usedTypes.asInstanceOf[AnyRef], cellHandler.totalTypes.asInstanceOf[AnyRef]))
    if (usedBytes != 0) {
      list2.add(String.format(I18n.translateToLocal("extracells.tooltip.storage.fluid.content"), usedBytes.asInstanceOf[AnyRef]))
    }
    if (partitioned) {
      list2.add(I18n.translateToLocal("gui.appliedenergistics2.Partitioned") + " - " + I18n.translateToLocal("gui.appliedenergistics2.Precise"))
    }
    list2.add(I18n.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + Math.floor(aeCurrentPower / ItemStorageCellPortableFluid.MAX_POWER * 1e4) / 1e2 + "%")
  }

  def getConfigInventory(is: ItemStack): IInventory = {
    return new ECFluidFilterInventory("configFluidCell", 63, is)
  }

  override def getDurabilityForDisplay(itemStack: ItemStack): Double = {
    return 1 - getAECurrentPower(itemStack) / ItemStorageCellPortableFluid.MAX_POWER
  }


  def getFilter(stack: ItemStack): util.ArrayList[Fluid] = {
    val inventory: ECFluidFilterInventory = new ECFluidFilterInventory("", 63, stack)
    val stacks: Array[ItemStack] = inventory.slots
    val filter: util.ArrayList[Fluid] = new util.ArrayList[Fluid]
    if (stacks.length == 0) return null
    for (s <- stacks) {
      if (s != null) {
        val fluid: Fluid = FluidRegistry.getFluid(ItemFluid.getFluidName(stack))
        if (fluid != null) filter.add(fluid)
      }
    }
    return filter
  }

  def getFuzzyMode(is: ItemStack): FuzzyMode = {
    if (is == null) return null
    if (!is.hasTagCompound) is.setTagCompound(new NBTTagCompound)
    if (is.getTagCompound.hasKey("fuzzyMode")) return FuzzyMode.valueOf(is.getTagCompound.getString("fuzzyMode"))
    is.getTagCompound.setString("fuzzyMode", FuzzyMode.IGNORE_ALL.name)
    return FuzzyMode.IGNORE_ALL
  }

  def getMaxBytes(is: ItemStack): Int = {
    return 512
  }


  def getMaxTypes(unused: ItemStack): Int = {
    return 3
  }

  override def getPowerFlow(itemStack: ItemStack): AccessRestriction = {
    return AccessRestriction.READ_WRITE
  }

  override def getRarity(itemStack: ItemStack): EnumRarity = EnumRarity.RARE


  override def getSubItems(item: Item, creativeTab: CreativeTabs, itemList: util.List[ItemStack]) {
    val itemList2 = itemList.asInstanceOf[util.List[ItemStack]]
    itemList2.add(new ItemStack(item))
    val itemStack: ItemStack = new ItemStack(item)
    injectAEPower(itemStack, ItemStorageCellPortableFluid.MAX_POWER)
    itemList2.add(itemStack)
  }

  override def getUnlocalizedName(itemStack: ItemStack): String = {
    return "extracells.item.storage.fluid.portable"
  }

  def getUpgradesInventory(is: ItemStack): IInventory = {
    return new InventoryPlain("configInventory", 0, 64)
  }

  def hasPower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = {
    return getAECurrentPower(is) >= amount
  }

  def isEditable(is: ItemStack): Boolean = {
    if (is == null) return false
    is.getItem == this
  }

  @SuppressWarnings(Array("rawtypes", "unchecked"))
  override def onItemRightClick(itemStack: ItemStack, world: World, player: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    new ActionResult(EnumActionResult.SUCCESS, ECApi.instance.openPortableFluidCellGui(player, hand, world))
  }

  @SideOnly(Side.CLIENT)
  override def registerModel(item: Item, manager: ModelManager) =
    manager.registerItemModel(item, 0, "storage/fluid/portable")


  def setFuzzyMode(is: ItemStack, fzMode: FuzzyMode) {
    if (is == null) return
    if (!is.hasTagCompound) is.setTagCompound(new NBTTagCompound)
    val tag: NBTTagCompound = is.getTagCompound
    tag.setString("fuzzyMode", fzMode.name)
  }

  override def showDurabilityBar(itemStack: ItemStack): Boolean = {
    return true
  }

  def usePower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = {
    extractAEPower(is, amount)
    return true
  }
}
