package com.setantamedia.fulcrum.common;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class DirectoryContentsDeleter extends SimpleFileVisitor<Path> {

   private final static Logger logger = Logger.getLogger(DirectoryContentsDeleter.class);
   private Boolean recursive = false;
   private PathMatcher filter = null;

   @Override
   public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
      FileVisitResult result = FileVisitResult.CONTINUE;
      if (recursive) {
         try {
            Files.walkFileTree(dir, this);
         } catch (Exception e) {
            logger.error("Problem processing directory: " + dir.toString());
            result = FileVisitResult.SKIP_SUBTREE;
         }
      } else {
         result = FileVisitResult.SKIP_SUBTREE;
      }
      return result;
   }

   @Override
   public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
      FileVisitResult result = FileVisitResult.CONTINUE;
      try {
         if (filter != null && filter.matches(file)) {
            Files.delete(file);
         }
      } catch (Exception e) {
         logger.error("Problem deleting file: " + file.toString());
         result = FileVisitResult.CONTINUE;
      }
      return result;
   }

   public PathMatcher getFilter() {
      return filter;
   }

   public void setFilter(PathMatcher filter) {
      this.filter = filter;
   }

   public Boolean getRecursive() {
      return recursive;
   }

   public void setRecursive(Boolean recursive) {
      this.recursive = recursive;
   }
}
