/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.uriloader;

import org.jd.gui.api.API;
import org.jd.gui.service.extension.ExtensionService;
import org.jd.gui.spi.UriLoader;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;

public class UriLoaderService {
    protected static final UriLoaderService URI_LOADER_SERVICE = new UriLoaderService();

    public static UriLoaderService getInstance() { return URI_LOADER_SERVICE; }

    protected HashMap<String, UriLoader> mapProviders = new HashMap<>();

    protected UriLoaderService() {
        Collection<UriLoader> providers = ExtensionService.getInstance().load(UriLoader.class);

        for (UriLoader provider : providers) {
            for (String scheme : provider.getSchemes()) {
                mapProviders.put(scheme, provider);
            }
        }
    }

    public UriLoader get(API api, URI uri) {
        UriLoader provider = mapProviders.get(uri.getScheme());

        if (provider.accept(api, uri)) {
            return provider;
        } else {
            return null;
        }
    }
}
