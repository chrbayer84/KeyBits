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

import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.digests.WhirlpoolDigest;

public class Hashes {
	public static byte[] byteArrayToAddress(byte[] data){
		byte[] fst = new byte[data.length + 1];
		System.arraycopy(data, 0, fst, 1, data.length);

		byte[] snd = Hashes.toSHA256(fst);

		byte[] trd = Hashes.toSHA256(snd);
		
		byte[] bba = new byte[fst.length + 4];
		System.arraycopy(fst, 0, bba, 0, fst.length);
		System.arraycopy(trd, 0, bba, bba.length - 4, 4);
		
		return bba;
	}
	
	public static byte[] toRIPEMD160(byte[] in)
	{
	    byte[] out = new byte[20];
	    
	    RIPEMD160Digest digest = new RIPEMD160Digest();
	    digest.update(in, 0, in.length);
	    digest.doFinal(out, 0);
	    
	    return out;
	}
	
	public static byte[] toSHA256(byte[] in)
	{
	    byte[] out = new byte[32];
	    
	    SHA256Digest digest = new SHA256Digest();
	    digest.update(in, 0, in.length);
	    digest.doFinal(out, 0);
	    
	    return out;
	}
	
	public static byte[] toSHA512(byte[] in)
	{
	    byte[] out = new byte[64];
	    
	    SHA512Digest digest = new SHA512Digest();
	    digest.update(in, 0, in.length);
	    digest.doFinal(out, 0);
	    
	    return out;
	}
	
	public static byte[] toWhirlpool(byte[] in)
	{
	    byte[] out = new byte[64];
	    
	    WhirlpoolDigest digest = new WhirlpoolDigest();
	    digest.update(in, 0, in.length);
	    digest.doFinal(out, 0);
	    
	    return out;
	}
}
