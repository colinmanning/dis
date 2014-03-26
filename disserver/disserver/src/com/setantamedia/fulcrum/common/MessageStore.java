package com.setantamedia.fulcrum.common;

import com.setantamedia.fulcrum.previews.PreviewCache;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Based on the PreviewCache class, this class is used to store messages in a partitioned file system structure
 *
 * @author Colin Manning
 */
public class MessageStore extends PreviewCache {

   public MessageStore(Path storeDir) {
      super(storeDir);
   }

   /**
    * Return a file that will be used to store a preview for an asset do not put the field id in the path - as we use
    * the id all the time
    *
    * @param database the database name
    * @param id the record id
    * @return
    */
   public Path makeStorePath(String id) {
      String storeLocation = makePartitionName(id);
      Path storePath = cacheDir.resolve(storeLocation);
      try {
         if (!Files.exists(storePath)) {
            Files.createDirectories(storePath);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return storePath;
   }

   public Path storeMessage(String id, String body) {
      Path result = makeStorePath(id).resolve(id + "_message.txt");
      try {
         try (PrintWriter writer = new PrintWriter(result.toFile(), "UTF-8")) {
            writer.print(body);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   public String getMessageBody(String id) {
      String result = "";
      StringBuilder text = new StringBuilder();
      try {
         Path messageFile = makeStorePath(id).resolve(id + "_message.txt");
         if (Files.exists(messageFile)) {
            String NL = System.getProperty("line.separator");
            try (Scanner scanner = new Scanner(new FileInputStream(messageFile.toFile()), "UTF-8")) {
               while (scanner.hasNextLine()) {
                  //text.append("<p>").append(scanner.nextLine()).append("</p>");
                  text.append(scanner.nextLine()).append(NL);
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally{
         text.trimToSize();
         result = text.toString();
      }
      return result;
   }
}
