package yuanwen;

import java.io.IOException;
/**
 * Created by qinyu on 2017-02-06.
 */
public class EntryPoint {
    public static void main(String[] args) throws IOException{
        ExternalSortor externalSortor = new ExternalSortor(Configuration.INPUT_FILE_NAME, Configuration.OUTPUT_FILE_NAME);
        externalSortor.process();
    }
}
