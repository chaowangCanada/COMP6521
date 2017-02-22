package yuanwen;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by qinyu on 2017-01-31.
 */
public class ExternalSortor {
    ArrayList<FileBuffer> fileBufferList = new ArrayList<>();


    public ExternalSortor(){
    }

    public String process(String inputFileName) throws IOException{
        long availableMemory = getAvailableMemory();
        String outputFileName = "sorted_" + inputFileName + ".tmp";
        File inputFile = new File(Configuration.INPUT_FILE_PATH,inputFileName);
        File outputFile = new File(outputFileName);
        FileBuffer.readTime = 0;
        List<File> tmpFileList = splitAndSort(inputFile, availableMemory);
        mergeSortedTmpFiles(tmpFileList, outputFile);
        System.out.println("number of reads for sorting file" + inputFileName + " is " + FileBuffer.readTime);
        return outputFileName;
    }

    private Comparator<String> idComparator = new Comparator<String>() {
        @Override
        public int compare(String r1, String r2) {
            return r1.substring(0, Configuration.LENGTH_OF_EMP_ID).compareTo(r2.substring(0, Configuration.LENGTH_OF_EMP_ID));
        }
    };

    private long getAvailableMemory() {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return runtime.maxMemory() - usedMemory;
    }

    private List<File> splitAndSort(File file, long availableMemory) throws IOException {
        List<File> files = new ArrayList<>();
        FileBuffer.availableMemory = availableMemory /2;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8")))
        {
            FileBuffer fileBuffer = new FileBuffer(reader);
            while (!fileBuffer.empty()) {
                    files.add(sortAndSaveToTmp(file, fileBuffer));
            }
            fileBuffer.close();
        }

        return files;
    }


    private File sortAndSaveToTmp(File inputFile, FileBuffer fileBuffer)
            throws IOException {

        fileBuffer.sort(idComparator);
        File tmpFile = File.createTempFile(inputFile.getName(), null, new File(Configuration.TMP_FILE_PATH));
        tmpFile.deleteOnExit();
        OutputStream out = new FileOutputStream(tmpFile);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
            while (fileBuffer.size() >= 2) {
                if (fileBuffer.size() == 1){
                    System.out.print("");
                }
                writer.write(fileBuffer.pop());
                writer.newLine();
            }
            writer.write(fileBuffer.pop());
            writer.newLine();
        }
        return tmpFile;
    }


    private void mergeSortedTmpFiles(List<File> fileList, File outputFile)
            throws IOException {

        FileBuffer.setAvailableMemory(getAvailableMemory()/(2 * fileList.size()));
        for (File file : fileList) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));

            FileBuffer fileBuffer = new FileBuffer(reader);
            fileBufferList.add(fileBuffer);
        }
        outputFile.delete();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile, true), "UTF-8"));
        merge(bufferedWriter, fileBufferList);

        for (File file : fileList) {
            file.delete();
        }
    }


    private void merge(BufferedWriter bufferedWriter, List<FileBuffer> fileBufferList) throws IOException {
        PriorityQueue<FileBuffer> pq = new PriorityQueue<>(
                11, new Comparator<FileBuffer>() {
            @Override
            public int compare(FileBuffer fb1,
                               FileBuffer fb2) {
                return idComparator.compare(fb1.peek(), fb2.peek());
            }
        });

        int bufferNum = fileBufferList.size();
        for (FileBuffer fileBuffer : fileBufferList) {
            if (!fileBuffer.empty()) {
                pq.add(fileBuffer);
            }
        }
        try {
            while (pq.size() > 0) {
                FileBuffer fileBuffer = pq.poll();
                String line = fileBuffer.pop();
                bufferedWriter.write(line);
                bufferedWriter.newLine();
                if (fileBuffer.empty()) {
                    fileBuffer.close();
                    if (bufferNum > 1){
                        FileBuffer.setAvailableMemory(FileBuffer.availableMemory * (bufferNum) / (--bufferNum));
                    }
                } else {
                    pq.add(fileBuffer);
                }
            }
        } finally {
            bufferedWriter.close();
            for (FileBuffer fileBuffer : pq) {
                fileBuffer.close();
            }
        }
    }
}
