/**
 * Copyright (C) 2014 keybits@gmx.de This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.github.chrbayer84.keybits;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

public class GnuPGP
{

    public PGPPublicKeyRingCollection getPublicKeyRingCollection( String file_name )
        throws Exception
    {
        return new PGPPublicKeyRingCollection( new FileInputStream( new File( file_name ) ) );
    }

    public PGPPublicKeyRing getPublicKeyRing( PGPPublicKeyRingCollection public_key_ring_collection, String id )
    {
        PGPPublicKeyRing ret = null;

        Iterator<PGPPublicKeyRing> iterator = public_key_ring_collection.getKeyRings();
        while ( iterator.hasNext() )
        {
            PGPPublicKeyRing public_key_ring = iterator.next();
            if ( Long.toHexString( public_key_ring.getPublicKey().getKeyID() ).equals( id ) )
                return public_key_ring;
        }

        return ret;
    }

    public String showPublicKey( PGPPublicKey public_key )
        throws Exception
    {
        String ret = Long.toHexString( public_key.getKeyID() ) + "\n";
        ret = ret + "   created     : " + public_key.getCreationTime() + "\n";
        ret = ret + "   valid (days): " + public_key.getValidDays() + "\n";
        ret = ret + "   users       : ";

        Iterator<String> user_iterator = public_key.getUserIDs();
        int n = 0;
        if ( user_iterator.hasNext() )
            n = 2;

        while ( user_iterator.hasNext() )
            ret = ret + user_iterator.next() + ", ";

        ret = ret.substring( 0, ret.length() - n ) + "\n";

        return ret;
    }

    public String showPublicKeys( PGPPublicKeyRing public_key_ring )
        throws Exception
    {
        String ret = "";

        Iterator<PGPPublicKey> iterator = public_key_ring.getPublicKeys();
        while ( iterator.hasNext() )
        {
            PGPPublicKey public_key = iterator.next();

            ret = ret + this.showPublicKey( public_key ) + "\n";
        }

        return ret;
    }

    public String showPublicKeys( PGPPublicKeyRingCollection public_key_ring_collection )
        throws Exception
    {
        String ret = "";

        Iterator<PGPPublicKeyRing> iterator = public_key_ring_collection.getKeyRings();
        while ( iterator.hasNext() )
        {
            PGPPublicKeyRing public_key_ring = iterator.next();
            ret = ret + this.showPublicKeys( public_key_ring ) + "\n";
        }

        return ret;
    }

    public String getArmored( PGPPublicKeyRing public_key_ring )
        throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArmoredOutputStream armored = new ArmoredOutputStream( out );

        public_key_ring.encode( armored );
        armored.close();

        String ret = new String( out.toByteArray() );
        out.close();

        return ret;
    }

    public PGPPublicKeyRing getDearmored( String armored )
        throws Exception
    {
        ByteArrayInputStream in = new ByteArrayInputStream( armored.getBytes() );
        PGPObjectFactory factory = new PGPObjectFactory( PGPUtil.getDecoderStream( in ) );
        Object o = factory.nextObject();
        if ( o instanceof PGPPublicKeyRing )
        {
            return (PGPPublicKeyRing) o;
        }

        return null;
    }

    public byte[] getEncoded( PGPPublicKeyRing public_key_ring )
        throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        public_key_ring.encode( out );
        byte[] ret = out.toByteArray();
        out.close();

        return ret;
    }

    public PGPPublicKeyRing getDecoded( byte[] encoded )
        throws Exception
    {
        ByteArrayInputStream in = new ByteArrayInputStream( encoded );
        PGPObjectFactory factory = new PGPObjectFactory( PGPUtil.getDecoderStream( in ) );
        Object o = factory.nextObject();
        if ( o instanceof PGPPublicKeyRing )
        {
            return (PGPPublicKeyRing) o;
        }

        return null;
    }
}
