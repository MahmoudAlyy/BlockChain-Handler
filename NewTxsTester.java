import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

public class NewTxsTester {
    

    public static void main(String[] args)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {


        KeyPair keyPairC = generateNewKeyPair();
        KeyPair keyPairD = generateNewKeyPair();
        KeyPair keyPairE = generateNewKeyPair();
        KeyPair keyPairF = generateNewKeyPair();
        KeyPair keyPairM = generateNewKeyPair();

        byte[] sig; 

        // coinbase transactions
        Transaction txA = new Transaction(20.0, keyPairC.getPublic());
        Transaction txB = new Transaction(20.0, keyPairD.getPublic());

        //utxo
        UTXOPool pool = new UTXOPool();
   
        // transation to put in the utxo
        Transaction txOld = new Transaction();

        txOld.addInput(null, 0);
        txOld.addOutput(10.0, keyPairF.getPublic());
        txOld.finalize();
        pool.addUTXO(new UTXO(txOld.getHash(), 0), txOld.getOutput(0));
        ///

        TxHandler txHandler = new TxHandler(pool);

        //
        Transaction txC = new Transaction();

        txC.addInput(txA.getHash(), 0);
        //txC.addInput(hexStringToByteArray("AAAA"), 0);

        txC.addOutput(10.0, keyPairE.getPublic());
        txC.addOutput(10.0, keyPairF.getPublic());

        sig = sign(keyPairC.getPrivate(), txC.getRawDataToSign(0));

        txC.addSignature(sig, 0);
        txC.finalize();

        //

        Transaction txD = new Transaction();

        txD.addInput(txB.getHash(), 0);

        txD.addOutput(10.0, keyPairE.getPublic());
        txD.addOutput(10.0, keyPairF.getPublic());

        sig = sign(keyPairD.getPrivate(), txD.getRawDataToSign(0));
        txD.addSignature(sig, 0);
 
        txD.finalize();
        
        //

        Transaction txE = new Transaction();

        txE.addInput(txC.getHash(), 0);

        txE.addInput(txD.getHash(), 0);

        txE.addOutput(10.0, keyPairF.getPublic());

        sig = sign(keyPairE.getPrivate(), txE.getRawDataToSign(0));
        txE.addSignature(sig, 0);

        sig = sign(keyPairE.getPrivate(), txE.getRawDataToSign(1));
        txE.addSignature(sig, 1);

        txE.finalize();

        //

        Transaction txF = new Transaction();

        txF.addInput(txE.getHash(), 0);
        txF.addInput(txC.getHash(), 1);
        txF.addInput(txD.getHash(), 1);
        txF.addInput(txOld.getHash(), 0);
        //txF.addInput(hexStringToByteArray("AAAA"), 0);

        // double spend
        //txF.addInput(txC.getHash(), 0);

        txF.addOutput(30.0, keyPairM.getPublic());

        sig = sign(keyPairF.getPrivate(), txF.getRawDataToSign(0));
        txF.addSignature(sig, 0);

        sig = sign(keyPairF.getPrivate(), txF.getRawDataToSign(1));
        txF.addSignature(sig, 1);

        sig = sign(keyPairF.getPrivate(), txF.getRawDataToSign(2));
        txF.addSignature(sig, 2);

        sig = sign(keyPairF.getPrivate(), txF.getRawDataToSign(3));
        txF.addSignature(sig, 3);

      
        txF.finalize();

        //ByteArrayWrapper bwa =new ByteArrayWrapper(txA.getHash());
        //System.out.println("testA "+ bwa.hashCode());
        
        System.out.println("txA " + UniqueID(txA.getHash()));
        System.out.println("txB " + UniqueID(txB.getHash()));
        System.out.println("txC " + UniqueID(txC.getHash()));
        System.out.println("txD " + UniqueID(txD.getHash()));
        System.out.println("txE " + UniqueID(txE.getHash()));
        System.out.println("txF " + UniqueID(txF.getHash()));
        System.out.println("txOld " + UniqueID(txOld.getHash()));
        System.out.println("");

        Transaction[] possibleTxs = new Transaction[6];
        possibleTxs[0] = txD;
        possibleTxs[1] = txC;
        possibleTxs[2] = txB;
        possibleTxs[3] = txA;
        possibleTxs[4] = txE;
        possibleTxs[5] = txF;

      

        txHandler.handleTxs(possibleTxs);

        System.out.println(UniqueID(txHandler.getUTXOPool().getAllUTXO().get(0).getTxHash()));
    


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
