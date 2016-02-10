/*
 * Copyright 2016 Andrew W. Buchanan (buchanan@difference.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.difference.historybook.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * Utility class to manage self-signed TLS certificates
 *
 */
public class CertManager {
	private static final Logger LOG = LoggerFactory.getLogger(CertManager.class);
	
	private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
	private static final int KEY_LENGTH = 4096;
	private static final String SIGNATURE_ALGORITHM = "SHA512withRSA"; 
	
	/**
	 * Create a self-signed certificate and store in a keystore (if it doesn't already exist)
	 * 
	 * @param keystore path to the keystore to save to
	 * @param password password to use to encrypt keystore
	 * @param alias name to give the certificate in the keystore
	 * @param x500String X500 name for the certificate. (e.g. "CN=localhost,OU=issuer)
	 * @param duration length of time a newly created certificate should remain valid (in seconds)
	 * 
	 * @throws @RuntimeException if an error occurs in creating the certificate
	 */
	public static void initialize(Path keystore, String password, String alias, String commonName, String organization, long duration) {
		if (keystore.toFile().exists()) {
			LOG.info("Keystore {} found.", keystore);
			return;
		}
		
		try {
			Security.addProvider(new BouncyCastleProvider());
			
			// generate a key pair
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", PROVIDER_NAME);
			keyPairGenerator.initialize(KEY_LENGTH, new SecureRandom());
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			PublicKey pubKey = keyPair.getPublic();
			PrivateKey privateKey = keyPair.getPrivate();
	
			// build name
			X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
		    nameBuilder.addRDN(BCStyle.CN, commonName);
		    nameBuilder.addRDN(BCStyle.O, organization);
		    nameBuilder.addRDN(BCStyle.OU, organization);
			X500Name issuerName = nameBuilder.build();
			X500Name subjectName = issuerName;
			
			// build serial
			BigInteger serial = BigInteger.valueOf(new Random().nextInt());
			
			// build a certificate generator
			X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
					issuerName, 
					serial, 
					new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000), // yesterday
					new Date(System.currentTimeMillis() + duration * 1000), 
					subjectName, 
					pubKey);
	
			KeyUsage usage = new KeyUsage(
					KeyUsage.digitalSignature | KeyUsage.keyEncipherment);
			certBuilder.addExtension(Extension.keyUsage, true, usage);
		    
			ASN1EncodableVector purposes = new ASN1EncodableVector();
		    purposes.add(KeyPurposeId.id_kp_serverAuth);
		    certBuilder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));
		    
			X509Certificate[] chain = new X509Certificate[1];
		    chain[0] = signCertificate(certBuilder, keyPair.getPrivate());
		    
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null,null);
			 
			keyStore.setKeyEntry(alias, privateKey, password.toCharArray(), chain);
			keyStore.store(new FileOutputStream(keystore.toFile()), password.toCharArray());
			Files.setPosixFilePermissions(keystore, ImmutableSet.of(PosixFilePermission.OWNER_READ));
			LOG.info("Created keystore at {}.", keystore);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | CertificateException
				| KeyStoreException | IOException | OperatorCreationException e) {
			LOG.error(e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}
	
	private static X509Certificate signCertificate( 
            X509v3CertificateBuilder certificateBuilder, 
            PrivateKey signedWithPrivateKey) throws OperatorCreationException, 
            CertificateException { 
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM) 
                .setProvider(PROVIDER_NAME).build(signedWithPrivateKey); 
        X509Certificate cert = new JcaX509CertificateConverter().setProvider( 
                PROVIDER_NAME).getCertificate(certificateBuilder.build(signer)); 
        return cert; 
    } 
}
