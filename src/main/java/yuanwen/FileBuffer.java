package yuanwen;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by qinyu on 2017-02-01.
 */
class FileBuffer {
    public static long availableMemory;
    public static int readTime;

    public BufferedReader fileBufferReader;
    private List<String> cache;

    public FileBuffer(BufferedReader bufferedReader) throws IOException {
        this.fileBufferReader = bufferedReader;
        cache = new ArrayList<>();
        read();
    }

    public void close() throws IOException {
        this.fileBufferReader.close();
        this.cache.clear();
    }

    public int size(){
        return cache.size();
    }

    public void sort(Comparator<String> comparator){
        Collections.sort(cache, comparator);
    }

    public boolean empty() {
        return this.cache.isEmpty();
    }

    public String peek() {
        return this.cache.get(0);
    }

    public String pop() throws IOException {
        String result = peek();
        this.cache.remove(0);
        if(this.empty()){
            read();
        }
        return result;
    }

    public static void setAvailableMemory(long availableMemory){
        FileBuffer.availableMemory = availableMemory;
    }


    private void read() throws IOException {
        String line;
        cache.clear();
        long blockSize = 0;
        if ((line = fileBufferReader.readLine()) != null) {
            cache.add(line);
            blockSize += 2 * getStringSize(line);
        }

        while ((line = fileBufferReader.readLine()) != null) {
            if (!line.isEmpty()) {
                cache.add(line);
                blockSize += getStringSize(line);
            }
            if (blockSize > FileBuffer.availableMemory) {
                break;
            }
        }
        this.readTime++;
    }

    private long getStringSize(String str) {
        return (str.length() * 2) + Configuration.OBJECT_OVERHEAD;
    }
}
