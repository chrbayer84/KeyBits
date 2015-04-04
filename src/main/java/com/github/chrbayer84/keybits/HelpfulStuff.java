/**
 *   Copyright (C) 2014
 *   keybits@gmx.de
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.txt.
 *
 */
package com.github.chrbayer84.keybits;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class HelpfulStuff {

	public static String insertPassphrase(String title) throws Exception{
		JPanel panel = new JPanel();
		JLabel label = new JLabel("enter password: ");
		JPasswordField pass = new JPasswordField(20);
		panel.add(label);
		panel.add(pass);
		String[] options = new String[]{"ok", "cancel"};
		JOptionPane.showOptionDialog(null, panel, title, JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
		return pass.getText();
	}
	
	public static String reInsertPassphrase(String title) throws Exception{
		JPanel panel = new JPanel();
		JLabel label = new JLabel("retype password: ");
		JPasswordField pass = new JPasswordField(20);
		panel.add(label);
		panel.add(pass);
		String[] options = new String[]{"ok", "cancel"};
		JOptionPane.showOptionDialog(null, panel, title, JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
		return pass.getText();
	}

	public static int yesNo(String text) throws Exception{		
	    JFrame frame = new JFrame();
	    int answer = JOptionPane.showConfirmDialog(frame, text);
	    if (answer == JOptionPane.YES_OPTION) {
	    	return 1;
	    } else if (answer == JOptionPane.NO_OPTION) {
	    	return 0;
	    }
	    return -1;
	}
	
	public static String readTextFile(String text_file_name) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(new File(text_file_name)));
		StringBuffer buffer = new StringBuffer();
		
		String line = "";
		while ((line = br.readLine()) != null)
			buffer.append(line + "\n");
		br.close();
		
		return buffer.toString();
	}
	
	public static String concatPathAndName(String path, String name){
		if (path.length() == 0)
			return name;
		
		if (path.charAt(path.length() - 1) == File.separatorChar)
			return path + name;
			
		return path + File.separatorChar + name;
	}
	
	public static HashMap<String, String> readConfigFile(String config_file_name) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(new File(config_file_name)));
		HashMap<String, String> hm = new HashMap<String, String>();
		
		String line = "";
		while ((line = br.readLine()) != null){
			if (line.charAt(0) != '#'){
				String[] split = line.split(" = ");
				hm.put(split[0], split[1]);
			}
		}
		br.close();
		
		return hm;
	}
	
	/**
	 * Concatenates two byte arrays.
	 * 
	 * @param head First array.
	 * @param tail Second array.
	 * @return Concatenation of the two arrays.
	 */
	public static byte[] concat(byte[] head, byte[] tail){
		byte[] ret = new byte[head.length + tail.length];

		System.arraycopy(head, 0, ret, 0, head.length);
		System.arraycopy(tail, 0, ret, head.length, tail.length);
		
		return ret;
	}
	
	/**
	 * Returns subarray of bytes.
	 * 
	 * @param data Array of bytes.
	 * @param offset First byte of subarray in original array.
	 * @param length Length of subarray.
	 * @return Array of bytes representing the subarray.
	 */
	public static byte[] subArray(byte[] data, int offset, int length){
		byte[] subarray = new byte[length];
		
		System.arraycopy(data, offset, subarray, 0, length);

		return subarray;
	}
	
	/**
	 * Returns int represented by four bytes.
	 * 
	 * @param b Array of four bytes.
	 * @return int represented by the four bytes.
	 */
	public static int byteArrayToInt(byte[] b) 
	{
	    return  b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}

	/**
	 * Returns four bytes represented by int.
	 * 
	 * @param a int.
	 * @return Four bytes representd by int.
	 */
	public static byte[] intToByteArray(int a)
	{
	    return new byte[] {
	        (byte) ((a >> 24) & 0xFF),
	        (byte) ((a >> 16) & 0xFF),   
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
	
	/**
	 * Xors two byte arrays.
	 * 
	 * @param data First array.
	 * @param xork Second array
	 * @return Result of xoring the two arrays.
	 */
	public static byte[] xor(byte[] data, byte[] xork){
		byte[] ret = new byte[data.length];
		
		int block_size = xork.length;
		int block_number = data.length/block_size;
		int rest = data.length%block_size;
		
		for (int i = 0; i < block_number; i++)
			for (int j = 0; j < block_size; j++)
				ret[i*block_size + j] = (byte) (data[i*block_size + j]^xork[j]);
		
		for (int j = 0; j < rest; j++)
			ret[block_number*block_size + j] = (byte) (data[block_number*block_size + j]^xork[j]);
		
		return ret;
	}
	
	/**
	 * Tests for equal arrays of bytes.
	 * 
	 * @param data1 First array.
	 * @param data2 Second array.
	 * @return True if equal, false otherwise.
	 */
	public static boolean equalArrays(byte[] data1, byte[] data2){
		if (data1.length != data2.length)
			return false;
		
		for (int i = 0; i < data1.length; i++)
			if (data1[i] != data2[i])
				return false;
		
		return true;
	}
	
	/**
	 * Returns randomly drawn byte array.
	 * 
	 * @param size Size of randomly drawn array.
	 * @return Randomly drawn byte array.
	 */
	public static byte[] getXorKey(int size){
		byte[] ret = new byte[size];
		(new Random()).nextBytes(ret);
		return ret;
	}
}
