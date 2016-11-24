package com.github.ustc_zzzz.virtualsign.unsafe;

import com.github.ustc_zzzz.virtualsign.VirtualSignPlugin;
import com.github.ustc_zzzz.virtualsign.api.VirtualSign;
import com.github.ustc_zzzz.virtualsign.api.VirtualSignService;

public class VirtualSignServiceFrostburnImpl implements VirtualSignService
{
    private final VirtualSignPlugin plugin;

    public VirtualSignServiceFrostburnImpl(VirtualSignPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public VirtualSign newSign()
    {
        return new VirtualSignFrostburnImpl(this.plugin);
    }
}
