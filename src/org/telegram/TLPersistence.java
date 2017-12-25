package org.telegram;

import org.telegram.api.engine.Logger;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.tl.TLContext;
import org.telegram.tl.TLObject;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: ex3ndr
 * Date: 09.11.13
 * Time: 0:29
 */
public class TLPersistence<T extends TLObject> {

    private static final String TAG = "KernelPersistence";

    private Class<T> destClass;
    private T obj;

    public TLPersistence(String fileName, Class<T> destClass) {
        this.destClass = destClass;

        long start = System.currentTimeMillis();
        Logger.d(TAG, "Loaded state in " + (System.currentTimeMillis() - start) + " ms");
        obj = loadData(fileName);

        if (obj == null) {
            try {
                obj = destClass.newInstance();
            } catch (Exception e1) {
                throw new RuntimeException("Unable to instantiate default settings");
            }
        }

        afterLoaded();
    }

    protected void afterLoaded() {

    }

    public T loadData(String fileName) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(System.getProperty("user.home") + "/.telegram/security/" + fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException ignored) {

                }
        }

        return null;
    }

    public void write(String fileName) {
        FileOutputStream outputStream = null;
        try {
            File stateFile = new File(System.getProperty("user.home") + "/.telegram/security", fileName);
            if(!stateFile.exists()) {
                stateFile.getParentFile().mkdirs();
                stateFile.createNewFile();
            }
            outputStream = new FileOutputStream(stateFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException ignored) {

                }
        }
    }

    public T getObj() {
        return obj;
    }
}
