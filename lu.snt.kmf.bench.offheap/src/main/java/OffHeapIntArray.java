package unsafe;

/**
 * Created by thomas on 20/05/15.
 */

import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

class OffHeapIntArray {
    private final Unsafe unsafe = getUnsafe();

    private final static long INT_SIZE_IN_BYTES = 4l;
    private final long startIndex;


    public OffHeapIntArray(long size) {
        startIndex = unsafe.allocateMemory(size * INT_SIZE_IN_BYTES);
        unsafe.setMemory(startIndex, size * INT_SIZE_IN_BYTES, (byte) 0);
    }

    public void setValue(long index, int value) {
        unsafe.putInt(index(index), value);
    }

    public int getValue(long index) {
        return unsafe.getInt(index(index));
    }

    private long index(long offset) {
        return startIndex + offset * INT_SIZE_IN_BYTES;
    }

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

    public static void main(String[] args) {
        long maximum = Integer.MAX_VALUE + 1L;

        OffHeapIntArray directIntArray = new OffHeapIntArray(maximum);
        //directIntArray.setValue(0L, 10);
        directIntArray.setValue(maximum+1, 20);

        //assertEquals(10, directIntArray.getValue(0L));
//        assertEquals(20, directIntArray.getValue(maximum));

        directIntArray.destroy();

    }
}