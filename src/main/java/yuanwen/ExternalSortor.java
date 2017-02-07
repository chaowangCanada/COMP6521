package yuanwen;

import java.io.*;
import java.util.*;

/**
 * Created by qinyu on 2017-01-31.
 */
public class ExternalSortor {
    private String inputFileName;
    private String outputFileName;

    public ExternalSortor(String inputFileName, String outputFileName){
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;

    }

    public void process() throws IOException{
        long availableMemory = getAvailableMemory();
        File inputFile = new File(Configuration.INPUT_FILE_PATH,inputFileName);
        File outputFile = new File(outputFileName);
        List<File> tmpFileList = splitAndSort(inputFile, availableMemory);
        mergeSortedTmpFiles(tmpFileList, outputFile);
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
                    files.add(sortAndSaveToTmp(tmpList));
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
            files.add(sortAndSaveToTmp(tmpList));
            tmpList.clear();
        }

        return files;
    }


    private File sortAndSaveToTmp(List<String> tmpList)
            throws IOException {

        Collections.sort(tmpList, idComparator);
        File tmpFile = File.createTempFile(inputFileName, null, new File(Configuration.TMP_FILE_PATH));
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


    private int mergeSortedTmpFiles(List<File> fileList, File outputFile)
            throws IOException {
        ArrayList<FileBuffer> fileBufferList = new ArrayList<>();
        for (File file : fileList) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));

            FileBuffer fileBuffer = new FileBuffer(reader);
            fileBufferList.add(fileBuffer);
        }
        outputFile.delete();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile, true), "UTF-8"));
        int rowNumber = merge(writer, fileBufferList);

        for (File file : fileList) {
            file.delete();
        }
        return rowNumber;
    }


    private int merge(BufferedWriter fileBufferWriter, List<FileBuffer> buffers) throws IOException {
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
        int rowNumber = 0;
        try {
            while (pq.size() > 0) {
                FileBuffer fileBuffer = pq.poll();
                String line = fileBuffer.pop();
                fileBufferWriter.write(line);
                fileBufferWriter.newLine();
                ++rowNumber;
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
        return rowNumber;
    }

    private long getStringSize(String str) {
        return (str.length() * 2) + Configuration.OBJECT_OVERHEAD;
    }
}
