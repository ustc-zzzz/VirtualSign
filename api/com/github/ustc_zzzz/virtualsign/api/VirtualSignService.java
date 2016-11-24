package com.github.ustc_zzzz.virtualsign.api;

/**
 * An interface to provide convenient method for creating a virtual sign.
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
