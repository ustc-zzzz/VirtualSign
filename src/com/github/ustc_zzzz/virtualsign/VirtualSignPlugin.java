package com.github.ustc_zzzz.virtualsign;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.github.ustc_zzzz.virtualsign.api.VirtualSign;
import com.github.ustc_zzzz.virtualsign.api.VirtualSignService;
import com.google.inject.Inject;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = VirtualSignPlugin.PLUGIN_ID, name = "VirtualSign", authors =
{ "ustc_zzzz" }, description = VirtualSignPlugin.DESCRIPTION, version = "@version@")
public class VirtualSignPlugin
{
    public static final String PLUGIN_ID = "virtualsign";
    public static final String DESCRIPTION = "A plugin providing API and demo for virtual signs";

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> config;

    private CommentedConfigurationNode rootConfig;

    private VirtualSignService service;

    private boolean enableDemoCommand;

    private boolean enableTextFormatter;

    private void loadConfig() throws IOException
    {
        this.rootConfig = this.config.load();

        this.enableDemoCommand = this.rootConfig.getNode("virtualsign", "enable-demo-command").getBoolean(true);

        this.enableTextFormatter = this.rootConfig.getNode("virtualsign", "enable-text-formatter").getBoolean(true);
    }

    private void saveConfig() throws IOException
    {
        this.rootConfig.getNode("virtualsign", "enable-demo-command").setValue(this.enableDemoCommand);

        this.rootConfig.getNode("virtualsign", "enable-text-formatter").setValue(this.enableTextFormatter);

        this.config.save(this.rootConfig);
    }

    private void loadCommand()
    {
        CommandCallable callable = CommandSpec.builder().executor((src, args) ->
        {
            if (this.enableDemoCommand && src instanceof Player)
            {
                TextSerializer ts = this.enableTextFormatter ? TextSerializers.FORMATTING_CODE : TextSerializers.PLAIN;
                List<Text> lt = args.<String> getAll("line").stream().map(ts::deserialize).collect(Collectors.toList());
                VirtualSign vs = this.service.newSign().lines(lt).finish((player, lines) ->
                {
                    player.sendMessage(Text.of(TextStyles.BOLD, "player ", TextColors.GRAY, player.getName()));
                    player.sendMessage(Text.of(TextStyles.BOLD, "wrote something at the virtual sign: "));
                    player.sendMessages(lines);
                });
                vs.show((Player) src);
                return CommandResult.success();
            }
            return CommandResult.empty();
        }).arguments(GenericArguments.allOf(GenericArguments.string(Text.of("line")))).build();
        Sponge.getCommandManager().register(this, callable, "virtualsign", "vsign", "vs");
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event)
    {
        try
        {
            this.loadConfig();
            this.saveConfig();
            this.logger.info("Loading configuration complete. ");
        }
        catch (IOException e)
        {
            this.logger.error("Error on handling configuration. ", e);
        }
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event)
    {
        String serviceClass = "";
        switch (Sponge.getPlatform().getMinecraftVersion().getName())
        {
        case "1.10.2":
        case "1.11":
            serviceClass = this.getClass().getPackage().getName() + ".unsafe.VirtualSignServiceFrostburnImpl";
            break;
        }
        try
        {
            Sponge.getServiceManager().setProvider(this, VirtualSignService.class,
                    (VirtualSignService) Class.forName(serviceClass).getConstructor(this.getClass()).newInstance(this));
        }
        catch (Exception e)
        {
            this.logger.error("Error on registering services. ", e);
        }
    }

    @Listener
    public void onStartingServer(GameStartingServerEvent event)
    {
        this.service = Sponge.getServiceManager().provideUnchecked(VirtualSignService.class);
        this.loadCommand();
    }

    @Listener
    public void onReload(GameReloadEvent event)
    {
        try
        {
            this.loadConfig();
            this.saveConfig();
            this.logger.info("Reloading configuration complete. ");
        }
        catch (IOException e)
        {
            this.logger.error("Error on handling configuration. ", e);
        }
    }

    public Path getConfigDir()
    {
        return this.configDir;
    }

    public Logger getLogger()
    {
        return this.logger;
    }

    public VirtualSignService getService()
    {
        return this.service;
    }
}
