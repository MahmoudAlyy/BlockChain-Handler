import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

public class HandleTxsTester {

    /*
     * This code shows how to test a very simple case on your code. It's recommended
     * that you think of more involved scenarios. Many things are simplified for the
     * purpose of testing.
     * 
     * We have two transactions tx1, and tx2. Assume tx1 is valid, and its output is
     * already in the UTXO pool. tx2 tries to spend that output. If tx2 provides a
     * valid signature, and does not try to spend more than the output value, while
     * specifying everything correctly it should be considered valid.
     */

    public static void main(String[] args)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {



        Transaction txA = new Transaction();

        txA.addInput(null, 0);
        KeyPair keyPairC = generateNewKeyPair();
        KeyPair keyPairD = generateNewKeyPair();

        txA.addOutput(20.0, keyPairC.getPublic());
        txA.addOutput(20.0, keyPairD.getPublic());

        txA.finalize();

        //

        Transaction txB = new Transaction();

        txB.addInput(null, 0);

        txB.addOutput(20.0, keyPairC.getPublic());

        txB.finalize();
        
        //

        Transaction txD = new Transaction();

        txD.addInput(txA.getHash(), 1);

        KeyPair keyPairF = generateNewKeyPair();
        
        txD.addOutput(10.0, keyPairF.getPublic());

        byte[] sig = sign(keyPairD.getPrivate(), txD.getRawDataToSign(0));
        txD.addSignature(sig, 0);
        txD.finalize();

        
        //

        Transaction txC = new Transaction();

        txC.addInput(txA.getHash(), 0);

        txC.addOutput(10.0, keyPairF.getPublic());

        sig = sign(keyPairC.getPrivate(), txC.getRawDataToSign(0));
        txC.addSignature(sig, 0);

        //2nd input

        txC.addInput(txB.getHash(), 0);

        txC.addOutput(10.0, keyPairF.getPublic());

        sig = sign(keyPairC.getPrivate(), txC.getRawDataToSign(1));
        txC.addSignature(sig, 1);

        txC.finalize();

        //
        byte[] randombytes = hexStringToByteArray("aaaa");

        Transaction txE = new Transaction();
            
        txE.addInput(randombytes, 1);

        txE.addOutput(10.0, keyPairF.getPublic());

        txE.finalize();

        //

        System.out.println("txA " + UniqueID(txA.getHash()));
        System.out.println("txB " + UniqueID(txB.getHash()));
        System.out.println("txC " + UniqueID(txC.getHash()));
        System.out.println("txD " + UniqueID(txD.getHash()));
        System.out.println("txE " + UniqueID(txE.getHash()));

        Transaction[] possibleTxs = new Transaction[5];
        possibleTxs[0] = txD;
        possibleTxs[1] = txC;
        possibleTxs[2] = txB;
        possibleTxs[3] = txA;
        possibleTxs[4] = txE;

        UTXOPool pool = new UTXOPool();
        TxHandler txHandler = new TxHandler(pool);

        txHandler.handleTxs(possibleTxs);
        

        //test2 testHandler = new test2();

        //testHandler.callme(possibleTxs);


        /*
         * The previous code only checks the validity. To update the pool, your
         * implementation of handleTxs() will be called.
         */

    }

    public static KeyPair generateNewKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.genKeyPair();
    }

    public static Integer UniqueID(byte[] hash) {
        return Arrays.toString(hash).hashCode();
    }

    public static byte[] sign(PrivateKey privKey, byte[] message)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privKey);
        signature.update(message);
        return signature.sign();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}