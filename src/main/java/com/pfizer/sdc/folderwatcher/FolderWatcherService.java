package com.pfizer.sdc.folderwatcher;

import com.pfizer.sdc.filewriter.FileWriterThread;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by LALAPM on 6/16/2016.
 */
public class FolderWatcherService implements Runnable {
    private final String mDirToWatch;
    private final String mRegexToMatch;

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;

    private final boolean trace = true;
    private final boolean recursive = true;
    private final ScheduledExecutorService mFileWriterPool;
    private String mSDCVolumeToWrite;

    public FolderWatcherService(String pDirToWatch, String pRegexToMatch, ScheduledExecutorService pFileWriterPool, String pSDCVolumeToWrite) throws IOException {
        mDirToWatch = pDirToWatch;
        mRegexToMatch = pRegexToMatch;
        this.mFileWriterPool = pFileWriterPool;
        mSDCVolumeToWrite = pSDCVolumeToWrite;

        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();

        Path dir = Paths.get(this.mDirToWatch);
        System.out.format("Scanning %s ...\n", dir);
        registerAll(dir);
        System.out.println("Done.");
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                if (isValidDir(dir)) {
                    register(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Checks with the regex if we want to monitor this directory
     */
    private boolean isValidDir(Path pDir) {
        //TODO use the regex and validate if we want to monitor this. return true or falce based on that
        return true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                // if file is created then start the new FileWriterThread
                if (kind == ENTRY_CREATE) {
                    if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        if (recursive) {
                            try {
                                registerAll(child);
                            } catch (IOException x) {
                                // ignore to keep sample readbale
                            }
                        }
                    } else {
                        //TODO keep track of the Future coming from Callable to make sure the threads are alive or closed etc.
                        //TODO externalize the 10 second delay
                        //Starts the FileWriter after 10 seconds in order to give time for any write to finish
                        mFileWriterPool.schedule(new FileWriterThread(child.toString(), this.mSDCVolumeToWrite), 10, TimeUnit.SECONDS);
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public void run() {
        this.processEvents();
    }
}
