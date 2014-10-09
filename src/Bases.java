public class Bases {
	private static char[] base16_alphabet = "0123456789ABCDEF".toCharArray();

	public static byte[] base16StringToByteArray(String s) {
	    int l = s.length();
	    byte[] ret = new byte[l/2];
	    for (int i = 0; i < l; i += 2)
	    	// get first character number in hex, divide by 16 and add second character number in hex
	        ret[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
	    
	    return ret;
	}
	
	public static String byteArrayToBase16String(byte[] bytes) {
	    char[] hex_chars = new char[2*bytes.length];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int value = bytes[j] & 0xFF;								// get both entries for hex-number, also necessary if byte has value 256 which is zero
	        hex_chars[2*j] = Bases.base16_alphabet[value >>> 4];		// divide value by 16 to get first character, shift bits four positions
	        hex_chars[2*j + 1] = Bases.base16_alphabet[value & 0x0F];	// get second entry for hex-number
	    }
	    return new String(hex_chars);
	}
}
