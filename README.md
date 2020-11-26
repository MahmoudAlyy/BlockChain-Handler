# BlockChain Handler
(An adapted version of assignments by the Princeton Bitcoin book authors)

Implemented a simple transaction handler (TxHandler.java) where it receives a set of input transactions and is required to return a set of mutually valid transactions while noting the following:  
-Received set of transactions could be unordered.  
-A transaction can spend the output of another transaction in the same block.  
-Among the transactions that the node receives, some transactions may try to spend the same output (double spend).  
-The constructed block does not need to be the largest block possible, however the main requirement is that the block should be a mutually valid transaction set of maximal size.
 
Implemented a node that maintains a blockchain (BlockChain.java). The node can be thought of as part of a blockchain-based consensus protocol, where a node could receive transactions and blocks from any other node, and updates its view accordingly. I implemented this while noting:   
-Only keep around the most recent blocks, since the entire blockchain could be huge in size.  
-Blocks form a tree rather than a list, since there could be (multiple) forks.
-Maintain a UTXO pool corresponding to every block on top of which a new block might be created.
