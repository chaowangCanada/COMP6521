package yuanwen;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by qinyu on 2017-02-01.
 */
class FileBuffer {
    public BufferedReader fileBufferReader;
    private String cache;

    public FileBuffer(BufferedReader bufferedReader) throws IOException {
        this.fileBufferReader = bufferedReader;
        read();
    }

    public void close() throws IOException {
        this.fileBufferReader.close();
    }

    public boolean empty() {
        return this.cache == null;
    }

    public String peek() {
        return this.cache;
    }

    public String pop() throws IOException {
        String result = peek();
        read();
        return result;
    }

    private void read() throws IOException {
        this.cache = this.fileBufferReader.readLine();
    }



}
