/**
 * Copyright (C) 2014 keybits@gmx.de This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.github.chrbayer84.keybits;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.spongycastle.crypto.params.KeyParameter;

import com.google.bitcoin.core.AbstractWalletEventListener;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.CheckpointManager;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Peer;
import com.google.bitcoin.core.PeerAddress;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionConfidence;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.SendRequest;
import com.google.bitcoin.core.Wallet.SendResult;
import com.google.bitcoin.crypto.KeyCrypterScrypt;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.SPVBlockStore;

public class MyWallet
{
    private final String passphrase;

    private File wallet_file;

    private final String wallet_file_name;

    private String blockchain_file_name;

    private String checkpoints_file_name;

    private Wallet wallet;

    private BlockChain blockchain;

    private SPVBlockStore block_store;

    private NetworkParameters net_params;

    private PeerGroup peer_group;

    private KeyCrypterScrypt key_crypter;

    private KeyParameter key_parameter;

    private boolean waiting;

    private boolean is_new_wallet = false;

    public MyWallet( String wallet_file_name )
    {
        this.wallet_file_name = wallet_file_name;
        this.passphrase = null;
    }

    /**
     * Constructor.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     */
    public MyWallet( String wallet_file_name, String blockchain_file_name )
    {
        this.wallet_file_name = wallet_file_name;
        this.blockchain_file_name = blockchain_file_name;
        this.passphrase = null;
    }

    /**
     * Constructor.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param passphrase The passphrase for encryption and decryption.
     */
    public MyWallet( String wallet_file_name, String blockchain_file_name, String passphrase )
    {
        this.wallet_file_name = wallet_file_name;
        this.blockchain_file_name = blockchain_file_name;
        this.passphrase = passphrase;
    }

    public MyWallet( String wallet_file_name, String blockchain_file_name, String checkpoints_file_name,
                     String passphrase )
    {
        this.wallet_file_name = wallet_file_name;
        this.blockchain_file_name = blockchain_file_name;
        this.checkpoints_file_name = checkpoints_file_name;
        this.passphrase = passphrase;
    }

    /**
     * Sets if code should wait for some event.
     * 
     * @param waiting True if waiting, false otherwise.
     */
    public void setWaiting( boolean waiting )
    {
        this.waiting = waiting;
    }

    /**
     * Gets if code is waiting for some event.
     * 
     * @return True if waiting, false otherwise.
     */
    public boolean getWaiting()
    {
        return this.waiting;
    }

    /**
     * Gets file name of wallet.
     * 
     * @return File name of wallet.
     */
    public String getWalletFileName()
    {
        return this.wallet_file_name;
    }

    /**
     * Gets file name of blockchain.
     * 
     * @return File name of blockchain.
     */
    public String getBlockchainFileName()
    {
        return this.blockchain_file_name;
    }

    /**
     * Gets wallet.
     * 
     * @return Wallet.
     */
    public Wallet getWallet()
    {
        return this.wallet;
    }

    /**
     * Gets blockchain.
     * 
     * @return Blockchain.
     */
    public BlockChain getBlockchain()
    {
        return this.blockchain;
    }

    /**
     * Gets blockstore.
     * 
     * @return Blockstore.
     */
    public SPVBlockStore getBlockStore()
    {
        return this.block_store;
    }

    /**
     * Gets network parameters.
     * 
     * @return Network parameters.
     */
    public NetworkParameters getNetworkParameters()
    {
        return this.net_params;
    }

    public void setCheckpointsFileName( String checkpoints_file_name )
    {
        this.checkpoints_file_name = checkpoints_file_name;
    }

    public String getCheckpointsFileName()
    {
        return this.checkpoints_file_name;
    }

    /**
     * Opens wallet, if wallet does not exist create one address.
     * 
     * @throws Exception Throws exception if something goes wrong.
     */
    public void openWallet()
        throws Exception
    {
        this.openWallet( 1 );
    }

    /**
     * Opens wallet, if wallet does not exist create n addresses.
     * 
     * @param n The number of addresses to create.
     * @throws Exception Throws exception if something goes wrong.
     */
    public void openWallet( int n )
        throws Exception
    {
        this.net_params = new MainNetParams();

        // Try to read the wallet from storage, create a new one if not possible.
        this.wallet_file = new File( this.wallet_file_name );

        if ( this.wallet_file.exists() )
        {
            this.wallet = Wallet.loadFromFile( this.wallet_file );
        }
        else
        {
            this.wallet = new Wallet( this.net_params );
            // create n keys
            for ( int i = 0; i < n; i++ )
                this.wallet.addKey( new ECKey() );

            this.is_new_wallet = true;
        }

        this.decrypt();
    }

    public void openBlockStore()
        throws Exception
    {
        // data structure for block chain storage in file, also see:
        // https://code.google.com/p/bitcoinj/source/browse/core/src/main/java/com/google/bitcoin/kits/WalletAppKit.java?r=ee0b56180b1826313a22781fc652101a68ea7f17

        File blockchain_file = new File( this.blockchain_file_name );

        // if there is no blockchain file, there exists a checkpoints file and the wallet is,
        // then use the checkpoints file for downloading the chain from the last checkpoint
        if ( !blockchain_file.exists() && this.checkpoints_file_name != null && this.is_new_wallet )
        {
            // if blockstore does not exist, set key creation time to current time since we do not want to download the
            // whole chain
            Iterator<ECKey> iterator = this.wallet.getKeys().iterator();
            while ( iterator.hasNext() )
            {
                ECKey key = iterator.next();
                key.setCreationTimeSeconds( System.currentTimeMillis() );
            }

            File checkpoint_file = new File( this.checkpoints_file_name );
            FileInputStream stream = new FileInputStream( checkpoint_file );

            this.block_store = new SPVBlockStore( this.net_params, blockchain_file );
            CheckpointManager.checkpoint( this.net_params, stream, this.block_store,
                this.wallet.getEarliestKeyCreationTime() );
        }
        else
            this.block_store = new SPVBlockStore( this.net_params, blockchain_file );

        this.blockchain = new BlockChain( this.net_params, this.wallet, this.block_store ); // initialize BlockChain
                                                                                            // object
    }

    public boolean removeKey( String address )
    {
        List<ECKey> list = this.getWallet().getKeys();
        for ( int i = 0; i < list.size(); i++ )
        {
            ECKey key = list.get( i );
            Address address_ = key.toAddress( this.getNetworkParameters() );
            if ( address_.toString().equals( address ) )
                this.getWallet().removeKey( key );
        }
        return false;
    }

    public void setCreationTime( long epoch )
    {
        Iterator<ECKey> iterator = this.wallet.getKeys().iterator();
        while ( iterator.hasNext() )
        {
            ECKey key = iterator.next();
            key.setCreationTimeSeconds( epoch );
        }
    }

    /**
     * Opens peer group.
     * 
     * @throws Exception Throws exception if something goes wrong.
     */
    public void openPeerGroup()
        throws Exception
    {
        // initialize peer group for synchronyzing the block chain
        this.peer_group = new PeerGroup( this.net_params, this.blockchain );
        this.peer_group.addAddress( new PeerAddress( InetAddress.getLocalHost() ) );
        this.peer_group.addWallet( this.wallet );
        this.peer_group.start();
    }

    public Block getBlockFromPeer( Sha256Hash block_hash )
        throws Exception
    {
        this.updateWallet();
        Peer peer = this.peer_group.getDownloadPeer();
        Block block = peer.getBlock( block_hash ).get();

        return block;
    }

    public Transaction getTransactionFromBlock( Block block, Sha256Hash transaction_hash )
        throws Exception
    {
        List<Transaction> list = block.getTransactions();
        for ( int i = 0; i < list.size(); i++ )
        {
            Transaction transaction = list.get( i );
            if ( transaction.getHash().equals( transaction_hash ) )
                return transaction;
        }

        return null;
    }

    public String[] getOutputAddressesFromTransaction( Transaction transaction )
        throws Exception
    {
        List<TransactionOutput> list = transaction.getOutputs();

        // minus one because is the address for change
        String[] ret = new String[list.size() - 1];
        for ( int i = 0; i < list.size() - 1; i++ )
        {
            TransactionOutput output = list.get( i );
            ret[i] = output.getScriptPubKey().getToAddress( this.getNetworkParameters() ).toString();
        }

        return ret;
    }

    /**
     * Update wallet.
     * 
     * @throws Exception Throws exception if something goes wrong.
     */
    public void updateWallet()
        throws Exception
    {
        // Now download and process the block chain.
        this.peer_group.setFastCatchupTimeSecs( this.wallet.getEarliestKeyCreationTime() );
        this.peer_group.addPeerDiscovery( new DnsDiscovery( this.net_params ) );
        this.peer_group.downloadBlockChain();
    }

    /**
     * Send amount of coins to address.
     * 
     * @param address The address.
     * @param amount Amount of coins to send.
     * @return The corresponding transaction.
     * @throws Exception Throws exception if something goes wrong.
     */
    public Transaction send( String address, BigInteger amount )
        throws Exception
    {
        Address target_address = new Address( this.wallet.getNetworkParameters(), address );
        SendResult result = this.wallet.sendCoins( this.peer_group, target_address, amount );
        result.broadcastComplete.get();

        return result.tx;
    }

    /**
     * Send amounts of coins to addresses.
     * 
     * @param addresses The addresses.
     * @param amounts Amounts of coins to send.
     * @return The corresponding transaction.
     * @throws Exception Throws exception if something goes wrong.
     */
    public Transaction sendMultiple( String[] addresses, BigInteger[] amounts )
        throws Exception
    {
        Address[] target_addresses = new Address[addresses.length];
        for ( int i = 0; i < addresses.length; i++ )
            target_addresses[i] = new Address( this.net_params, addresses[i] );
        Transaction transaction = new Transaction( this.net_params );
        for ( int i = 0; i < addresses.length; i++ )
            transaction.addOutput( amounts[i], target_addresses[i] );

        SendRequest request = SendRequest.forTx( transaction );
        this.wallet.completeTx( request );
        this.wallet.commitTx( request.tx );
        this.peer_group.broadcastTransaction( request.tx ).get();

        return transaction;
    }

    /**
     * Close wallet.
     * 
     * @throws Exception Throws exception if something goes wrong.
     */
    public void closeWallet()
        throws Exception
    {
        this.encrypt();
        this.wallet.saveToFile( this.wallet_file ); // save wallet contents to disk
    }

    public void closeBlockStore()
        throws Exception
    {
        this.block_store.close(); // close block chain storage object (and corresponding file)
    }

    /**
     * Close peer group.
     */
    public void closePeerGroup()
    {
        this.peer_group.stop();
    }

    /**
     * Returns some information about wallet.
     */
    public void someOutput()
    {
        // get some information of current wallet
        ECKey first_key = this.wallet.getKeys().get( 0 );
        System.out.println( "First key in the wallet:\n" + first_key );
        System.out.println( "Complete content of the wallet:\n" + this.wallet );
        if ( this.wallet.isPubKeyHashMine( first_key.getPubKeyHash() ) )
        {
            System.out.println( "Yep, that's my key." );
        }
        else
        {
            System.out.println( "Nope, that key didn't come from this wallet." );
        }
    }

    /**
     * Encrypts wallet.
     */
    private void encrypt()
    {
        if ( this.passphrase == null )
            return;

        if ( this.wallet.isEncrypted() )
            return;

        this.key_crypter = new KeyCrypterScrypt();
        this.key_parameter = this.key_crypter.deriveKey( this.passphrase );
        this.wallet.encrypt( this.key_crypter, this.key_parameter );
    }

    /**
     * Decrypts wallet.
     */
    private void decrypt()
    {
        // if (this.wallet.isEncrypted() && this.passphrase == null)
        // this.passphrase = HelpfullStuff.insertPassphrase("decrypt wallet " + this.wallet_file.getName(), false);

        if ( this.wallet.isEncrypted() && this.passphrase == null )
            return;

        if ( !this.wallet.isEncrypted() )
            return;

        this.key_parameter = this.wallet.getKeyCrypter().deriveKey( this.passphrase );
        this.wallet.decrypt( this.key_parameter );
    }

    // --------------------------------------------------------------------------------------------
    // static methods
    // --------------------------------------------------------------------------------------------

    /**
     * Creates a wallet. Files are created if necessary.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param passphrase The passphrase for encryption and decryption.
     * @param number_of_addresses The number of addresses in wallet.
     * @throws Exception Throws an exception if something goes wrong.
     */
    public static void createWallet( String wallet_file_name, String blockchain_file_name, int number_of_addresses,
                                     String passphrase )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, null, passphrase );

        wallet.openWallet( number_of_addresses );
        wallet.closeWallet();
    }

    /**
     * Updates a wallet.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param passphrase The passphrase for encryption and decryption.
     * @throws Exception Throws an exception if something goes wrong.
     */
    public static void updateWallet( String wallet_file_name, String blockchain_file_name, String checkpoints_file_name )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, checkpoints_file_name, null );

        wallet.openWallet();
        wallet.openBlockStore();
        wallet.openPeerGroup();
        wallet.updateWallet();
        wallet.closePeerGroup();
        wallet.closeBlockStore();
        wallet.closeWallet();
    }

    public static void updateWallet( String wallet_file_name, String blockchain_file_name )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, null, null );

        wallet.openWallet();
        wallet.openBlockStore();
        wallet.openPeerGroup();
        wallet.updateWallet();
        wallet.closePeerGroup();
        wallet.closeBlockStore();
        wallet.closeWallet();
    }

    public static String[] getAddressesFromBlockAndTransaction( String wallet_file_name, String blockchain_file_name,
                                                                String checkpoints_file_name, String block_hash,
                                                                String transaction_hash )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, checkpoints_file_name, null );

        wallet.openWallet();
        wallet.openBlockStore();
        wallet.openPeerGroup();

        Block block = wallet.getBlockFromPeer( new Sha256Hash( block_hash ) );
        Transaction transaction = wallet.getTransactionFromBlock( block, new Sha256Hash( transaction_hash ) );
        String[] addresses = wallet.getOutputAddressesFromTransaction( transaction );

        wallet.closePeerGroup();
        wallet.closeBlockStore();
        wallet.closeWallet();

        return addresses;
    }

    /**
     * Gets balance of wallet.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param passhprase The passphrase for encryption and decryption.
     * @return The balance of wallet.
     * @throws Exception Throws an exception if something goes wrong.
     */
    public static BigInteger getBalanceOfWallet( String wallet_file_name, String blockchain_file_name )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name );

        wallet.openWallet();

        BigInteger ret = wallet.getWallet().getBalance();

        wallet.closeWallet();

        return ret;
    }

    public static boolean removeKey( String wallet_file_name, String blockchain_file_name, String address )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name );

        wallet.openWallet();

        boolean ret = wallet.removeKey( address );

        wallet.closeWallet();

        return ret;
    }

    public static void setCreationTime( String wallet_file_name, long epoch )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name );

        wallet.openWallet();
        wallet.setCreationTime( epoch );
        wallet.closeWallet();
    }

    /**
     * Gets address in wallet.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param index The address to return.
     * @param passphrase The passphrase for encryption and decryption.
     * @return String of address.
     * @throws Throws exception if something goes wrong.
     */
    public static String getAddress( String wallet_file_name, String blockchain_file_name, int index )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name );

        wallet.openWallet();

        String address =
            wallet.getWallet().getKeys().get( index ).toAddress( wallet.getNetworkParameters() ).toString();

        wallet.closeWallet();

        return address;
    }

    /**
     * Gets a transaction.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param hex_string The transaction hash as hex string.
     * @param passphrase The passphrase for encryption and decryption.
     * @return The corresponding transaction.
     * @throws Exception Throws exception if something goes wrong.
     */
    public static Transaction getTransaction( String wallet_file_name, String blockchain_file_name, String hex_string )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name );

        wallet.openWallet();

        Transaction transaction = wallet.getWallet().getTransaction( new Sha256Hash( hex_string ) );

        wallet.closeWallet();

        return transaction;
    }

    /**
     * Shows content of wallet.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param passphrase The passphrase for encryption and decryption.
     * @return Human readable string of content of wallet.
     * @throws Exception Throws an exception if something goes wrong.
     */
    public static String showContentOfWallet( String wallet_file_name, String blockchain_file_name )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name );

        wallet.openWallet();
        wallet.closeWallet();

        return wallet.getWallet() + "";
    }

    /**
     * Sends amount of coins from wallet file to address.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param address The address to send coins to.
     * @param amount The amount to send.
     * @param passphrase The passphrase for encryption and decryption.
     * @return The corresponding transaction.
     * @throws Exception Throws exception if something goes wrong.
     */
    public static Transaction sendCoins( String wallet_file_name, String blockchain_file_name,
                                         String checkpoints_file_name, String address, BigInteger amount,
                                         String passphrase )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, checkpoints_file_name, passphrase );

        wallet.openWallet();
        wallet.openBlockStore();
        wallet.openPeerGroup();
        wallet.updateWallet();

        BigInteger balance = wallet.getWallet().getBalance();
        if ( balance.longValue() < amount.longValue() )
            throw new Exception( "not enough funds" );

        Transaction transaction = wallet.send( address, amount );

        wallet.closePeerGroup();
        wallet.closeBlockStore();
        wallet.closeWallet();
        // wallet.someOutput();

        return transaction;
    }

    /**
     * Monitors a pending transaction. Idea is, that, after a transaction was done using, e.g. sendCoins, following
     * coding using the sending wallet should wait until transaction is not pending anymore. This is achieved with this
     * method.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param transaction The transaction which is to be monitored.
     * @param passphrase The passphrase for encryption and decryption.
     * @throws Exception Throws exception if something goes wrong.
     */
    public static void monitorPendingTransaction( String wallet_file_name, String blockchain_file_name,
                                                  String checkpoints_file_name, final Transaction transaction )
        throws Exception
    {
        final MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, checkpoints_file_name, null );

        wallet.openWallet();
        wallet.openBlockStore();
        wallet.openPeerGroup();
        wallet.updateWallet();

        wallet.setWaiting( true );
        wallet.getWallet().addEventListener( new AbstractWalletEventListener()
        {
            @Override
            public void onTransactionConfidenceChanged( Wallet w, Transaction tx )
            {
                if ( transaction.equals( tx ) )
                {
                    System.out.println( tx.toString() );
                    if ( tx.getConfidence().getConfidenceType() != TransactionConfidence.ConfidenceType.PENDING )
                        wallet.setWaiting( false );
                }
            }
        } );

        while ( wallet.getWaiting() )
            Thread.sleep( 1000 );

        wallet.closePeerGroup();
        wallet.closeBlockStore();
        wallet.closeWallet();
    }

    /**
     * Monitors pending transactions.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockhchain file.
     * @param passphrase The passphrase for encryption and decryption.
     * @throws Exception Throws an exception if something goes wrong.
     */
    public static void monitorPendingTransactions( String wallet_file_name, String blockchain_file_name,
                                                   String checkpoints_file_name )
        throws Exception
    {
        final MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, checkpoints_file_name, null );

        wallet.openWallet();
        wallet.openBlockStore();
        wallet.openPeerGroup();
        wallet.updateWallet();

        if ( wallet.getWallet().getPendingTransactions().size() > 0 )
        {
            wallet.setWaiting( true );
            wallet.getWallet().addEventListener( new AbstractWalletEventListener()
            {
                @Override
                public void onTransactionConfidenceChanged( Wallet w, Transaction tx )
                {
                    System.out.println( tx.toString() );

                    boolean exists_pending_transaction = false;
                    Set<Transaction> transactions = wallet.getWallet().getTransactions( true );
                    Iterator<Transaction> iterator = transactions.iterator();
                    while ( iterator.hasNext() )
                    {
                        Transaction transaction = iterator.next();
                        if ( tx.equals( transaction ) )
                            if ( tx.getConfidence().getConfidenceType() == TransactionConfidence.ConfidenceType.PENDING )
                                exists_pending_transaction = true;
                    }
                    wallet.setWaiting( exists_pending_transaction );
                }
            } );

            while ( wallet.getWaiting() )
                Thread.sleep( 1000 );
        }

        wallet.closePeerGroup();
        wallet.closeBlockStore();
        wallet.closeWallet();
    }

    /**
     * Monitors depth in blockchain of a transaction. Idea is, that, after a transaction was recognized by the receiving
     * wallet, the following code should wait until transaction is deep enough in blockchain, usually depth >= 6. This
     * is achieved with this method.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param transaction The transaction which is to be monitored.
     * @param depth Depth when monitoring ends.
     * @param passphrase The passphrase for encryption and decryption.
     * @throws Exception Throws exception if something goes wrong.
     */
    public static void monitorTransactionDepth( String wallet_file_name, String blockchain_file_name,
                                                String checkpoints_file_name, final Transaction transaction,
                                                final int depth )
        throws Exception
    {
        final MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, checkpoints_file_name, null );

        wallet.openWallet();
        wallet.openBlockStore();
        wallet.openPeerGroup();
        wallet.updateWallet();

        wallet.setWaiting( true );
        wallet.getWallet().addEventListener( new AbstractWalletEventListener()
        {
            @Override
            public void onTransactionConfidenceChanged( Wallet w, Transaction tx )
            {
                if ( transaction.equals( tx ) )
                {
                    System.out.println( tx.toString() );
                    if ( tx.getConfidence().getDepthInBlocks() >= depth )
                        wallet.setWaiting( false );
                }
            }
        } );

        while ( wallet.getWaiting() )
            Thread.sleep( 1000 );

        wallet.closePeerGroup();
        wallet.closeBlockStore();
        wallet.closeWallet();
    }

    /**
     * Monitors depth in blockchain of all transactions.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param depth Depth when monitoring ends.
     * @param passphrase The passphrase for encryption and decryption.
     * @throws Exception Throws exception if something goes wrong.
     */
    public static void monitorTransactionDepth( String wallet_file_name, String blockchain_file_name,
                                                String checkpoints_file_name, final int depth )
        throws Exception
    {
        final MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, checkpoints_file_name, null );

        wallet.openWallet();
        wallet.openBlockStore();
        wallet.openPeerGroup();
        wallet.updateWallet();

        boolean exists_flat_transaction = false;
        Set<Transaction> transactions = wallet.getWallet().getTransactions( true );
        // System.out.println(transactions.size());
        Iterator<Transaction> iterator = transactions.iterator();
        while ( iterator.hasNext() )
        {
            Transaction transaction = iterator.next();
            // System.out.println(transaction.getConfidence().getDepthInBlocks());
            if ( transaction.getConfidence().getDepthInBlocks() < depth
                || transaction.getConfidence().getConfidenceType() == TransactionConfidence.ConfidenceType.PENDING )
                exists_flat_transaction = true;
        }

        if ( exists_flat_transaction )
        {
            wallet.setWaiting( true );
            wallet.getWallet().addEventListener( new AbstractWalletEventListener()
            {
                @Override
                public void onTransactionConfidenceChanged( Wallet w, Transaction tx )
                {
                    boolean exists_flat_transaction = false;
                    Set<Transaction> transactions = wallet.getWallet().getTransactions( true );
                    Iterator<Transaction> iterator = transactions.iterator();
                    while ( iterator.hasNext() )
                    {
                        Transaction transaction = iterator.next();
                        if ( transaction.equals( tx ) )
                        {
                            if ( transaction.getConfidence().getDepthInBlocks() < depth
                                || transaction.getConfidence().getConfidenceType() == TransactionConfidence.ConfidenceType.PENDING )
                            {
                                System.out.println( transaction.toString() );
                                // System.out.println(transaction.getHashAsString() + "\t" +
                                // transaction.getConfidence().getDepthInBlocks());
                                exists_flat_transaction = true;
                            }
                        }
                    }
                    wallet.setWaiting( exists_flat_transaction );
                }
            } );

            while ( wallet.getWaiting() )
                Thread.sleep( 1000 );
        }

        wallet.closePeerGroup();
        wallet.closeBlockStore();
        wallet.closeWallet();
    }

    /**
     * Creates transactions representing keypair.
     * 
     * @param wallet_file_name The wallet file.
     * @param blockchain_file_name The blockchain file.
     * @param addresses An array of addresses representing keys.
     * @param amount Amount of BTCs for each address.
     * @param passphrase The passphrase for encryption and decryption.
     * @return The corresponding transaction.
     * @throws Exception Throws exception if something goes wrong.
     */
    public static Transaction createKeys( String wallet_file_name, String blockchain_file_name,
                                          String checkpoints_file_name, String[] addresses, BigInteger fee,
                                          BigInteger amount_per_address, String passphrase )
        throws Exception
    {
        MyWallet wallet = new MyWallet( wallet_file_name, blockchain_file_name, checkpoints_file_name, passphrase );

        wallet.openWallet();
        wallet.openBlockStore();
        wallet.openPeerGroup();
        wallet.updateWallet();

        int number_of_transactions = addresses.length;
        BigInteger balance = wallet.getWallet().getBalance();

        BigInteger needed =
            new BigInteger( "" + ( number_of_transactions * amount_per_address.longValue() + fee.longValue() ) );
        if ( balance.longValue() < needed.longValue() )
            throw new Exception( "not enough funds" );

        // make corresponding costs
        BigInteger[] amounts = new BigInteger[addresses.length];
        for ( int i = 0; i < addresses.length; i++ )
            amounts[i] = amount_per_address;

        // send it
        Transaction transaction = wallet.sendMultiple( addresses, amounts );

        wallet.closePeerGroup();
        wallet.closeBlockStore();
        wallet.closeWallet();

        return transaction;
    }

    /**
     * Computes fees based on the number of input and output addresses as described at http://bitcoinfees.com. Changed
     * formula because of http://bitzuma.com/posts/making-sense-of-bitcoin-transaction-fees/
     * 
     * @param number_of_inputs Number of input addresses.
     * @param number_of_outputs Number of output addresses.
     * @return Amount of satoshis for doing the transaction.
     */
    public static int getFees( int number_of_inputs, int number_of_outputs )
    {
        int bytes = 181 * number_of_inputs + 35 * number_of_outputs + 10; // 35 instead of 34 because of to small fee
                                                                          // for 112 addresses
        double kbytes = bytes / 1000.0;
        int ceil_kbytes = ( new Double( Math.ceil( kbytes ) ) ).intValue();

        return SendRequest.DEFAULT_FEE_PER_KB.intValue() * ceil_kbytes;
    }
}
