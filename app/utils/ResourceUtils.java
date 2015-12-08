package utils;

import java.io.*;
import java.net.*;

/**
 * Created by Ott Konstantin on 26.08.2014.
 */
public class ResourceUtils {


    public static void getResource(String url, String outdir, String filename) throws URISyntaxException, IOException, CorruptFileException,FileNotFoundException {
        URI inputURI = new URI(url);
        URL inputURL = inputURI.toURL();
        URLConnection ucon = inputURL.openConnection();
        try {
            InputStream inStream = ucon.getInputStream();
            BufferedInputStream binStream = new BufferedInputStream(inStream);
            File outDir = new File(outdir);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            FileOutputStream outFileStream = new FileOutputStream(outDir + "/" + filename);

            writeFile(binStream, outFileStream);
        } catch (CorruptFileException e) {
            throw new CorruptFileException(filename+ " filesize is 0B");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void writeFile(InputStream in, OutputStream out)
            throws IOException, CorruptFileException {
        byte[] buffer = new byte[1024];
        int len;
        int filesize=0;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
            filesize += len;
        }

        in.close();
        out.close();
        if (filesize == 0) {
            throw new CorruptFileException("filesize is 0B");

        }
    }
}
