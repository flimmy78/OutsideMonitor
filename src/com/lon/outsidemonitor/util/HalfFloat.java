package com.lon.outsidemonitor.util;

public class HalfFloat {

	 public static int Halfp2Singles( float[] target,  int[] source, int numel)
     {
         
         int[] hp = source; // Type pun input as an unsigned 16-bit int
         long[] xp = new long[numel];// target; // Type pun output as an unsigned 32-bit int
         for (int i = 0; i < numel; i++)
         {
             target[i] = 0;
             xp[i] = BitConverter.toUint(BitConverter.getBytes(target[i]),0);
         }
         

      
         int h, hs, he, hm;
         long xs, xe, xm;
         int xes;
         int e;
         int next;  // Little Endian adjustment
         int checkieee = 1;  // Flag to check for IEEE754, Endian, and word size
         //double one = 1.0; // Used for checking IEEE754 floating point format

         if (checkieee != 0)
         { // 1st call, so check for IEEE754, Endian, and word size
             byte[] tmp = BitConverter.getBytes(1.0);
             long t = BitConverter.toUint(tmp, 0);
             if (t != 0)
             { // If Big Endian, then no adjustment
                 next = 0;
             }
             else
             { // If Little Endian, then adjustment will be necessary
                 next = 1;
                 t = BitConverter.toUint(tmp, 4);
             }
             if (t != 0x3FF00000)
             { // Check for exact IEEE 754 bit pattern of 1.0
                 return 1;  // Floating point bit pattern is not IEEE 754
             }
           
             checkieee = 0; // Everything checks out OK
         }

         //if( source == 0 || target == 0 ) // Nothing to convert (e.g., imag part of pure real)
         //    return 0;
         int hpIndex = 0;
         int xpIndex = 0;
         while (numel != 0)
         {
             numel--;
             h = hp[hpIndex++];
             if ((h & 0x7FFF) == 0)
             {  // Signed zero
                 xp[xpIndex++] = ((long)h) << 16;  // Return the signed zero
             }
             else
             { // Not zero
                 hs = (int)(h & 0x8000);  // Pick off sign bit
                 he = (int)(h & 0x7C00);  // Pick off exponent bits
                 hm = (int)(h & 0x03FF);  // Pick off mantissa bits
                 if (he == 0)
                 {  // Denormal will convert to normalized
                     e = -1; // The following loop figures out how much extra to adjust the exponent
                     do
                     {
                         e++;
                         hm <<= 1;
                     } while ((hm & 0x0400) == 0); // Shift until leading bit overflows into exponent bit
                     xs = ((long)hs) << 16; // Sign bit
                     xes = (int) (((long)(he >> 10)) - 15 + 127 - e); // Exponent unbias the halfp, then bias the single
                     xe = (long)(xes << 23); // Exponent
                     xm = ((long)(hm & 0x03FF)) << 13; // Mantissa
                     xp[xpIndex++] = (xs | xe | xm); // Combine sign bit, exponent bits, and mantissa bits\

                 }
                 else if (he == 0x7C00)
                 {  // Inf or NaN (all the exponent bits are set)
                     if (hm == 0)
                     { // If mantissa is zero ...
                         xp[xpIndex++] = (((long)hs) << 16) | ((long)0x7F800000); // Signed Inf
                     }
                     else
                     {
                         xp[xpIndex++] = (long)0xFFC00000; // NaN, only 1st mantissa bit set
                     }
                 }
                 else
                 { // Normalized number
                     xs = ((long)hs) << 16; // Sign bit
                     xes = (int) (((long)(he >> 10)) - 15 + 127); // Exponent unbias the halfp, then bias the single
                     xe = (long)(xes << 23); // Exponent
                     xm = ((long)hm) << 13; // Mantissa
                     xp[xpIndex] = (xs | xe | xm); // Combine sign bit, exponent bits, and mantissa bits
                     target[xpIndex] = BitConverter.toSingle(BitConverter.getBytes(xp[xpIndex]),0);
                     xpIndex++;
                 }
             }
         }
         return 0;
     }
}
