package com.github.ustc_zzzz.virtualsign.unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.text.Text;

import com.github.ustc_zzzz.virtualsign.VirtualSignPlugin;
import com.github.ustc_zzzz.virtualsign.api.VirtualSign;
import com.google.common.base.Throwables;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketSignEditorOpen;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;

public class VirtualSignFrostburnImpl implements VirtualSign
{
    private static final MethodHandle TEXT_TO_NMS_METHOD;

    private static final Random RANDOM = new Random();

    private static final int LENGTH = 4;

    static
    {
        try
        {
            MethodType returnType = MethodType.methodType(ITextComponent.class);
            TEXT_TO_NMS_METHOD = MethodHandles.publicLookup().findVirtual(Text.class, "toComponent", returnType);
        }
        catch (NoSuchMethodException | IllegalAccessException e)
        {
            throw Throwables.propagate(e);
        }
    }

    private final Text[] lines = new Text[LENGTH];

    private final VirtualSignPlugin plugin;

    private BiConsumer<Player, List<Text>> callback = null;

    public VirtualSignFrostburnImpl(VirtualSignPlugin plugin)
    {
        this.plugin = plugin;
        Arrays.fill(this.lines, Text.of());
    }

    @Override
    public VirtualSign lines(List<Text> lines)
    {
        int index = 0;
        for (Text line : lines)
        {
            this.lines[index] = line;
            if (++index >= LENGTH)
            {
                break;
            }
        }
        return this;
    }

    @Override
    public VirtualSign line(int lineNumber, Text text)
    {
        if (lineNumber >= 0 && lineNumber < LENGTH)
        {
            this.lines[lineNumber] = text;
        }
        return this;
    }

    @Override
    public VirtualSign finish(BiConsumer<Player, List<Text>> eventHandler)
    {
        this.callback = eventHandler;
        return this;
    }

    @Override
    public void show(Player player)
    {
        EntityPlayerMP nmsPlayer = (EntityPlayerMP) player;
        WorldServer nmsWorld = nmsPlayer.getServerWorld();
        int baseX = 64 * (int) (nmsPlayer.posX / 64);
        int baseZ = 64 * (int) (nmsPlayer.posZ / 64);
        for (int i = 0; i < 64; ++i)
        {
            int x = baseX + RANDOM.nextInt(64), z = baseZ + RANDOM.nextInt(64);
            for (int y = 255; y >= 0; --y)
            {
                BlockPos pos = new BlockPos(x, y, z);
                if (nmsWorld.isAirBlock(pos))
                {
                    nmsWorld.setBlockState(pos, Blocks.WALL_SIGN.getDefaultState(), 0);
                    TileEntitySign tileEntity = new TileEntitySign();
                    tileEntity.setPlayer(nmsPlayer);
                    for (int j = 0; j < LENGTH; ++j)
                    {
                        try
                        {
                            tileEntity.signText[j] = (ITextComponent) TEXT_TO_NMS_METHOD.invokeExact(this.lines[j]);
                        }
                        catch (Throwable e)
                        {
                            throw Throwables.propagate(e);
                        }
                    }
                    nmsWorld.setTileEntity(pos, tileEntity);
                    nmsPlayer.connection.sendPacket(new SPacketBlockChange(nmsWorld, pos));
                    nmsPlayer.connection.sendPacket(new SPacketUpdateTileEntity(pos, 9, tileEntity.getUpdateTag()));
                    nmsPlayer.connection.sendPacket(new SPacketSignEditorOpen(pos));
                    EventListener<ChangeSignEvent> el = new EventListener<ChangeSignEvent>()
                    {
                        @Override
                        public void handle(ChangeSignEvent event) throws Exception
                        {
                            if (VirtualSignFrostburnImpl.this.callback != null)
                            {
                                VirtualSignFrostburnImpl.this.callback.accept(player, event.getText().asList());
                            }
                            nmsWorld.removeTileEntity(pos);
                            nmsWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
                            nmsPlayer.connection.sendPacket(new SPacketBlockChange(nmsWorld, pos));
                            event.setCancelled(true);
                            Sponge.getEventManager().unregisterListeners(this);
                        }
                    };
                    Sponge.getEventManager().registerListener(this.plugin, ChangeSignEvent.class, Order.FIRST, el);
                    return;
                }
            }
        }
        throw new RuntimeException("What? I cannot find only one air block for the sign? ");
    }
}
