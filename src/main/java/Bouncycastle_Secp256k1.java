import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;


import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.util.MessageDigestUtils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;



/***********************************************************************************************
 * 												*
 *		Ellipische Kurven-Operationen Secp256k1 mit Bouncycastle			*
 *		-	Erstellung einer ECDSA Signatur						*
 *		-	Verifizierung der ECDSA Signatur					*
 *		-	EC-Multiplikation und Addition						*
 *												*
 ***********************************************************************************************/


public class Bouncycastle_Secp256k1 {


    /**
     * Es wird eine Signatur erstellt bestehend aus den Teilen "r" und "s".
     * Übergeben wird der 32byte lange Hash, der signiert werden soll,
     * - der Priv.Key 32Byte,
     * - die Zufallszahl "k" als ByteArray.
     * Rückgabe ist ein BigInteger-Array bestehend aus 2 Elementen: [0] = r   und    [1] = s.
     * Achtung: Die Zufallszahl "k" muss aus einer kryptographisch starken Entropie stammen!
     * Falls "k" vorhersebar ist, kann der Priv.Key leicht aufgedeckt werden!!!
     */
    public static BigInteger[] sig(byte[] hash, byte[] priv, byte[] k) {
        k = Secp256k1.to_fixLength(k, 32);
        X9ECParameters p = SECNamedCurves.getByName("secp256k1");
        ECDomainParameters params = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());
        ECPrivateKeyParameters priKey = new ECPrivateKeyParameters(new BigInteger(1, priv), params);
        SecureRandom rand = new FixedSecureRandom(k);
        ECDSASigner dsa = new ECDSASigner();
        dsa.init(true, new ParametersWithRandom(priKey, rand));
        BigInteger[] sig = dsa.generateSignature(hash);
        return sig;
    }



    public static boolean verify(byte[] hash, BigInteger[] sig, String pub) {
        X9ECParameters p = SECNamedCurves.getByName("secp256k1");
        ECDomainParameters params = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());
        ECDSASigner dsa = new ECDSASigner();
//        System.out.println(pub[0].toString(16));
//        System.out.println(pub[1].toString(16));
//        System.out.println("04" + pub[0].toString(16) + pub[1].toString(16));
        System.out.println(pub);
        ECPublicKeyParameters pubKey = new ECPublicKeyParameters(params.getCurve().decodePoint(Hex.decode(pub)), params);
        dsa.init(false, pubKey);
        if (dsa.verifySignature(hash, sig[0], sig[1])) return true;
        else return false;
    }


    /**
     * Multipliziert den Generator-Punkt mit dem factor.
     */
    public static BigInteger[] mul_G(BigInteger factor) {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECPoint Q = spec.getG().multiply(factor).normalize();
        BigInteger[] erg = new BigInteger[2];
        erg[0] = Q.getAffineXCoord().toBigInteger();
        erg[1] = Q.getAffineYCoord().toBigInteger();
        return erg;
    }


    /**
     * Multipliziert einen Punkt "point" auf der elliptischen Kurve mit dem factor.
     */
    public static BigInteger[] mul_Point(BigInteger[] point, BigInteger factor) {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECCurve curve = spec.getCurve();
        ECPoint P = curve.createPoint(point[0], point[1]);
        ECPoint Q = P.multiply(factor).normalize();
        BigInteger[] erg = new BigInteger[2];
        erg[0] = Q.getAffineXCoord().toBigInteger();
        erg[1] = Q.getAffineYCoord().toBigInteger();
        return erg;
    }


    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    static final ECDomainParameters CURVE =
            new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());


    /**
     * Addiert zwei Punkte auf der elliptischen Kurve.
     */
    public static BigInteger[] add(BigInteger[] a, BigInteger[] b) {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECCurve curve = spec.getCurve();
        ECPoint P = curve.createPoint(a[0], a[1]);
        ECPoint Q = curve.createPoint(b[0], b[1]);
        ECPoint R = P.add(Q).normalize();
        BigInteger[] erg = new BigInteger[2];
        erg[0] = R.getAffineXCoord().toBigInteger();
        erg[1] = R.getAffineYCoord().toBigInteger();
        return erg;
    }


}