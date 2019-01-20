package artifacts.common.item;

import artifacts.Artifacts;
import artifacts.client.model.ModelBottledCloud;
import artifacts.common.CommonProxy;
import artifacts.common.network.PacketBottledCloudJump;
import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.render.IRenderBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber
public class BaubleBottledCloud extends BaubleBase implements IRenderBauble {

    protected ModelBase model = new ModelBottledCloud();

    protected ResourceLocation textures;

    public final boolean isFart;

    public BaubleBottledCloud(String name, boolean isFart) {
        super(name, BaubleType.BELT);
        this.isFart = isFart;
        if (isFart) {
            textures = new ResourceLocation(Artifacts.MODID, "textures/entity/bottled_fart.png");
        } else {
            textures = new ResourceLocation(Artifacts.MODID, "textures/entity/bottled_cloud.png");
        }

    }

    @Override
    public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, RenderType renderType, float partialticks) {
        if (renderType == RenderType.BODY) {
            Helper.rotateIfSneaking(player);
            Minecraft.getMinecraft().renderEngine.bindTexture(textures);
            model.render(player, partialticks, 0, 0, 0, 0, 1/16F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.translateToLocal("tooltip." + name + ".name"));
    }

    @SideOnly(Side.CLIENT)
    private static boolean canDoubleJump;

    @SideOnly(Side.CLIENT)
    private static boolean hasReleasedJumpKey;

    @SubscribeEvent
    @SuppressWarnings("unused")
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;

            if (player != null) {
                if ((player.onGround || player.isOnLadder()) && !player.isInWater()) {
                    hasReleasedJumpKey = false;
                    canDoubleJump = true;
                } else {
                    if (!player.movementInput.jump) {
                        hasReleasedJumpKey = true;
                    } else {
                        if (!player.capabilities.isFlying && canDoubleJump && hasReleasedJumpKey) {
                            canDoubleJump = false;
                            ItemStack stack = BaublesApi.getBaublesHandler(player).getStackInSlot(BaubleType.BELT.getValidSlots()[0]);
                            if (stack.getItem() instanceof BaubleBottledCloud) {
                                CommonProxy.NETWORK_HANDLER_INSTANCE.sendToServer(new PacketBottledCloudJump(((BaubleBottledCloud) stack.getItem()).isFart));
                                player.jump();
                                player.fallDistance = 0;
                                if (((BaubleBottledCloud) stack.getItem()).isFart) {
                                    player.playSound(BaubleWhoopieCushion.FART, 1, 0.9F + player.getRNG().nextFloat() * 0.2F);
                                } else {
                                    player.playSound(SoundEvents.BLOCK_CLOTH_FALL, 1, 0.9F + player.getRNG().nextFloat() * 0.2F);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
