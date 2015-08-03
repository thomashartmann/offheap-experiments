package unsafe;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by thomas on 20/05/15.
 */
public class AlginmentBenchmark {
    private static final Unsafe UNSAFE = getUnsafe();
    public static final long NUMBER_OF_READS = 1000_000000;

    @Test
    public void testUnaligned() {
        // | long 8 | short: 2 | byte 1 | int 4 | short: 2 (x) | byte: 1 | byte: 1

        long startAddress = UNSAFE.allocateMemory(19);
        UNSAFE.setMemory(startAddress, 19, (byte) 0);

        UNSAFE.putLong(startAddress, 5l);
        UNSAFE.putShort(startAddress + 8, (short) 100);
        UNSAFE.putByte(startAddress + 10, (byte) 1);
        UNSAFE.putInt(startAddress + 11, 54);
        UNSAFE.putShort(startAddress + 13, (short) 2);
        UNSAFE.putByte(startAddress + 15, (byte) 5);
        UNSAFE.putByte(startAddress + 16, (byte) 5);

        long startRead = System.currentTimeMillis();
        long b = 0;
        for (long i = 0; i < NUMBER_OF_READS; i++) {
            b += UNSAFE.getShort(startAddress + 15);
        }
        long endRead = System.currentTimeMillis();
        System.out.println("unaligned: " + (endRead - startRead) + " ms");
        System.out.println(b);
    }

    @Test
    public void testUnalignedWithJumps() {
        // | long 8 | short: 2 (x) | byte 1 | int 4 | short: 2 (x) | byte: 1 | byte: 1

        long startAddress = UNSAFE.allocateMemory(19);
        UNSAFE.setMemory(startAddress, 19, (byte) 0);

        UNSAFE.putLong(startAddress, 5l);
        UNSAFE.putShort(startAddress + 8, (short) 100);
        UNSAFE.putByte(startAddress + 10, (byte) 1);
        UNSAFE.putInt(startAddress + 11, 54);
        UNSAFE.putShort(startAddress + 13, (short) 2);
        UNSAFE.putByte(startAddress + 15, (byte) 5);
        UNSAFE.putByte(startAddress + 16, (byte) 5);

        long startRead = System.currentTimeMillis();
        long b = 0;
        for (long i = 0; i < NUMBER_OF_READS; i++) {
            b += UNSAFE.getShort(startAddress + 15);
            b += UNSAFE.getShort(startAddress + 8);
        }
        long endRead = System.currentTimeMillis();
        System.out.println("unaligned with jumps: " + (endRead - startRead) + " ms");
        System.out.println(b);
    }

    @Test
    public void testAligned() {
        // | long 8 | short: 2 (x) | byte 2 | int 4 | short: 2 (x) | byte: 4 | byte: 2

        long startAddress = UNSAFE.allocateMemory(24);
        UNSAFE.setMemory(startAddress, 24, (byte) 0);

        UNSAFE.putLong(startAddress, 5l);
        UNSAFE.putShort(startAddress + 8, (short) 5);
        UNSAFE.putShort(startAddress + 10, (byte) 7);
        UNSAFE.putInt(startAddress + 12, 756);
        UNSAFE.putShort(startAddress + 16, (short) 7);
        UNSAFE.putInt(startAddress + 20, (byte) 3);
        UNSAFE.putShort(startAddress + 22, (byte) 5);

        long startRead = System.currentTimeMillis();
        long b = 0;
        for (long i = 0; i < NUMBER_OF_READS; i++) {
            b += (byte) UNSAFE.getInt(startAddress + 16);
        }
        long endRead = System.currentTimeMillis();
        System.out.println("aligned: " + (endRead - startRead) + " ms");
        System.out.println(b);
    }

    @Test
    public void testAlignedWithJumps() {
        // | long 8 | short: 2 (x) | byte 2 | int 4 | short: 2 (x) | byte: 4 | byte: 2

        long startAddress = UNSAFE.allocateMemory(24);
        UNSAFE.setMemory(startAddress, 24, (byte) 0);

        UNSAFE.putLong(startAddress, 5l);
        UNSAFE.putShort(startAddress + 8, (short) 5);
        UNSAFE.putShort(startAddress + 10, (byte) 7);
        UNSAFE.putInt(startAddress + 12, 756);
        UNSAFE.putShort(startAddress + 16, (short) 7);
        UNSAFE.putInt(startAddress + 20, (byte) 3);
        UNSAFE.putShort(startAddress + 22, (byte) 5);

        long startRead = System.currentTimeMillis();
        long b = 0;
        for (long i = 0; i < NUMBER_OF_READS; i++) {
            b += (byte) UNSAFE.getInt(startAddress + 16);
            b += (byte) UNSAFE.getInt(startAddress + 8);

        }
        long endRead = System.currentTimeMillis();
        System.out.println("aligned with jumps: " + (endRead - startRead) + " ms");
        System.out.println(b);
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
