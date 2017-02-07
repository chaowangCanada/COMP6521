package yuanwen;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by qinyu on 2017-02-01.
 */
class FileBuffer {
    public FileBuffer(BufferedReader reader) throws IOException {
        this.fileBufferReader = reader;
        reload();
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
        String answer = peek().toString();// make a copy
        reload();
        return answer;
    }

    private void reload() throws IOException {
        this.cache = this.fileBufferReader.readLine();
    }

    public BufferedReader fileBufferReader;

    private String cache;

}
