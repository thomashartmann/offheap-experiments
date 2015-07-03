package unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by thomas on 21/05/15.
 */
public class UnsafeUtils {
    private static final Unsafe unsafe = getUnsafe();

    /**
     * allocates the specified number of bytes
     *
     * @return
     */
    public static long alloc(long bytes) {
        return unsafe.allocateMemory(bytes);
    }

    /**
     * increases or decreases the size of the specified block of memory, reallocates it if needed
     *
     * @return
     */
    public static long calloc(long bytes) {
        long address = unsafe.allocateMemory(bytes);
        unsafe.setMemory(address, bytes, (byte) 0);
        return address;
    }

    /**
     * allocates the specified number of bytes and initializes them to zero
     *
     * @return
     */
    public static long realloc(long address, long bytes) {
        return unsafe.reallocateMemory(address, bytes);
    }

    /**
     * releases the specified block of memory back to the system
     *
     * @param address
     */
    public static void free(long address) {
        unsafe.freeMemory(address);
    }

    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {

            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new RuntimeException("unsafe problems...");
        }
    }
}
