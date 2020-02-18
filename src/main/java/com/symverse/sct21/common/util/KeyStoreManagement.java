package com.symverse.sct21.common.util;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.WalletUtils;

import com.symverse.common.model.CommUtil;
import com.symverse.core.config.systemenv.SystemEnvFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KeyStoreManagement {


	@Autowired SystemEnvFactory systemEvn;  // System.getProperty를 가져옵니다.
	
	public Credentials getCredentials(String keyStoreName , String password ) throws Exception {

        Credentials credentials;

		try {
			log.debug("[ca_log]load credential");
			log.debug("[ca_log] credential keystorename : "+keyStoreName);
		    File keyStoreFile;
			if(systemEvn.SERVICE_MODE.contains("docker")) {  // docker container keystore save location
				keyStoreFile = new File("/webapp/keystore/"+keyStoreName);
		    }else{
		    	keyStoreFile = new ClassPathResource("keystore/"+keyStoreName).getFile();
		    }
			
			if(keyStoreFile == null) {
				log.debug("INSERT KeyStoreFile...");
			}
			credentials = WalletUtils.loadCredentials(password, keyStoreFile);

		} catch (IOException e) {
			throw new Exception(e.getMessage());
		} catch (CipherException e) {
			throw new Exception(e.getMessage());
		}

		return credentials;

	}
	
	
	public String getKeyStorePublicHash( String keyStoreName , String password ) throws Exception {
		Credentials keyStoreObj = WalletUtils.loadCredentials(password , new ClassPathResource("keystore/"+keyStoreName).getFile());
		// System.out.println("sha3 result :"+ Hash.sha3(keyStoreObj.getEcKeyPair().getPublicKey().toString(16))); // 0x5f1bf75564f1fb86aef9bbb2ffc527693b6b0e54b257812118d0bf82184dcee0
		String publicKeyToSha3 = Hash.sha3(keyStoreObj.getEcKeyPair().getPublicKey().toString(16));
		// System.out.println("public hash : "+ CommUtil.substringFromEnd( 40  , publicKeyToSha3 ) );
        return publicKeyToSha3;
	}

}



