package yuanwen;

import java.io.*;
import java.util.*;

/**
 * Created by qinyu on 2017-01-31.
 */
public class ExternalSortor {


    public ExternalSortor(){
    }

    public String process(String inputFileName) throws IOException{
        long availableMemory = getAvailableMemory();
        String outputFileName = "sorted_" + inputFileName + ".tmp";
        File inputFile = new File(Configuration.INPUT_FILE_PATH,inputFileName);
        File outputFile = new File(outputFileName);
        List<File> tmpFileList = splitAndSort(inputFile, availableMemory);
        mergeSortedTmpFiles(tmpFileList, outputFile);
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
        long maxBlockSize = availableMemory / 2;
        List<String> tmpList = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8")))
        {
            String line;
            long blockSize = 0;
            while ((line = reader.readLine()) != null) {
                if (blockSize >= maxBlockSize)
                {
                    files.add(sortAndSaveToTmp(file, tmpList));
                    blockSize =0;
                    tmpList.clear();
                }

                if (!line.isEmpty())
                {
                    tmpList.add(line);
                    blockSize += getStringSize(line);
                }
            }
        }
        finally
        {
            files.add(sortAndSaveToTmp(file, tmpList));
            tmpList.clear();
        }

        return files;
    }


    private File sortAndSaveToTmp(File inputFile, List<String> tmpList)
            throws IOException {

        Collections.sort(tmpList, idComparator);
        File tmpFile = File.createTempFile(inputFile.getName(), null, new File(Configuration.TMP_FILE_PATH));
        tmpFile.deleteOnExit();
        OutputStream out = new FileOutputStream(tmpFile);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
            for (String r : tmpList) {
                writer.write(r);
                writer.newLine();
            }
        }
        return tmpFile;
    }


    private void mergeSortedTmpFiles(List<File> fileList, File outputFile)
            throws IOException {
        ArrayList<FileBuffer> fileBufferList = new ArrayList<>();
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


    private void merge(BufferedWriter fileBufferWriter, List<FileBuffer> buffers) throws IOException {
        PriorityQueue<FileBuffer> pq = new PriorityQueue<>(
                11, new Comparator<FileBuffer>() {
            @Override
            public int compare(FileBuffer fb1,
                               FileBuffer fb2) {
                return idComparator.compare(fb1.peek(), fb2.peek());
            }
        });

        for (FileBuffer fileBuffer : buffers) {
            if (!fileBuffer.empty()) {
                pq.add(fileBuffer);
            }
        }
        try {
            while (pq.size() > 0) {
                FileBuffer fileBuffer = pq.poll();
                String line = fileBuffer.pop();
                fileBufferWriter.write(line);
                fileBufferWriter.newLine();
                if (fileBuffer.empty()) {
                    fileBuffer.fileBufferReader.close();
                } else {
                    pq.add(fileBuffer);
                }
            }
        } finally {
            fileBufferWriter.close();
            for (FileBuffer fileBuffer : pq) {
                fileBuffer.close();
            }
        }
    }

    private long getStringSize(String str) {
        return (str.length() * 2) + Configuration.OBJECT_OVERHEAD;
    }
}
