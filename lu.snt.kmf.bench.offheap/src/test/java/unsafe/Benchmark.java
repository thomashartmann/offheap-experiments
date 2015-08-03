package unsafe;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by thomas on 20/05/15.
 */
public class Benchmark {
    private static final int REPETITIONS = 500;
    private static final int SIZE = 10 * 1000 * 1000;

    private static final int FREE_AFTER = 25;
    private static final int OBJECTS_TO_FREE = 10;


    @Test
    public void primitiveLongArrayGC() {
        int write = 0;
        int read = 0;
        int allocate = 0;

        long start = System.currentTimeMillis();

        Collection<long[]> arrays = new ArrayList<long[]>();

        long sum = 0;
        for (int r = 0; r < REPETITIONS; r++) {

            long start_allocating = System.currentTimeMillis();
            long[] longArray = new long[SIZE];
            arrays.add(longArray);
            long end_allocating = System.currentTimeMillis();
            allocate += (end_allocating - start_allocating);

            // writing
            long start_writing = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                longArray[i] = (long) i;
            }
            long end_writing = System.currentTimeMillis();
            write += (end_writing - start_writing);

            // reading
            long start_reading = System.currentTimeMillis();
            for (long i = 0; i < SIZE; i++) {
                sum += longArray[((int) i)];
            }
            long end_reading = System.currentTimeMillis();
            read += (end_reading - start_reading);

            if (r > 0 && r % FREE_AFTER == 0) {
                for (int f = 0; f < OBJECTS_TO_FREE; f++) {
                    arrays.remove(arrays.iterator().next());
                }
            }

        }

        long end = System.currentTimeMillis();

        System.out.println("primitiveLongArray write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("primitiveLongArray read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);
//        System.out.println("allocate (avg / " + REPETITIONS + ") : " + ((double) allocate) / REPETITIONS);
//        System.out.println("sum " + sum);
        System.out.println("primitiveLongArray full: " + (end - start));

    }


    @Test
    public void unsafeLongArrayGC() {
        int write = 0;
        int read = 0;
        int allocate = 0;

        long start = System.currentTimeMillis();

        Collection<Long> addresses = new ArrayList<Long>();

        long sum = 0;
        for (int r = 0; r < REPETITIONS; r++) {

            Unsafe unsafe = getUnsafe();

            long start_allocating = System.currentTimeMillis();
            long start_address = unsafe.allocateMemory(((long) SIZE) * 8);
            unsafe.setMemory(start_address, (long) SIZE * 8, (byte) 0);
            addresses.add(start_address);
            long end_allocating = System.currentTimeMillis();
            allocate += (end_allocating - start_allocating);

            // writing
            long start_writing = System.currentTimeMillis();
            for (long i = 0; i < SIZE; i++) {
                unsafe.putLong(start_address + i * 8, i);
                //unsafe.putByte(start_address + i * 8, (byte) i);
            }
            long end_writing = System.currentTimeMillis();
            write += (end_writing - start_writing);

            //assertEquals(10, unsafe.getLong(start_address + 10 * 8));

            // reading
            long start_reading = System.currentTimeMillis();
            for (long i = 0; i < SIZE; i++) {
                sum += unsafe.getLong(start_address + i * 8);
            }
            long end_reading = System.currentTimeMillis();
            read += (end_reading - start_reading);

            if (r > 0 && r % FREE_AFTER == 0) {
                for (int f = 0; f < OBJECTS_TO_FREE; f++) {
                    long adr = addresses.iterator().next();
                    unsafe.freeMemory(adr);
                    addresses.remove(adr);
                }
            }

        }

        long end = System.currentTimeMillis();

        System.out.println("unsafeLongArray write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("unsafeLongArray read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);
//        System.out.println("unsafeLongArray allocate (avg / " + REPETITIONS + ") : " + ((double) allocate) / REPETITIONS);
//        System.out.println("sum: " + sum);
        System.out.println("primitiveLongArray full: " + (end - start));

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
