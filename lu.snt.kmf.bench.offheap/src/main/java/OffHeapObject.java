package unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by thomas on 20/05/15.
 */
public class OffHeapObject {
    private static final Unsafe unsafe = getUnsafe();

    public long sizeOf(Class<?> clazz) {
        long maximumOffset = 0;
        do {
            for (Field f : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    maximumOffset = Math.max(maximumOffset, unsafe.objectFieldOffset(f));
                }
            }
        }
        while ((clazz = clazz.getSuperclass()) != null);

        return maximumOffset + 8;
    }

    public void place(Object o, long address) throws Exception {
        Class clazz = o.getClass();
        do {
            for (Field f : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    long offset = unsafe.objectFieldOffset(f);
                    if (f.getType() == long.class) {
                        unsafe.putLong(address + offset, unsafe.getLong(o, offset));
                    } else if (f.getType() == int.class) {
                        unsafe.putInt(address + offset, unsafe.getInt(o, offset));
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        } while ((clazz = clazz.getSuperclass()) != null);
    }

    public Object read(Class clazz, long address) throws Exception {
        Object instance = unsafe.allocateInstance(clazz);
        do {
            for (Field f : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    long offset = unsafe.objectFieldOffset(f);
                    if (f.getType() == long.class) {
                        unsafe.putLong(instance, offset, unsafe.getLong(address + offset));
                    } else if (f.getType() == int.class) {
                        unsafe.putLong(instance, offset, unsafe.getInt(address + offset));
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        return instance;
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
}
