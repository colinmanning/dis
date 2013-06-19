package com.setantamedia.fulcrum.microsoft.office.onenote.custom.tim.workflow;

import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import com.setantamedia.fulcrum.microsoft.office.onenote.custom.tim.Notebook;
import com.setantamedia.fulcrum.microsoft.office.onenote.custom.tim.Page;
import com.setantamedia.fulcrum.microsoft.office.onenote.custom.tim.Section;
import com.setantamedia.fulcrum.microsoft.office.onenote.custom.tim.Sectiongroup;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class OneNoteFileProcessor extends FileProcessor {

   private static Logger logger = Logger.getLogger(OneNoteFileProcessor.class);
   private JAXBContext jaxbContext = null;
   private static Unmarshaller unmarshaller = null;
   private Notebook notebook = null;

   public OneNoteFileProcessor() {
   }

   @Override
   public void init() {
      super.init();
      try {
         jaxbContext = JAXBContext.newInstance("com.setantamedia.fulcrum.microsoft.office.onenote.custom.tim");
         unmarshaller = jaxbContext.createUnmarshaller();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void fileCreated(Path path) {
      String fileName = path.getFileName().toString();
      if (ignoreFiles.contains(fileName)) {
         return;
      }
      try {
         if (!Files.isDirectory(path) && fileName.endsWith(".xml")) {
            InputStream fileStream = Files.newInputStream(path, StandardOpenOption.READ);
            //FileInputStream fileStream = new FileInputStream(file);
            if (processFile(path.getParent(), fileStream)) {
               fileStream.close();
               Files.delete(path);
            } else {
               fileStream.close();
            }
         }
      } catch (Exception e) {
         logger.error("problem processing OneNote - " + fileName);
         e.printStackTrace();
      }
   }

   private boolean processFile(Path rootPath, InputStream fileStream) throws Exception {
      boolean result = false;
      try {
         notebook = (Notebook) unmarshaller.unmarshal(fileStream);
         Path notebookFolder = destination.resolve(notebook.getTitle());
         if (!Files.exists(notebookFolder)) {
            Files.createDirectories(notebookFolder);
         }

         /*
          * Pricess the main section
          */
         Section mainSection = notebook.getSection();
         Path mainSectionFolder = notebookFolder.resolve(mainSection.getTitle());
         if (!Files.exists(mainSectionFolder)) {
            Files.createDirectories(mainSectionFolder);
         }
         for (Page page : mainSection.getPage()) {
            String path = page.getPath();
            if ("".equals(path)) {
               continue;
            }
            Path pageFile = rootPath.resolve(path);
            if (!Files.exists(pageFile)) {
               continue;
            }
            Path destFile = mainSectionFolder.resolve(path);
            if (Files.exists(destFile)) {
               Files.delete(destFile);
            }
            Files.move(pageFile, destFile);
         }

         /*
          * Pricess the section groups
          */
         for (Sectiongroup sectionGroup : notebook.getSectiongroup()) {
            Path sectionGroupFolder = notebookFolder.resolve(sectionGroup.getTitle());
            if (!Files.exists(sectionGroupFolder)) {
               Files.createDirectories(sectionGroupFolder);
            }
            for (Section section : sectionGroup.getSection()) {
               Path sectionFolder = sectionGroupFolder.resolve(section.getTitle());
               if (!Files.exists(sectionFolder)) {
                  Files.createDirectories(sectionFolder);
               }
               for (Page page : section.getPage()) {
                  String path = page.getPath();
                  if ("".equals(path)) {
                     continue;
                  }
                  Path pageFile = rootPath.resolve(path);
                  if (!Files.exists(pageFile)) {
                     continue;
                  }
                  Path destFile = sectionFolder.resolve(path);
                  if (Files.exists(destFile)) {
                     Files.delete(destFile);
                  }
                  Files.move(pageFile, destFile);
               }
            }
         }
         result = true;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   @Override
   public void fileDeleted(Path file) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void fileModified(Path file) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void directoryCreated(Path directory) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void directoryDeleted(Path directory) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void directoryModified(Path directory) {
      throw new UnsupportedOperationException("Not supported yet.");
   }
   
   @Override
   public void terminate() {

   }
}
