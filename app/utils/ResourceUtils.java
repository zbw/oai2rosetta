package utils;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;

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


    public static String cleanUrl(String url) throws IOException {
        URL u = getCleanURL(url);
        HttpURLConnection huc =  (HttpURLConnection)  u.openConnection();
        huc.setRequestMethod("HEAD");
        /*
        dspace with cocoon makes a redirect to bitstream. When there are spaces in the filename, they arent encoded in
        the header location field. Some special chars seem to cut the filename. So whe cant just follow the redirect.
        We have to check for redirect and then take the original filename.
         */
        huc.setInstanceFollowRedirects(false);
        huc.setRequestProperty("Accept-Charset", "UTF-8");
        int status = huc.getResponseCode();
        for (Map.Entry<String, List<String>> header : huc.getHeaderFields().entrySet()) {
            System.out.println(header.getKey() + "=" + header.getValue());
        }
        if (status == HttpURLConnection.HTTP_OK) {
            return url;
        } else if (status == HttpURLConnection.HTTP_MOVED_PERM) {
            String path =  huc.getHeaderField("Location");
            String query = path.substring(path.indexOf("?")+1);
            //file = URLEncoder.encode(path.substring(path.lastIndexOf("/")+1, path.indexOf(";")));
            String file = URLEncoder.encode(u.getFile().substring(u.getFile().lastIndexOf("/")+1));
            path = path.substring(0, path.lastIndexOf("/"));
            url = u.getProtocol() + "://"+u.getHost() + path +"/"+ file + "?"+ query;
            return cleanUrl(url);
        } else {
            return null;
        }
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
