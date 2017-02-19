package project1;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by ERIC_LAI on 2017-02-08.
 */
@SuppressWarnings("Since15")
public class NioFileReader {

    private ByteBuffer buffer;
    private FileChannel fc;
    private String cache;
    private String subCache;
    private boolean isWin;

    public NioFileReader(String path, long maxMemory) throws IOException {
        FileInputStream fin = new FileInputStream(path);
        fc = fin.getChannel();
        int bufferSize;
        if (maxMemory > Integer.MAX_VALUE) bufferSize = Integer.MAX_VALUE;
        else bufferSize = (int) maxMemory;
        buffer = ByteBuffer.allocate(bufferSize);
        fc.read(buffer);
        readPreparation();
        cache = pop();
        subCache = pop();
        isWin = getOs();
    }

    public String readLine() throws IOException {
        if (cache != null) {
            String tmp = cache;
            cache = subCache;
            subCache = pop();
            return tmp;
        }
        return null;
    }

    private String pop() throws IOException {
        return splitLine();
    }

    private String splitLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int in;
        if (isEOF()) return null;
        in = buffer.get();
        while (!isLineTermianal(in)) {
            sb.append((char) in);
            if (isEOF()) break;
            in = buffer.get();
        }
        return sb.toString();
    }

    private boolean isEOF() throws IOException {
        if (!buffer.hasRemaining()) {
            reload();
            if (!buffer.hasRemaining()) return true;
        }
        return false;
    }

    private void reload() throws IOException {
        buffer.clear();
        fc.read(buffer);
        buffer.flip();
    }

    private void readPreparation() {
        buffer.flip();
    }

    private boolean isLineTermianal(int sym) {
        if (sym == '\n') {
            // if (isWin) buffer.get();
            return true;
        }
        return false;
    }

    private boolean getOs() {
        String os = System.getProperty("os.name");
        return os.contains("Windows");
    }
}
