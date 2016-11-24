package com.github.ustc_zzzz.virtualsign.api;

/**
 * An interface to provide convenient method for creating a virtual sign. See
 * {@link org.spongepowered.api.service.ServiceManager#provide(Class)} and
 * {@link org.spongepowered.api.service.ServiceManager#provideUnchecked(Class)}.
 *
 * @author zzzz
 */
public interface VirtualSignService
{
    /**
     * Create a new virtual sign. See {@link VirtualSign}.
     *
     * @return a new virtual sign
     */
    VirtualSign newSign();
}
