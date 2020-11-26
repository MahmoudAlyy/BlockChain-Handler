import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

// The BlockChain class should maintain only limited block nodes to satisfy the functionality.
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    /** Contains all the block currently in the blockchain, maps block hash to the block object */
    private HashMap<Integer, Block> blockchainMap ;

    /** Maps the block hash to the block height*/
    private ConcurrentHashMap<Integer, Integer> blockHeightMap;

    /** Maps the block hash to its own UTXOpool */
    private HashMap<Integer, UTXOPool> utxopoolMap;

    /** Global transaction pool */
    private TransactionPool transactionPool;
    
    
    /**
     * create an empty blockchain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        blockchainMap = new HashMap<Integer, Block>();
        blockchainMap.put(UniqueID(genesisBlock.getHash()),genesisBlock);

        blockHeightMap = new ConcurrentHashMap<Integer, Integer>();
        blockHeightMap.put(UniqueID(genesisBlock.getHash()), 1);


        UTXOPool utxoPool = new UTXOPool();

        /// add coinbase trasaction
        if (genesisBlock.getCoinbase() != null)
            utxoPool.addUTXO(new UTXO(genesisBlock.getCoinbase().getHash(),0),genesisBlock.getCoinbase().getOutput(0));

        // add rest of transactions to utxopool        
        for (int i = 0; i < genesisBlock.getTransactions().size(); i ++) {
            for (int j = 0; j <  genesisBlock.getTransactions().get(i).getOutputs().size(); j ++) {
                utxoPool.addUTXO(new UTXO(genesisBlock.getTransactions().get(i).getHash(), j),  genesisBlock.getTransactions().get(i).getOutput(j));
            }
        }

        utxopoolMap = new HashMap<Integer, UTXOPool>();
        utxopoolMap.put(UniqueID(genesisBlock.getHash()), utxoPool);

        transactionPool = new TransactionPool();

    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        Integer maxHeight = 0;
        Block maxBlock = null;
        for (Integer hash : blockHeightMap.keySet()) {
            if(blockHeightMap.get(hash) > maxHeight){
                maxHeight = blockHeightMap.get(hash);
                maxBlock = blockchainMap.get(hash);
            }
        }
        return maxBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        Block current = getMaxHeightBlock();
        return utxopoolMap.get(UniqueID(current.getHash()));
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return transactionPool;
    }

    /**
     * Add {@code block} to the blockchain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}, where maxHeight is 
     * the current height of the blockchain.
	 * <p>
	 * Assume the Genesis block is at height 1.
     * For example, you can try creating a new block over the genesis block (i.e. create a block at 
	 * height 2) if the current blockchain height is less than or equal to CUT_OFF_AGE + 1. As soon as
	 * the current blockchain height exceeds CUT_OFF_AGE + 1, you cannot create a new block at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {

        if (block.getPrevBlockHash() == null)
            return false;
    
        if (blockchainMap.get(UniqueID(block.getPrevBlockHash())) == null )
            return false;
        
        //check if the new block height is > maxHeigth - cut off age 
        Integer newBlockHeight = blockHeightMap.get(UniqueID(block.getPrevBlockHash())) + 1;
        Integer maxHeight = blockHeightMap.get(UniqueID(getMaxHeightBlock().getHash()));

        if (newBlockHeight <= maxHeight - CUT_OFF_AGE)
            return false;
                
        //get utxopool of the blocks parent 
        UTXOPool utxoPool = utxopoolMap.get(UniqueID(block.getPrevBlockHash()));

        TxHandler handler = new TxHandler(utxoPool);
        Transaction [] handledTxs = handler.handleTxs(block.getTransactions().toArray(new Transaction[0]));

        // make sure that all trasnactions passed the check , if there is a diff , it means that one of the transacation in the block is invalid
        if (handledTxs.length != block.getTransactions().size())
            return false;
    
        //all looks good so lets add the block
        blockchainMap.put(UniqueID(block.getHash()), block);
        blockHeightMap.put(UniqueID(block.getHash()), newBlockHeight);
        
        //update utxo to use the new head
        utxopoolMap.put(UniqueID(block.getHash()), utxopoolMap.get(UniqueID(block.getPrevBlockHash())));
        //utxopoolMap.remove(UniqueID(block.getPrevBlockHash()));

        //remove transactions from transaction pool
        for (int i = 0; i <handledTxs.length ; i ++) {
        transactionPool.removeTransaction(handledTxs[i].getHash());
        }

        //remove invalid blocks
        removeInValidHeightBlocks();
        return true;
       
    }

    // remove old blocks from memory 
    public void removeInValidHeightBlocks(){

        Integer maxHeight = blockHeightMap.get(UniqueID(getMaxHeightBlock().getHash()));

        for (Integer key : blockHeightMap.keySet()) {

            if (blockHeightMap.get(key) < maxHeight - CUT_OFF_AGE){

                blockchainMap.remove(key);
                blockHeightMap.remove(key);
                utxopoolMap.remove(key);
            }
        }
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }

    /** returns a unique integer ID for the givenhash (so that debugging is easier) */
    public Integer UniqueID(byte[] hash) {
        return Arrays.toString(hash).hashCode();
    }


    public void printall (){
        System.out.println("PRINT ALL");
        System.out.println(blockchainMap);
        System.out.println(blockHeightMap);
    }
}