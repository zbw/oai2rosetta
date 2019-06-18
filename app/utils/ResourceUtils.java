package utils;

import play.Logger;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    public static List<String> unzip(String path, String zipFileName) throws IOException, CorruptFileException {
        return unzip(path, zipFileName, null);
    }

    public static List<String> unzip(String path, String zipFileName, String cp) throws IOException, CorruptFileException {
        List<String> resources = new ArrayList();
        ZipInputStream zis = null;
        byte[] buffer = new byte[1024];
        try {
            if (cp==null) {
                zis =
                        new ZipInputStream(new FileInputStream(path+zipFileName));
            } else {
                zis =
                        new ZipInputStream(new FileInputStream(path+zipFileName), Charset.forName("Cp437"));
                
            }
            ZipEntry ze = zis.getNextEntry();
            while(ze!=null){
                String name = ze.getName().replaceAll("/","-");

                File newFile = new File(path + File.separator + name);
                if (ze.isDirectory()) {
                    // we dont need dirs. we have files flat
                    //newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }

                        fos.close();
                        resources.add(name);
                    }
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            //System.gc();
            File delFile = new File(path+zipFileName);
            try {
                Files.delete(delFile.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } catch (IllegalArgumentException e) {
            Logger.error("unzip error: " +  e.getMessage());
            if (cp==null) {
                Logger.error("try again with codepage 437 when not done before");
                zis.closeEntry();
                zis.close();
                return unzip(path, zipFileName, "Cp437");
            } else {
                throw e;
            }
        } catch (Exception e) {
            zis.closeEntry();
            zis.close();
             e.printStackTrace();
             throw e;
        } finally {
            zis.close();
        }

        return resources;
    }


}
