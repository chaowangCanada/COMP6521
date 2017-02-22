package yuanwen;

import java.io.*;

/**
 * Created by qinyu on 2017-02-12.
 */
public class FileJoiner {
    public FileJoiner(){
    }

    public void process(String fileName1, String fileName2, String outputFileName){
        File file1 = new File(fileName1);
        File file2 = new File(fileName2);
        File outputFile = new File(outputFileName);
        FileBuffer.readTime = 0;
        try {
            joinFiles(file1, file2, outputFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("number of reads for join files is " + FileBuffer.readTime);
    }

    public void joinFiles(File file1, File file2, File outputFile ) throws IOException{
        FileBuffer.setAvailableMemory(getAvailableMemory() / 3);

        BufferedReader r1 = new BufferedReader(new InputStreamReader(
                new FileInputStream(file1), "UTF-8"));
        FileBuffer fb1 = new FileBuffer(r1);

        BufferedReader r2 = new BufferedReader(new InputStreamReader(
                new FileInputStream(file2), "UTF-8"));
        FileBuffer fb2 = new FileBuffer(r2);

        outputFile.delete();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile, true), "UTF-8"));

        try {
            while (!fb1.empty() && !fb2.empty()) {
                if (fb1.peek().length() < Configuration.LENGTH_OF_EMP_ID){
                    fb1.pop();
                    continue;
                }

                if (fb2.peek().length() < Configuration.LENGTH_OF_EMP_ID){
                    fb2.pop();
                    continue;
                }

                String id1 = fb1.peek().substring(0,Configuration.LENGTH_OF_EMP_ID);
                String id2 = fb2.peek().substring(0,Configuration.LENGTH_OF_EMP_ID);

                if (id1.equals(id2)) {
                    String line = fb1.peek() + fb2.pop();
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                } else if (id1.compareTo(id2) < 0) {
                    fb1.pop();
                } else {
                    fb2.pop();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            fb1.close();
            fb2.close();
            bufferedWriter.close();
        }
    }

    private long getAvailableMemory() {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return runtime.maxMemory() - usedMemory;
    }
}
