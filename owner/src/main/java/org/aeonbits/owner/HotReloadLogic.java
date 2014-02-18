/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import static org.aeonbits.owner.Config.HotReloadType.ASYNC;
import static org.aeonbits.owner.Config.HotReloadType.SYNC;
import static org.aeonbits.owner.Util.fileFromURL;
import static org.aeonbits.owner.Util.now;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.HotReloadType;

/**
 * @author Luigi R. Viggiano
 */
class HotReloadLogic implements Serializable {
    private static final long serialVersionUID = -3819125660720521332L;

    private final PropertiesManager manager;
    private final long interval;
    private final HotReloadType type;
    private volatile long lastCheckTime = now();
    private final List<WatchableFile> watchableFiles = new ArrayList<WatchableFile>();

    private static class WatchableFile implements Serializable {
        private static final long serialVersionUID = -2289003623192543240L;

        private final File file;
        private long lastModifiedTime;

        WatchableFile(File file) {
            this.file = file;
            this.lastModifiedTime = file.lastModified();
        }

        public boolean isChanged() {
            long lastModifiedTimeNow = file.lastModified();
            boolean changed = lastModifiedTime != lastModifiedTimeNow;
            if (changed)
                lastModifiedTime = lastModifiedTimeNow;
            return changed;
        }
    }

    public HotReloadLogic(HotReload hotReload, List<URL> urls, PropertiesManager manager) {
        this.manager = manager;
        type = hotReload.type();
        interval = hotReload.unit().toMillis(hotReload.value());
        setupWatchableResources(urls);
    }

    private void setupWatchableResources(List<URL> urls) {
        for (URL url : urls) {
            File file = fileFromURL(url);
            if (file != null)
                watchableFiles.add(new WatchableFile(file));
        }
    }

    synchronized void checkAndReload() {
        if (needsReload())
            manager.reload();
    }

    private boolean needsReload() {
        if (manager.isLoading())
            return false;

        long now = now();
        if (now < lastCheckTime + interval)
            return false;

        try {
            for (WatchableFile resource : watchableFiles)
                if (resource.isChanged())
                    return true;
            return false;
        } finally {
            lastCheckTime = now;
        }
    }

    boolean isAsync() {
        return type == ASYNC;
    }

    boolean isSync() {
        return type == SYNC;
    }

}
