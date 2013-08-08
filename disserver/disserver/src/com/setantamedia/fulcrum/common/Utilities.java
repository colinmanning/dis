package com.setantamedia.fulcrum.common;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Utilities {

   public final static int BUFFER_SIZE = 16 * 1024;
   public final static char WINDOWS_SEPARATOR = '\\';


   public static String generateGuid() {
      String result = UUIDUtil.combUUID().toString();
      return result;
   }

   public static String generatePassword() {
      String result = null;
      Long timeNow = Calendar.getInstance().getTimeInMillis();
      if ((timeNow / 2) == 0) {
         result = getRandomChars(8);
      } else {
         result = getRandomChars(7);
      }
      return result;
   }


   private static char[] randomChars = "NP7MOn3bde6yfgchijlmkopqrstuvwSxzABDEFaQGHIJKL540RS98TVWXYZ1U2".toCharArray();

   public static String getRandomChars(int count) {
      Random r = new Random(System.currentTimeMillis());
      char[] id = new char[count];
      for (int i = 0; i < count; i++) {
         id[i] = randomChars[r.nextInt(randomChars.length)];
      }
      return new String(id);
   }

   /**
    * Revert normalised field names to original field name.
    *
    * " " => "_" "(" => "_LRB_" ")" => "_RRB_" "[" => "_LSB_" "]" => "_RSB_" "/" => "_FWD_" "\" => "_BWD_" "-" =>
    * "_HYP_"
    *
    * @param fieldName
    * @return
    */
   public static String denormaliseFieldName(String fieldName) {
      return fieldName.replaceAll("_LRB_", "\\(").replaceAll("_RRB_", "\\)").replaceAll("_LSB_", "\\[").replaceAll("_RSB_", "\\]").replaceAll("_FWD_", "\\/").replaceAll("_BWD_", "\\\\").replaceAll("_HYP_", "\\-").replaceAll("_", " ");
   }

   public static Locale getLocale(String locale) {
      return (locale != null && !"".equals(locale)) ? new Locale(locale) : Locale.getDefault();
   }

   public static String firstUpper(String value) {
      String result = value;
      if (Character.isLowerCase(result.charAt(0))) {
         char[] cs = result.toCharArray();
         cs[0] = Character.toUpperCase(cs[0]);
         result = String.valueOf(cs);
      }

      return result;
   }

   public static String firstLower(String value) {
      String result = value;
      if (Character.isUpperCase(result.charAt(0))) {
         char[] cs = result.toCharArray();
         cs[0] = Character.toLowerCase(cs[0]);
         result = String.valueOf(cs);
      }
      return result;
   }

   public static Locale getLocale() {
      return Locale.getDefault();
   }

   public static byte[] getBytesFromFile(File file) throws IOException {
      return getBytesFromInputStream(new FileInputStream(file));
   }

   public static byte[] getBytesFromPath(Path file) throws IOException {
      return getBytesFromInputStream(Files.newInputStream(file));
   }

   public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
      int count = 0;
      int totalCount = 0;
      byte[] buffer = new byte[BUFFER_SIZE];
      ArrayList<byte[]> dataBits = new ArrayList<byte[]>();
      while (true) {
         count = is.read(buffer);
         if (count == 0) {
            continue;
         } else if (count == -1) {
            // try again in case eof character in the stream
            count = is.read(buffer);
            if (count == -1) {
               break;
            }
         }
         // Java 6 specific
         dataBits.add(Arrays.copyOf(buffer, count));
         // dataBits.add(buffer.clone());
         totalCount += count;
      }
      is.close();
      ByteArrayOutputStream bos = new ByteArrayOutputStream(totalCount);
      for (byte[] dataBit : dataBits) {
         bos.write(dataBit, 0, dataBit.length);
      }
      return bos.toByteArray();
   }

   public static String convertStreamToString(InputStream is) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();

      String line = null;
      try {
         while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
         }
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try {
            is.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return sb.toString();
   }
    /*
     * Parse a web request url path and split into the parts
     */
    public static String[] getPathElements(String pathInfo) {
      String[] result = null;
      if (pathInfo != null) {
         String decodedURL;
         try {
            decodedURL = URLDecoder.decode(pathInfo, "UTF-8");
            result = decodedURL.split("/");
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
         }
      }
      return result;
   }

   public static void savePreview(Path file, byte[] data) {
      try {
         OutputStream fos = Files.newOutputStream(file);
         fos.write(data);
         fos.flush();
         fos.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static DigitalAsset buildAsset(File file) throws Exception {
      return buildAsset(file.getName(), new FileInputStream(file));
   }

   public static DigitalAsset buildAsset(String name, InputStream inputStream) {
      DigitalAsset result = new DigitalAsset();
      try {
         result.name = name;
         result.data = getBytesFromInputStream(inputStream);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   public static <T> T[] arrayMerge(T[]... arrays) {
      // Determine required size of new array
      int count = 0;
      for (T[] array : arrays) {
         count += array.length;
      }

      // create new array of required class
      T[] mergedArray = (T[]) Array.newInstance(arrays[0][0].getClass(), count);

      // Merge each array into new array
      int start = 0;
      for (T[] array : arrays) {
         System.arraycopy(array, 0, mergedArray, start, array.length);
         start += array.length;
      }
      return mergedArray;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   public static <T> T[] mergeArrays(T[]... arrays) {
      List list = new ArrayList();
      for (T[] array : arrays) {
         list.addAll(Arrays.asList(array));
      }
      return (T[]) Array.newInstance(arrays[0][0].getClass(), list.size());
   }

   public static boolean isTrue(String value) {
      return ((value == null) ? false : (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) || value.equalsIgnoreCase("1")) ? true : false;
   }
   private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',};

   private static String asHex(byte hash[]) {
      char buf[] = new char[hash.length * 2];
      for (int i = 0, x = 0; i < hash.length; i++) {
         buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
         buf[x++] = HEX_CHARS[hash[i] & 0xf];
      }
      return new String(buf);
   }

   /**
    *
    * @param source
    * @return
    * @throws Exception
    */
   public static String md5Tag(String source) throws Exception {
      MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
      digest.update(source.getBytes());
      return asHex(digest.digest());
   }

   /**
    * Copy a file
    *
    * @param in
    * @param out
    * @throws Exception
    */
   public static Boolean copyFile(File in, File out) throws IOException {
      Boolean result = false;
      FileChannel inChannel = new FileInputStream(in).getChannel();
      FileChannel outChannel = new FileOutputStream(out).getChannel();
      try {
         // magic number for Windows, 64Mb - 32Kb)
         int maxCount = (64 * 1024 * 1024) - (32 * 1024);
         long size = inChannel.size();
         long position = 0;
         while (position < size) {
            position += inChannel.transferTo(position, maxCount, outChannel);
         }
         result = true;
      } catch (IOException e) {
         e.printStackTrace();
         throw e;
      } finally {
         if (inChannel != null) {
            inChannel.close();
         }
         if (outChannel != null) {
            outChannel.close();
         }
      }
      return result;
   }

   public static String getPreviewMimeType(String formatName) {
      String result = "image/jpeg";
      if (formatName == null || "".equals(formatName)) {
         return result;
      }
      if ("JPG".equalsIgnoreCase(formatName) || "JPEG".equalsIgnoreCase(formatName)) {
         result = "image/jpeg";
      } else if ("GIF".equalsIgnoreCase(formatName)) {
         result = "image/gif";
      } else if ("PNG".equalsIgnoreCase(formatName)) {
         result = "image/png";
      }
      return result;
   }

   public static String replaceParameter(String source, String parameter, String value) {
      return source.replaceAll("\\$\\{" + parameter + "\\}", value);
   }

   public static String readTextFile(File aFile) {
      //...checks on aFile are elided
      StringBuilder contents = new StringBuilder();

      try {
         //use buffering, reading one line at a time
         //FileReader always assumes default encoding is OK!
         BufferedReader input = new BufferedReader(new FileReader(aFile));
         try {
            String line = null; //not declared within while loop
	        /*
             * readLine is a bit quirky : it returns the content of a line MINUS the newline. it returns null only for
             * the END of the stream. it returns an empty String if two newlines appear in a row.
             */
            while ((line = input.readLine()) != null) {
               contents.append(line);
               contents.append(System.getProperty("line.separator"));
            }
         } finally {
            input.close();
         }
      } catch (IOException ex) {
         ex.printStackTrace();
      }

      return contents.toString();
   }

   public static boolean isWindows() {
      return (File.separatorChar == WINDOWS_SEPARATOR);
   }

   public static String padRight(String s, int n) {
      return String.format("%1$-" + n + "s", s);
   }

   public static String padLeft(String s, int n) {
      return String.format("%1$" + n + "s", s);
   }
   
  
}
