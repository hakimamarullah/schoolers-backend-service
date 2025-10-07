package com.schoolers.utils;

import com.schoolers.exceptions.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.springframework.aot.hint.annotation.RegisterReflection;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
@RegisterReflection
public class SignatureUtils {

    public boolean verifySignature(String data, String signatureBase64, String publicKeyBase64,
                                   String algorithm) {
        try {
            // Decode public key
            byte[] publicKeyBytes = Base64.getDecoder().decode(cleanUpBase64String(publicKeyBase64));
            PublicKey publicKey = generatePublicKey(publicKeyBytes, algorithm, true);
            if (Objects.isNull(publicKey)) {
                publicKey = generatePublicKey(publicKeyBytes, algorithm, false);
            }

            if (Objects.isNull(publicKey)) {
                return false;
            }

            // Initialize signature verifier
            String signatureAlgorithm = algorithm.equals("RSA") ? "SHA256withRSA" : "SHA256withECDSA";
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            // Decode and verify signature
            byte[] signatureBytes = Base64.getDecoder().decode(cleanUpBase64String(signatureBase64));
            return signature.verify(signatureBytes);

        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    public void validatePublicKey(String pemKey, String algorithm) {
        String cleanKey = cleanUpBase64String(pemKey);
        byte[] publicKeyBytes = Base64.getDecoder().decode(cleanKey);

        var pubKey = generatePublicKey(publicKeyBytes, algorithm, false);
        if (Objects.isNull(pubKey)) {
            pubKey = generatePublicKey(publicKeyBytes, algorithm, true);
        }

        if (Objects.isNull(pubKey)) {
            throw new SignatureException("Invalid public key format");
        }
    }

    /**
     * Attempts to validate the key. If `convert` is true, wraps PKCS#1 → X.509.
     */
    private PublicKey generatePublicKey(byte[] keyBytes, String algorithm, boolean convert) {
        try {
            byte[] bytesToUse = convert ? convertPkcs1ToX509(keyBytes) : keyBytes;
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytesToUse);
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            log.info("Public key validated as {}", convert ? "PKCS#1 → X.509" : "X.509");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            return null;
        }
    }


    private String cleanUpBase64String(String pem) {
        return pem
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "")
                .trim();
    }

    /**
     * Wraps PKCS#1 RSA key bytes into X.509 (SubjectPublicKeyInfo) structure
     */
    private byte[] convertPkcs1ToX509(byte[] pkcs1Bytes) {
        try {
            // ASN.1 header for rsaEncryption OID: SEQUENCE { AlgorithmIdentifier {rsaEncryption, NULL}, BIT STRING (key) }
            // Using BouncyCastle ASN.1 to wrap it
            ASN1EncodableVector vec = new ASN1EncodableVector();
            ASN1EncodableVector algId = new ASN1EncodableVector();
            algId.add(new ASN1ObjectIdentifier("1.2.840.113549.1.1.1")); // rsaEncryption
            algId.add(DERNull.INSTANCE);

            vec.add(new DERSequence(algId));
            vec.add(new DERBitString(pkcs1Bytes));

            return new DERSequence(vec).getEncoded("DER");
        } catch (Exception e) {
            throw new SignatureException("Failed to convert PKCS#1 to X.509");
        }
    }

    public String generateSecureToken() {
        return UUID.randomUUID() + "-" + System.currentTimeMillis();
    }

    public String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new SignatureException("Hash generation failed");
        }
    }
}
