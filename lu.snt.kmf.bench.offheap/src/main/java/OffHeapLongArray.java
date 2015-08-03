package unsafe;

/**
 * Created by thomas on 20/05/15.
 */

import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class OffHeapLongArray {
    private final Unsafe unsafe = getUnsafe();

   // private final static long LONG_SIZE_IN_BYTES = 8;
    private final long startIndex;


    public OffHeapLongArray(long size) {
        startIndex = unsafe.allocateMemory(size * 8);
        unsafe.setMemory(startIndex, size * 8, (byte) 0); // initialize with zero
    }

    public void setValue2(long index, long value) {
        unsafe.putLong(startIndex + index * 8, value);
    }

    public long getValue2(long index) {
        return unsafe.getLong(startIndex + index * 8);
    }

    /*
    public void setValue(long index, long value) {
        unsafe.putLong(index(index), value);
    }

    public long getValue(long index) {
        return unsafe.getLong(index(index));
    }*/


    //private long index(long offset) {
    //    return startIndex + offset * LONG_SIZE_IN_BYTES;
    //}

    public void destroy() {
        unsafe.freeMemory(startIndex);
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

//    public static void main(String[] args) {
//        long maximum = Integer.MAX_VALUE;
//
//        DirectLongArray directLongArray = new DirectLongArray(maximum);
//        directLongArray.setValue(0L, 10L);
//        directLongArray.setValue(maximum, 20L);
//
//        assertEquals(10, directLongArray.getValue(0L));
//        assertEquals(20, directLongArray.getValue(maximum));
//
//        directLongArray.destroy();
//
//    }
}