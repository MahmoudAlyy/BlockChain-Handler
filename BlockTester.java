import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

public class BlockTester {

    public static void main(String[] args)
            throws NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        KeyPair first_User_keypair = generateNewKeyPair();
        KeyPair second_User_keypair = generateNewKeyPair();
        KeyPair third_User_keypair = generateNewKeyPair();
        KeyPair fourth_User_keypair = generateNewKeyPair();
        KeyPair fifth_User_keypair = generateNewKeyPair();

        // Create a new Genesis give first user 25.0 coins

        Block genesis = new Block(null, first_User_keypair.getPublic());
        genesis.finalize();
        BlockChain block_chain = new BlockChain(genesis);
        BlockHandler blockHandler = new BlockHandler(block_chain);

        // user 1 Create transaction_0 inputs -> coin base
        // outputs -> 10.0 coins user 3
        // outputs -> 15.0 coins user 2

        Transaction transaction_0 = new Transaction();
        transaction_0.addInput(genesis.getCoinbase().getHash(), 0);
        transaction_0.addOutput(15.0, second_User_keypair.getPublic());
        transaction_0.addOutput(10.0, third_User_keypair.getPublic());

        byte[] sig = sign(first_User_keypair.getPrivate(), transaction_0.getRawDataToSign(0));
        transaction_0.addSignature(sig, 0);
        transaction_0.finalize();

        // user 2 Create transaction_1 inputs -> transaction_0 output 0
        // outputs -> 15.0 coins user 3

        Transaction transaction_1 = new Transaction();
        transaction_1.addInput(transaction_0.getHash(), 0);
        transaction_1.addOutput(15.0, third_User_keypair.getPublic());

        byte[] sig_1 = sign(second_User_keypair.getPrivate(), transaction_1.getRawDataToSign(0));
        transaction_1.addSignature(sig_1, 0);
        transaction_1.finalize();

        // user 3 Create transaction_2 inputs -> transaction_0 output 1
        // inputs -> transaction_1 output 0
        // outputs -> 25.0 coins user 5

        Transaction transaction_2 = new Transaction();
        transaction_2.addInput(transaction_0.getHash(), 1);
        transaction_2.addInput(transaction_1.getHash(), 0);
        transaction_2.addOutput(25.0, fifth_User_keypair.getPublic());

        byte[] sig_2 = sign(third_User_keypair.getPrivate(), transaction_2.getRawDataToSign(0));
        transaction_2.addSignature(sig_2, 0);
        byte[] sig_2_2 = sign(third_User_keypair.getPrivate(), transaction_2.getRawDataToSign(1));
        transaction_2.addSignature(sig_2_2, 1);
        transaction_2.finalize();

        // Adding transactions to the transaction pool
        // They are all valid so they should be accepted

        block_chain.addTransaction(transaction_0);
        block_chain.addTransaction(transaction_1);
        block_chain.addTransaction(transaction_2);

        // user 4 mines the block

        blockHandler.createBlock(fourth_User_keypair.getPublic());

        //System.out.println(block_chain.get_Memory().size());
        block_chain.printall();
        System.out.println(block_chain.getMaxHeightUTXOPool().getAllUTXO().get(0));

    }

    public static KeyPair generateNewKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.genKeyPair();
    }

    public static byte[] sign(PrivateKey privKey, byte[] message)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privKey);
        signature.update(message);
        return signature.sign();
    }

}