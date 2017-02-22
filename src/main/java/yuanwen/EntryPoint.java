package yuanwen;

import java.io.IOException;
/**
 * Created by qinyu on 2017-02-06.
 */
public class EntryPoint {
    public static void main(String[] args) throws IOException{
        ExternalSortor externalSortor = new ExternalSortor();
        FileJoiner fileJoiner = new FileJoiner();
        fileJoiner.process(externalSortor.process(Configuration.INPUT_FILE_NAME_1),
                externalSortor.process(Configuration.INPUT_FILE_NAME_2), Configuration.OUTPUT_FILE_NAME);

    }
}
