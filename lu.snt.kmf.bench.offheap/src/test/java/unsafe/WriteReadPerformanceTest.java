package unsafe;

import com.alexkasko.unsafe.offheap.OffHeapUtils;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.junit.Test;
import sun.misc.Unsafe;
import xerial.larray.LLongArray;
import xerial.larray.japi.LArrayJ;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 20/05/15.
 */
public class WriteReadPerformanceTest {
    private static final int REPETITIONS = 500;
    private static final int SIZE = 20 * 1000 * 1000;

    private static final int FREE_AFTER = 5;


    @Test
    public void primitiveLongArrayGC() {
        int write = 0;
        int read = 0;
        int allocate = 0;

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
                for (int f = 0; f < FREE_AFTER; f++) {
                    arrays.remove(arrays.iterator().next());
                }
            }

        }
        System.out.println("primitiveLongArray write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("primitiveLongArray read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);
//        System.out.println("allocate (avg / " + REPETITIONS + ") : " + ((double) allocate) / REPETITIONS);

        System.out.println("sum " + sum);

    }


    @Test
    public void unsafeLongArrayGC() {
        int write = 0;
        int read = 0;
        int allocate = 0;

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
                for (int f = 0; f < FREE_AFTER; f++) {
                    long adr = addresses.iterator().next();
                    unsafe.freeMemory(adr);
                    addresses.remove(adr);
                }
            }

        }
        System.out.println("unsafeLongArray write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("unsafeLongArray read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);
//        System.out.println("unsafeLongArray allocate (avg / " + REPETITIONS + ") : " + ((double) allocate) / REPETITIONS);

//        System.out.println("sum: " + sum);

    }

    @Test
    public void unsafeLongArray() {
        int write = 0;
        int read = 0;
        int allocate = 0;

        long sum = 0;
        for (int r = 0; r < REPETITIONS; r++) {

            Unsafe unsafe = getUnsafe();

            long start_allocating = System.currentTimeMillis();
            long start_address = unsafe.allocateMemory(((long) SIZE) * 8);
            unsafe.setMemory(start_address, (long) SIZE * 8, (byte) 0);
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

        }
        System.out.println("unsafeLongArray write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("unsafeLongArray read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);
//        System.out.println("unsafeLongArray allocate (avg / " + REPETITIONS + ") : " + ((double) allocate) / REPETITIONS);

//        System.out.println("sum: " + sum);

    }

    private long getLongMethod(Unsafe unsafe, long start, long index) {
        return unsafe.getLong(start + index * 8);
    }

    @Test
    public void primitiveLongArray() {
        int write = 0;
        int read = 0;
        int allocate = 0;

        long sum = 0;
        for (int r = 0; r < REPETITIONS; r++) {

            long start_allocating = System.currentTimeMillis();
            long[] longArray = new long[SIZE];
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

        }
        System.out.println("primitiveLongArray write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("primitiveLongArray read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);
//        System.out.println("allocate (avg / " + REPETITIONS + ") : " + ((double) allocate) / REPETITIONS);

        System.out.println("sum " + sum);

    }

    @Test
    public void lArray() {
        int write = 0;
        int read = 0;
        int allocate = 0;

        long sum = 0;
        for (int r = 0; r < REPETITIONS; r++) {

            long start_allocating = System.currentTimeMillis();
            LLongArray longArray = LArrayJ.newLLongArray(SIZE);
            long end_allocating = System.currentTimeMillis();
            allocate += (end_allocating - start_allocating);

            // writing
            long start_writing = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                longArray.update(i, i);
            }
            long end_writing = System.currentTimeMillis();
            write += (end_writing - start_writing);

            // reading
            long start_reading = System.currentTimeMillis();
            for (long i = 0; i < SIZE; i++) {
                sum += longArray.apply(i);
            }
            long end_reading = System.currentTimeMillis();
            read += (end_reading - start_reading);

            longArray.free();
        }
        System.out.println("lArray write: " + write);
        System.out.println("lArray read: " + read);

        System.out.println("lArray write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("lArray read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);
//        System.out.println("allocate (avg / " + REPETITIONS + ") : " + ((double) allocate) / REPETITIONS);

        System.out.println("sum " + sum);

    }


    @Test
    public void unsafeTools() {
        int write = 0;
        int read = 0;
        int allocate = 0;

        long sum = 0;
        for (int r = 0; r < REPETITIONS; r++) {

            long start_allocating = System.currentTimeMillis();
            com.alexkasko.unsafe.offheaplong.OffHeapLongArray longArray = new com.alexkasko.unsafe.offheaplong.OffHeapLongArray(SIZE);
            long end_allocating = System.currentTimeMillis();
            allocate += (end_allocating - start_allocating);

            // writing
            long start_writing = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                longArray.set(i, i);
            }
            long end_writing = System.currentTimeMillis();
            write += (end_writing - start_writing);

            // reading
            long start_reading = System.currentTimeMillis();
            for (long i = 0; i < SIZE; i++) {
                sum += longArray.get(i);
            }
            long end_reading = System.currentTimeMillis();
            read += (end_reading - start_reading);

            OffHeapUtils.free(longArray);
        }
        System.out.println("unsafeTools write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("unsafeTools read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);
//        System.out.println("allocate (avg / " + REPETITIONS + ") : " + ((double) allocate) / REPETITIONS);

        System.out.println("sum " + sum);

    }

    private long getPrimitiveArrayElement(long[] longArray, int index) {
        return longArray[index];
    }


    //@Test
    public void unsafeLongArray2() {
        int write = 0;
        int read = 0;

        for (int r = 0; r < REPETITIONS; r++) {
            unsafe.OffHeapLongArray directLongArray = new unsafe.OffHeapLongArray(SIZE);

            // writing
            long start_writing = System.currentTimeMillis();
            for (long i = 0; i < SIZE; i++) {
                directLongArray.setValue2(i, i);
            }
            long end_writing = System.currentTimeMillis();
            write += (end_writing - start_writing);

            assertEquals(10, directLongArray.getValue2(10L));

            // reading
            long sum = 0;
            long start_reading = System.currentTimeMillis();
            for (long i = 0; i < SIZE; i++) {
                sum += directLongArray.getValue2(i);
            }
            long end_reading = System.currentTimeMillis();
            read += (end_reading - start_reading);
            System.out.println("sum: " + sum);
        }

        System.out.println("2unsafeLongArray2 write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("2unsafeLongArray2 read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);

    }

    @Test
    public void objectLongArray() {
        int write = 0;
        int read = 0;
        int allocate = 0;

        long sum = 0;
        for (int r = 0; r < REPETITIONS; r++) {

            long start_allocating = System.currentTimeMillis();
            Long[] longArray = new Long[SIZE];
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

        }
        System.out.println("objectLongArray write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("objectLongArray read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);
        //System.out.println("allocate (avg / " + REPETITIONS + ") : " + ((double) allocate) / REPETITIONS);

        System.out.println("sum " + sum);

    }


    //@Test
    public void directByteBuffer() {
        int write = 0;
        int read = 0;

        for (int r = 0; r < REPETITIONS; r++) {

            long start_alloc = System.currentTimeMillis();
            LongBuffer b = ByteBuffer.allocateDirect(SIZE * 8).asLongBuffer();
            long end_alloc = System.currentTimeMillis();
//            System.out.println("bytebuffer alloc: " + (end_alloc - start_alloc));

            // writing
            long start_writing = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                b.put(i, Long.valueOf(i));
            }
            long end_writing = System.currentTimeMillis();
//            System.out.println("bytebuffer writing: " + (end_writing - start_writing));
            write += (end_writing - start_writing);

            assertEquals(10L, b.get(10));

            // reading
            Map<Long, Long> map = new HashMap<Long, Long>();
            long start_reading = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                map.put((long) i, b.get(i));
            }
            long end_reading = System.currentTimeMillis();
            //System.out.println("bytebuffer reading: " + (end_reading - start_reading));
            read += (end_reading - start_reading);
        }

        System.out.println("write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);

    }

    //@Test
    public void chronicleArray() throws IOException {

        int write = 0;
        int read = 0;

        for (int r = 0; r < REPETITIONS; r++) {

            File file = File.createTempFile("chronicle", "dat");
            ChronicleMapBuilder<Integer, Long> builder =
                    ChronicleMapBuilder.of(Integer.class, Long.class).entries(SIZE);
            ChronicleMap<Integer, Long> chronicleMap = builder.createPersistedTo(file);

            // writing
            long start_writing = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                chronicleMap.put(Integer.valueOf(i), Long.valueOf(i));
            }
            long end_writing = System.currentTimeMillis();
            write += (end_writing - start_writing);

            assertEquals(10, (long) chronicleMap.get(10));

            // reading
            long sum = 0;
            long start_reading = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                sum += chronicleMap.get(i);
            }
            long end_reading = System.currentTimeMillis();
            read += (end_reading - start_reading);
            System.out.println("sum: " + sum);
        }

        System.out.println("chronicle");
        System.out.println("chronicleArray write (avg / " + REPETITIONS + ") : " + ((double) write) / REPETITIONS);
        System.out.println("chronicleArray read (avg / " + REPETITIONS + ") : " + ((double) read) / REPETITIONS);

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
