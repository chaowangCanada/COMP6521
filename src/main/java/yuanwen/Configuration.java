package yuanwen;

/**
 * Created by qinyu on 2017-02-06.
 */
public interface Configuration {
    int LENGTH_OF_EMP_ID = 7;

    int OBJECT_HEADER = 16;
    int ARRAR_HEADER = 24;
    int OBJECT_REFERENCE = 8;
    int OBJECT_OVERHEAD = OBJECT_HEADER + ARRAR_HEADER +OBJECT_REFERENCE;

    String INPUT_FILE_NAME_1 = "t1.txt";
    String INPUT_FILE_NAME_2 = "t2.txt";
    String INPUT_FILE_PATH = "C:\\Users\\qinyu\\workspace\\COMP6521\\src\\main\\resources";
    String OUTPUT_FILE_NAME = "output.txt";
    String TMP_FILE_PATH = "C:\\Users\\qinyu\\workspace\\COMP6521\\src\\main\\resources";
}
