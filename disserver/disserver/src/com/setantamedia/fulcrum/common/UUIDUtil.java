package com.setantamedia.fulcrum.common;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;

public class UUIDUtil {

   public static UUID combUUID() {
      UUID srcUUID = UUID.randomUUID();
      java.sql.Timestamp ts = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());

      long upper16OfLowerUUID = zeroLower48BitsOfLong(srcUUID.getLeastSignificantBits());
      long lower48Time = zeroUpper16BitsOfLong(ts.getTime());
      long lowerLongForNewUUID = upper16OfLowerUUID | lower48Time;

      return new UUID(srcUUID.getMostSignificantBits(), lowerLongForNewUUID);
   }

   public static String base64URLSafeOfUUIDObject(UUID uuid) {
      byte[] bytes = ByteBuffer.allocate(16).putLong(0, uuid.getLeastSignificantBits()).putLong(8, uuid.getMostSignificantBits()).array();
      return Base64.encodeBase64URLSafeString(bytes);
   }

   public static String base64URLSafeOfUUIDString(String uuidString) {
      UUID uuid = UUID.fromString(uuidString);
      return UUIDUtil.base64URLSafeOfUUIDObject(uuid);
   }

   private static long zeroLower48BitsOfLong(long longVar) {
      long upper16BitMask = -281474976710656L;
      return longVar & upper16BitMask;
   }

   private static long zeroUpper16BitsOfLong(long longVar) {
      long lower48BitMask = 281474976710656L - 1L;
      return longVar & lower48BitMask;
   }
}
