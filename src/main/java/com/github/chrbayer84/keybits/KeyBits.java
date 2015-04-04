/**
 * Copyright (C) 2014 keybits@gmx.de This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.github.chrbayer84.keybits;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigInteger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import com.google.bitcoin.core.Wallet.SendRequest;

public class KeyBits
{

    /**
     * @param args
     */
    public static void main( String[] args )
        throws Exception
    {
        int number_of_addresses = 1;
        int depth = 1;

        String usage = "java -jar KeyBits.jar [options]";
        // create parameters which can be chosen
        Option help = new Option( "h", "print this message" );
        Option verbose = new Option( "v", "verbose" );
        Option exprt = new Option( "e", "export public key to blockchain" );
        Option imprt =
            OptionBuilder.withArgName( "string" ).hasArg().withDescription( "import public key from blockchain" )
                .create( "i" );

        Option blockchain_address =
            OptionBuilder.withArgName( "string" ).hasArg().withDescription( "bitcoin address" ).create( "a" );
        Option create_wallet =
            OptionBuilder.withArgName( "file name" ).hasArg().withDescription( "create wallet" ).create( "c" );
        Option update_wallet =
            OptionBuilder.withArgName( "file name" ).hasArg().withDescription( "update wallet" ).create( "u" );
        Option balance_wallet =
            OptionBuilder.withArgName( "file name" ).hasArg().withDescription( "return balance of wallet" )
                .create( "b" );
        Option show_wallet =
            OptionBuilder.withArgName( "file name" ).hasArg().withDescription( "show content of wallet" ).create( "w" );
        Option send_coins =
            OptionBuilder.withArgName( "file name" ).hasArg().withDescription( "send satoshis" ).create( "s" );
        Option monitor_pending =
            OptionBuilder.withArgName( "file name" ).hasArg()
                .withDescription( "monitor pending transactions of wallet" ).create( "p" );
        Option monitor_depth =
            OptionBuilder.withArgName( "file name" ).hasArg().withDescription( "monitor transaction depths of wallet" )
                .create( "d" );
        Option number =
            OptionBuilder.withArgName( "integer" ).hasArg().withDescription( "in combination with -c, -d, -r or -s" )
                .create( "n" );
        Option reset = OptionBuilder.withArgName( "file name" ).hasArg().withDescription( "reset wallet" ).create( "r" );

        Options options = new Options();

        options.addOption( help );
        options.addOption( verbose );
        options.addOption( imprt );
        options.addOption( exprt );

        options.addOption( blockchain_address );
        options.addOption( create_wallet );
        options.addOption( update_wallet );
        options.addOption( balance_wallet );
        options.addOption( show_wallet );
        options.addOption( send_coins );
        options.addOption( monitor_pending );
        options.addOption( monitor_depth );
        options.addOption( number );
        options.addOption( reset );

        BasicParser parser = new BasicParser();
        CommandLine cmd = null;

        String header = "This is KeyBits v0.01b.1412953962" + System.getProperty( "line.separator" );
        // show help if wrong usage
        try
        {
            cmd = parser.parse( options, args );
        }
        catch ( Exception e )
        {
            printHelp( usage, options, header );
        }

        if ( cmd.getOptions().length == 0 )
            printHelp( usage, options, header );

        if ( cmd.hasOption( "h" ) )
            printHelp( usage, options, header );

        if ( cmd.hasOption( "v" ) )
            System.out.println( header );

        if ( cmd.hasOption( "c" ) && cmd.hasOption( "n" ) )
            number_of_addresses = new Integer( cmd.getOptionValue( "n" ) ).intValue();

        if ( cmd.hasOption( "d" ) && cmd.hasOption( "n" ) )
            depth = new Integer( cmd.getOptionValue( "n" ) ).intValue();

        String checkpoints_file_name = "checkpoints";
        if ( !new File( checkpoints_file_name ).exists() )
            checkpoints_file_name = null;

        // ---------------------------------------------------------------------

        if ( cmd.hasOption( "c" ) )
        {
            String wallet_file_name = cmd.getOptionValue( "c" );

            String passphrase = HelpfulStuff.insertPassphrase( "enter password for " + wallet_file_name );
            if ( !new File( wallet_file_name ).exists() )
            {
                String passphrase_ = HelpfulStuff.reInsertPassphrase( "enter password for " + wallet_file_name );

                if ( !passphrase.equals( passphrase_ ) )
                {
                    System.out.println( "passwords do not match" );
                    System.exit( 0 );
                }
            }

            MyWallet.createWallet( wallet_file_name, wallet_file_name + ".chain", number_of_addresses, passphrase );
            System.exit( 0 );
        }

        if ( cmd.hasOption( "u" ) )
        {
            String wallet_file_name = cmd.getOptionValue( "u" );
            MyWallet.updateWallet( wallet_file_name, wallet_file_name + ".chain", checkpoints_file_name );
            System.exit( 0 );
        }

        if ( cmd.hasOption( "b" ) )
        {
            String wallet_file_name = cmd.getOptionValue( "b" );
            System.out.println( MyWallet.getBalanceOfWallet( wallet_file_name, wallet_file_name + ".chain" )
                .longValue() );
            System.exit( 0 );
        }

        if ( cmd.hasOption( "w" ) )
        {
            String wallet_file_name = cmd.getOptionValue( "w" );
            System.out.println( MyWallet.showContentOfWallet( wallet_file_name, wallet_file_name + ".chain" ) );
            System.exit( 0 );
        }

        if ( cmd.hasOption( "p" ) )
        {
            System.out.println( "monitoring of pending transactions ... " );
            String wallet_file_name = cmd.getOptionValue( "p" );
            MyWallet.monitorPendingTransactions( wallet_file_name, wallet_file_name + ".chain", checkpoints_file_name );
            System.exit( 0 );
        }

        if ( cmd.hasOption( "d" ) )
        {
            System.out.println( "monitoring of transaction depth ... " );
            String wallet_file_name = cmd.getOptionValue( "d" );
            MyWallet.monitorTransactionDepth( wallet_file_name, wallet_file_name + ".chain", checkpoints_file_name,
                depth );
            System.exit( 0 );
        }

        if ( cmd.hasOption( "r" ) && cmd.hasOption( "n" ) )
        {
            long epoch = new Long( cmd.getOptionValue( "n" ) );
            System.out.println( "resetting wallet ... " );
            String wallet_file_name = cmd.getOptionValue( "r" );

            File chain_file = ( new File( wallet_file_name + ".chain" ) );
            if ( chain_file.exists() )
                chain_file.delete();

            MyWallet.setCreationTime( wallet_file_name, epoch );
            MyWallet.updateWallet( wallet_file_name, wallet_file_name + ".chain", checkpoints_file_name );

            System.exit( 0 );
        }

        if ( cmd.hasOption( "s" ) && cmd.hasOption( "a" ) && cmd.hasOption( "n" ) )
        {
            String wallet_file_name = cmd.getOptionValue( "s" );
            String address = cmd.getOptionValue( "a" );
            Integer amount = new Integer( cmd.getOptionValue( "n" ) );

            String wallet_passphrase = HelpfulStuff.insertPassphrase( "enter password for " + wallet_file_name );
            MyWallet.sendCoins( wallet_file_name, wallet_file_name + ".chain", checkpoints_file_name, address,
                new BigInteger( amount + "" ), wallet_passphrase );

            System.out.println( "waiting ..." );
            Thread.sleep( 10000 );
            System.out.println( "monitoring of transaction depth ... " );
            MyWallet.monitorTransactionDepth( wallet_file_name, wallet_file_name + ".chain", checkpoints_file_name, 1 );
            System.out.println( "transaction fixed in blockchain with depth " + depth );
            System.exit( 0 );
        }

        // ----------------------------------------------------------------------------------------
        // creates public key
        // ----------------------------------------------------------------------------------------

        GnuPGP gpg = new GnuPGP();

        if ( cmd.hasOption( "e" ) )
        {
            BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ( ( line = input.readLine() ) != null )
                sb.append( line + "\n" );

            PGPPublicKeyRing public_key_ring = gpg.getDearmored( sb.toString() );
            // System.out.println(gpg.showPublicKeys(public_key_ring));

            byte[] public_key_ring_encoded = gpg.getEncoded( public_key_ring );

            String[] addresses = ( new Encoding() ).encodePublicKey( public_key_ring_encoded );
            // System.out.println(gpg.showPublicKey(gpg.getDecoded(encoding.decodePublicKey(addresses))));

            // file names for message
            String public_key_file_name = Long.toHexString( public_key_ring.getPublicKey().getKeyID() ) + ".wallet";
            String public_key_wallet_file_name = public_key_file_name;
            String public_key_chain_file_name = public_key_wallet_file_name + ".chain";

            // hier muss dass passwort noch nach encodeAddresses weitergeleitet werden da sonst zweimal abfrage
            String public_key_wallet_passphrase =
                HelpfulStuff.insertPassphrase( "enter password for " + public_key_wallet_file_name );
            if ( !new File( public_key_wallet_file_name ).exists() )
            {
                String public_key_wallet_passphrase_ =
                    HelpfulStuff.reInsertPassphrase( "enter password for " + public_key_wallet_file_name );

                if ( !public_key_wallet_passphrase.equals( public_key_wallet_passphrase_ ) )
                {
                    System.out.println( "passwords do not match" );
                    System.exit( 0 );
                }
            }

            MyWallet.createWallet( public_key_wallet_file_name, public_key_chain_file_name, 1,
                public_key_wallet_passphrase );
            MyWallet.updateWallet( public_key_wallet_file_name, public_key_chain_file_name, checkpoints_file_name );
            String public_key_address =
                MyWallet.getAddress( public_key_wallet_file_name, public_key_chain_file_name, 0 );
            System.out.println( "address of public key: " + public_key_address );

            // 10000 additional satoshis for sending transaction to address of recipient of message and 10000 for fees
            KeyBits.encodeAddresses( public_key_wallet_file_name, public_key_chain_file_name, checkpoints_file_name,
                addresses, 2 * SendRequest.DEFAULT_FEE_PER_KB.intValue(), depth, public_key_wallet_passphrase );
        }

        if ( cmd.hasOption( "i" ) )
        {
            String location = cmd.getOptionValue( "i" );

            String[] addresses = null;
            if ( location.indexOf( "," ) > -1 )
            {
                String[] locations = location.split( "," );
                addresses =
                    MyWallet.getAddressesFromBlockAndTransaction( "main.wallet", "main.wallet.chain",
                        checkpoints_file_name, locations[0], locations[1] );
            }
            else
            {
                addresses = BlockchainDotInfo.getKeys( location );
            }

            byte[] encoded = ( new Encoding() ).decodePublicKey( addresses );
            PGPPublicKeyRing public_key_ring = gpg.getDecoded( encoded );

            System.out.println( gpg.getArmored( public_key_ring ) );

            System.exit( 0 );
        }
    }

    public static void encodeAddresses( String wallet_file_name, String chain_file_name, String checkpoints_file_name,
                                        String[] addresses, int amount, int depth, String passphrase )
        throws Exception
    {
        // compute necessary amount of satoshis for encoding addresses using second wallet (keypair or message wallet)
        double satoshis_of_bitcoin = 100000000.0;

        int fee = MyWallet.getFees( 1, addresses.length );
        int amount_per_address = SendRequest.DEFAULT_FEE_PER_KB.intValue();
        int number_of_addresses = addresses.length;
        BigInteger needed = new BigInteger( "" + ( number_of_addresses * amount_per_address + fee + amount ) );

        // create or update second wallet and get available funds
        BigInteger available = MyWallet.getBalanceOfWallet( wallet_file_name, chain_file_name );

        System.out.println( "number of outputs: " + number_of_addresses );
        System.out.println( "available funds  : " + available + " satoshis (" + available.longValue()
            / satoshis_of_bitcoin + " XBT)" );
        System.out.println( "needed fees      : " + fee + " satoshis (" + fee / satoshis_of_bitcoin + " XBT)" );
        System.out.println( "needed funds     : " + needed + " satoshis (" + needed.longValue() / satoshis_of_bitcoin
            + " XBT)" );

        // test if there are enough funds in second wallet
        // if yes, create addresses, else move enough founds to second wallet
        if ( available.longValue() >= needed.longValue() )
        {
            createAddresses( wallet_file_name, chain_file_name, checkpoints_file_name, addresses, new BigInteger( fee
                + "" ), new BigInteger( amount_per_address + "" ), depth, passphrase );
        }
        else
        {
            // get first (and only) address in second wallet
            String address = MyWallet.getAddress( wallet_file_name, chain_file_name, 0 ); // System.out.println(address);

            BigInteger to_send = new BigInteger( "" + ( needed.longValue() - available.longValue() ) );
            System.out.println();
            System.out.println( "please send " + to_send.longValue() + " satoshis (" + to_send.longValue()
                / satoshis_of_bitcoin + " XBT) to address " + address );

            System.exit( 0 );
        }
    }

    public static void createAddresses( String wallet_file_name, String chain_file_name, String checkpoints_file_name,
                                        String[] addresses, BigInteger transaction_cost, BigInteger amount_per_address,
                                        int depth, String passphrase )
        throws Exception
    {
        if ( HelpfulStuff.yesNo( "create addresses?" ) == 1 )
        {
            try
            {
                System.out.println( "creating addresses of keys ... " );
                MyWallet.createKeys( wallet_file_name, chain_file_name, checkpoints_file_name, addresses,
                    transaction_cost, amount_per_address, passphrase );

                // wait until transaction is not pending anymore
                // System.out.println("monitoring of pending transactions ... ");
                // MyWallet.monitorPendingTransactions(usr_dir + wallet, usr_dir + chain, passphrase);

                System.out.println( "waiting ..." );
                Thread.sleep( 10000 );
                System.out.println( "monitoring of transaction depth ... " );
                MyWallet.monitorTransactionDepth( wallet_file_name, chain_file_name, checkpoints_file_name, depth );
                System.out.println( "transaction fixed in blockchain with depth " + depth );
                System.exit( 0 );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                System.exit( -1 );
            }
        }
        else
            System.exit( 0 );

    }

    public static void printHelp( String usage, Options options, String header )
    {
        System.out.println( header );
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( usage, options );
        System.exit( 0 );
    }
}
