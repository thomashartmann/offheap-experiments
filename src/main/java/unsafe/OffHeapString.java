package unsafe;

import sun.misc.Unsafe;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

/**
 * Created by thomas on 20/05/15.
 */
public class OffHeapString {
    private static final Unsafe unsafe = getUnsafe();

    private long startIndex;
    private int size;

    public void toOffHeap(String s) throws UnsupportedEncodingException {
        size = s.length();
        startIndex = unsafe.allocateMemory(size * 8);
        unsafe.setMemory(startIndex, size * 8, (byte) 0); // initialize with zero

        byte[] bytes = s.getBytes("UTF-8");
        for (int i = 0; i < bytes.length; i++) {
            unsafe.putByte(startIndex + i * 8, bytes[i]);
        }
    }

    public String fromOffHeap(long address) throws UnsupportedEncodingException {
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = unsafe.getByte(startIndex + i * 8);
        }
        return new String(bytes, "UTF-8");
    }

    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {

            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (Unsafe) singleoneInstanceField.get(null);

        } catch (Exception e) {
            throw new RuntimeException("unsafe problems...");
        }
    }

    public static void main(String[] args) {
        try {
            OffHeapString s = new OffHeapString();
            s.toOffHeap("hui");

            System.out.println(s.fromOffHeap(s.startIndex));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
