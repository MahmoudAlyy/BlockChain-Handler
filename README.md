# BlockChain
(An adapted version of assignments by the Princeton Bitcoin book authors)

Implemented a simple transaction handler where it receives a set of input transactions and is required to return a set of mutually valid transactions. 
while noting the following:


Received set of transactions could be unordered.
A transaction can spend the output of another transaction in the same block.
Among the transactions that the node receives, some transactions may try to spend the same output (double spend).
The constructed block does not need to be the largest block possible, however the main requirement is that the block should be a mutually valid transaction set of maximal size.
 
Among the transactions that the node receives, some transactions may try to spend the same output, i.e. double spend an output. This should not be allowed; only one transaction can spend an output. This means that transactions cannot be validated in isolation. 
The constructed block does not need to be the largest block possible, however the main requirement is that the block should be a mutually valid transaction set of maximal size, i.e. a set that cannot be enlarged by any other transaction from the received set.
to implement TxHandler.java according to the specifications in its java doc. Make sure to take the block constraints (Page 1) into account when implementing handleTxs()

will implement a node that maintains a blockchain. The node can be thought of as part of a blockchain-based consensus protocol, where a node could receive transactions and blocks from any other node, and updates its view accordingly.   Your task in this part is to implement the BlockChain class. This class is responsible for maintaining a blockchain. Since the entire blockchain could be huge in size, you should only keep around the most recent blocks.   Since there can be (multiple) forks, blocks form a tree rather than a list. Your design should take this into account. You have to maintain a UTXO pool corresponding to every block on top of which a new block might be created.
