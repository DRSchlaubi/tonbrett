package dev.schlaubi.tonbrett.app.desktop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.net.URI;
import java.util.Objects;

/**
 * Utility class for calling {@code uwp_helper} functions.
 * <p>
 * Implemented in Java because {@code @PolymorphicSignature} doesn't seem to work in Kotlin
 */
public class NativeUtil {

    private static final NativeUtil instance = new NativeUtil();
    private final Linker linker = Linker.nativeLinker();
    private final SymbolLookup uwpHelper = SymbolLookup.libraryLookup("uwp_helper", SegmentScope.auto());

    private final MethodHandle launchUriMethod = linker.downcallHandle(
            uwpHelper.find("launch_uri").orElseThrow(),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    private final MethodHandle getAppdataFolder = linker.downcallHandle(
            uwpHelper.find("get_appdata_folder").orElseThrow(),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    private final MethodHandle getAppdataFolderPathLength = linker.downcallHandle(
            uwpHelper.find("get_appdata_folder_path_length").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG)
    );

    /**
     * Tries to launch the URI using the UWP {@code Launcher}.
     *
     * @param uri the {@link URI} to launch
     * @throws Throwable if an error occurs
     */
    public static void launchUri(@NotNull URI uri) throws Throwable {
        Objects.requireNonNull(uri, "uri must not be null");
        try (var arena = Arena.openConfined()) {
            var url = arena.allocateUtf8String(uri.toString());

            instance.launchUriMethod.invoke(url);
        }
    }

    private static long getAppdataFolderLength() throws Throwable {
        return (long) instance.getAppdataFolderPathLength.invoke();
    }

    /**
     * Tries to retrieve the current UWP app data folder.
     *
     * @return the absolute path to the folder
     * @throws Throwable if an error occurrs
     */
    @Nullable
    public static String getAppdataFolder() throws Throwable {
        var length = getAppdataFolderLength();
        if (length == 0) {
            return null;
        }
        try(var arena = Arena.openConfined()) {
            var buffer = arena.allocateArray(ValueLayout.JAVA_CHAR, length);
            instance.getAppdataFolder.invoke(buffer);
            return new String(buffer.toArray(ValueLayout.JAVA_CHAR));
        }
    }
}
