import java.util.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.bitcoin.core.Base58;

public class Encoding {
	private Random random = new Random();
	
	public static void main(String[] args) throws Exception{
	}

	/**
	 * Encodes public key as addresses.
	 * 
	 * @param public_key Public key as byte array.
	 * @return Array of strings containing addresses.
	 */
	public String[] encodePublicKey(byte[] public_key){
		byte[] xork  = HelpfullStuff.getXorKey(20);
		byte[] encrypted_public_key  = HelpfullStuff.xor(public_key, xork);
		byte[] xork_and_encrypted_public_key = HelpfullStuff.concat(xork, encrypted_public_key);
		
		// add size
		byte[] bsize = HelpfullStuff.intToByteArray(xork_and_encrypted_public_key.length);
		byte[] size_and_xork_and_encrypted_public_key = HelpfullStuff.concat(bsize, xork_and_encrypted_public_key);
		
		// make blocks
		byte[][] addresses = this.encode(size_and_xork_and_encrypted_public_key , 20);
		
		// encode size
		addresses[0][0] = (byte)(addresses[0][0]^addresses[0][4]);
		addresses[0][1] = (byte)(addresses[0][1]^addresses[0][5]);
		addresses[0][2] = (byte)(addresses[0][2]^addresses[0][6]);
		addresses[0][3] = (byte)(addresses[0][3]^addresses[0][7]);
		
		String[] ret = new String[addresses.length];
		for (int i = 0; i < addresses.length; i++)
			ret[i] = Base58.encode(Hashes.byteArrayToAddress(addresses[i]));

		return ret;
	}
	
	/**
	 * Decodes public key from addresses.
	 * 
	 * @param addresses Array of strings containing addresses.
	 * @return Byte array containing public key.
	 * @throws Exception
	 */
	public byte[] decodePublicKey(String[] addresses) throws Exception{
		byte[][] keyparts = new byte[addresses.length][20];
		
		// decode laesst das erste byte zero und ist 25 bytes lang, warum auch immer ...
		for (int i = 0; i < addresses.length; i++)
			keyparts[i] = HelpfullStuff.subArray(Base58.decode(addresses[i]), 1, 20);
		
		// decode size
		keyparts[0][0] = (byte)(keyparts[0][0]^keyparts[0][4]);
		keyparts[0][1] = (byte)(keyparts[0][1]^keyparts[0][5]);
		keyparts[0][2] = (byte)(keyparts[0][2]^keyparts[0][6]);
		keyparts[0][3] = (byte)(keyparts[0][3]^keyparts[0][7]);
		
		byte[] size_and_xork_and_encrypted_public_key = this.decode(keyparts);
		byte[] xork_and_encrypted_public_key = HelpfullStuff.subArray(size_and_xork_and_encrypted_public_key , 20, size_and_xork_and_encrypted_public_key .length - 20);
		byte[] xork = HelpfullStuff.subArray(size_and_xork_and_encrypted_public_key , 0, 20);
		byte[] public_key  = HelpfullStuff.xor(xork_and_encrypted_public_key, xork);
		
		return public_key;
	}
	
	/**
	 * Encodes array of bytes as array of array of bytes where each element is an address.
	 * 
	 * @param data The array which is to be encoded.
	 * @param block_size The size of each element, should be 20.
	 * @return Array of array of bytes where each element is an address.
	 */
	private byte[][] encode(byte[] data, int block_size){
		int size = data.length;
		int block_number = size/block_size;
		int rest = size%block_size;
		
		byte[][] encoding = new byte[block_number + 1][block_size];
		
		for (int i = 0; i < block_number; i++)
			System.arraycopy(data, i*block_size, encoding[i], 0, block_size);

		if (rest > 0){
			System.arraycopy(data, block_number*block_size, encoding[block_number], 0, rest);
			
			byte[] rnd = new byte[block_size - rest];
			for (int i = 0; i < block_size - rest; i++)
				rnd[i] = (byte)this.random.nextInt(256);
			
			System.arraycopy(rnd, 0, encoding[block_number], rest, block_size - rest);
		}
		
		return encoding;
	}
	
	/**
	 * Not clear yet.
	 * 
	 * @param data
	 * @return
	 */
	public byte[] decode(byte[][] data) throws Exception{
		int size = HelpfullStuff.byteArrayToInt(new byte[]{data[0][0], data[0][1], data[0][2], data[0][3]});
		byte[] new_data = new byte[data[0].length - 4];
		
		System.arraycopy(data[0], 4, new_data, 0, new_data.length);
		data[0] = new_data;

		return this.decode_(data, size);
	}
	
	/**
	 * Not clear yet.
	 * 
	 * @param data
	 * @param size
	 * @return
	 */
	private byte[] decode_(byte[][] data, int size) throws Exception{
		if (size > 16384)
			throw new Exception("length of public string exceeds 16384 characters");
		
		byte[] decoding = new byte[size];
		
		int block_number = data.length; 
		int offset = 0;
		for (int i = 0; i < block_number; i++){
			for (int j = 0; j < data[i].length; j++){
				if (offset == size)
					break;
				
				decoding[offset] = data[i][j];
				offset++;
			}
		}
		
		return decoding;
	}
}
