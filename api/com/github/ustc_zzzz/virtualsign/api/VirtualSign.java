package com.github.ustc_zzzz.virtualsign.api;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * The representation of the virtual sign.
 *
 * @author zzzz
 */
public interface VirtualSign
{
    /**
     * Set the lines of the virtual sign. This method needs a {@link List} of
     * {@link org.spongepowered.api.text.Text} for custom styling support on the
     * sign GUI. If the length of the parameter i smaller than the length of the
     * sign (4 in Minecraft 1.11 and previous versions), the extra lines will
     * not be set, if larger, the redundant part of the list will be ignored.
     */
    VirtualSign lines(List<Text> lines);

    /**
     * Almost the same as {@link #lines(List)}, while this method uses arrays.
     */
    default VirtualSign lines(Text... lines)
    {
        return this.lines(Arrays.asList(lines));
    }

    /**
     * Set the text of specific line of the sign. If the line does not exist,
     * the operation will be ignored.
     */
    VirtualSign line(int lineNumber, Text text);

    /**
     * Set the callback which will be called after the player specified by
     * {@link #show(Player)} finished editing the sign and close the GUI.
     */
    VirtualSign finish(BiConsumer<Player, List<Text>> eventHandler);

    /**
     * Specify the player which would edit the sign and show the GUI to him.
     */
    void show(Player player);
}
