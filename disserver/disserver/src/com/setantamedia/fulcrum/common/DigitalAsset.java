/**
 * 
 */
package com.setantamedia.fulcrum.common;

public class DigitalAsset {

   /**
    * The asset name
    */
   public String name = "";

   /**
    * Asset action, if any used to generate the file
    */
   public String assetAction = "";
   /**
    * The actual asset data
    */
   public byte[] data = new byte[0];

   public DigitalAsset() {
   }
}
