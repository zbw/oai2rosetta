package utils;

import java.io.*;
import java.net.*;

/**
 * Created by Ott Konstantin on 26.08.2014.
 */
public class ResourceUtils {


    public static void getResource(String url, String outdir, String filename) throws URISyntaxException, IOException, CorruptFileException,FileNotFoundException {
        //URI inputURI = new URI(url);
        URL inputURL = getCleanURL(url);
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

    public static boolean existSource(String url) throws IOException {
        URL u = getCleanURL(url);
        HttpURLConnection huc =  (HttpURLConnection)  u.openConnection();
        huc.setRequestMethod("HEAD");
        huc.setInstanceFollowRedirects(true);
        int status = huc.getResponseCode();
        return (status == HttpURLConnection.HTTP_OK);
    }
    private static URL getCleanURL(String url) throws MalformedURLException {
        //maybe we need some cleening here
        URL cleanurl = new URL(url);
        return cleanurl;
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
