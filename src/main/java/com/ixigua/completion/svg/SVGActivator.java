package com.ixigua.completion.svg;

import javax.imageio.spi.IIORegistry;

/**
 * A component activator.
 *
 */
public class SVGActivator {
    private SVGImageReaderSpi provider;

    public void activate() {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        provider = new SVGImageReaderSpi();
        registry.registerServiceProvider(provider);
    }

    public void deactivate() {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.deregisterServiceProvider(provider);
    }
}
