package com.zero.bandaid.patch;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by chaopei on 2016/7/22.
 * 类替换的patch
 */
public class ClassPatch extends Patch {

    private DexClassLoader mDexClassLoader;
    private Object mNewDexElements;

    ClassPatch(Info info) {
        super(info);
    }

    @Override
    public boolean init() {
        mDexClassLoader = new DexClassLoader(getPath(), getOdexPath(), getPath(), getPathClassLoader());
        try {
            mNewDexElements = getDexElements(getPathList(mDexClassLoader));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean apply() {
        try {
            Object baseDexElements = getDexElements(getPathList(getPathClassLoader()));
            Object allDexElements = combineArray(mNewDexElements, baseDexElements);
            Object pathList = getPathList(getPathClassLoader());
            setField(pathList, pathList.getClass(), "dexElements", allDexElements);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static PathClassLoader getPathClassLoader() {
        PathClassLoader pathClassLoader = (PathClassLoader) ClassPatch.class.getClassLoader();
        return pathClassLoader;
    }

    private static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }

    private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object combineArray(Object firstArray, Object secondArray) {
        Class<?> localClass = firstArray.getClass().getComponentType();
        int firstArrayLength = Array.getLength(firstArray);
        int allLength = firstArrayLength + Array.getLength(secondArray);
        Object result = Array.newInstance(localClass, allLength);
        for (int k = 0; k < allLength; ++k) {
            if (k < firstArrayLength) {
                Array.set(result, k, Array.get(firstArray, k));
            } else {
                Array.set(result, k, Array.get(secondArray, k - firstArrayLength));
            }
        }
        return result;
    }

    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static void setField(Object obj, Class<?> cl, String field, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }
}
