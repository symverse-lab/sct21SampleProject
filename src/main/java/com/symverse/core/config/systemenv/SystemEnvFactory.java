package com.symverse.core.config.systemenv;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.symverse.sct21.transaction.service.Sct21SendRawTransactionService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SystemEnvFactory {

	
	// spring config server get parama
	@Value("${symverse.engine.version}") private String engineVersion;
	@Value("${symverse.chain.id}") private String chainId;
	@Value("${symverse.fork.id}") private String forkId;
	
	public String SERVICE_MODE ;
	public String KEYSTORE_PASSWORD ;
	public String KEYSTORE_FILENAME ;
	public String WORK_NODE ;
	public String CHAIN_ID ;
	public String FORK_ID ;
	public String TEMP_ID ;
	public String NODE_URL ;
	public String ENGINE_VERSION ;
				
	
	@PostConstruct
	public void systemEnvInitParameter() {
		this.SERVICE_MODE = Optional.ofNullable(System.getProperty("SERVICE_MODE")).orElse("docker").toLowerCase();
		this.KEYSTORE_PASSWORD = Optional.ofNullable(System.getProperty("KEYSTORE_PASSWORD")).orElse("INSERT_PASSWORD").toLowerCase();
		this.KEYSTORE_FILENAME = Optional.ofNullable(System.getProperty("KEYSTORE_FILENAME")).orElse("keystore.json").toLowerCase();
		this.CHAIN_ID = Optional.ofNullable(System.getProperty("CHAIN_ID")).orElse(chainId).toLowerCase();
		this.FORK_ID = Optional.ofNullable(System.getProperty("FORK_ID")).orElse("0").toLowerCase();
		this.TEMP_ID = Optional.ofNullable(System.getProperty("TEMP_ID")).orElse("0").toLowerCase();
		this.NODE_URL = Optional.ofNullable(System.getProperty("NODE_URL")).orElse("http://58.227.193.175:8545").toLowerCase();
		this.ENGINE_VERSION = Optional.ofNullable(System.getProperty("ENGINE_VERSION")).orElse(engineVersion).toLowerCase();
		this.WORK_NODE = Optional.ofNullable(System.getProperty("WORK_NODE")).orElse("0002db0859e72aad0002").toLowerCase();
		
		log.debug("");
		log.debug("");
		log.debug(" [[ ↓↓ Docker ENV Argument ↓↓ ]] ");
		log.debug("★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★ ");
		log.debug(" [ SERVICE_MODE : " + this.SERVICE_MODE + " ]");
		log.debug(" [ ENGINE_VERSION : " + this.ENGINE_VERSION + " ]");
		log.debug(" [ KEYSTORE_FILENAME : " + this.KEYSTORE_FILENAME + " ]");
		log.debug(" [ KEYSTORE_PASSWORD : " + this.KEYSTORE_PASSWORD + " ]");
		log.debug(" [ WORK_NODE_ACCOUNT : " + this.WORK_NODE + " ]");
		log.debug(" [ CHAIN_ID : " + this.CHAIN_ID + " ]");
		log.debug(" [ FORK_ID : " + this.FORK_ID + " ]");
		log.debug(" [ NODE_URL : " + this.NODE_URL + " ]");
		log.debug(" [[ ↑↑ Docker ENV Argument ↑↑ ]] ");
		log.debug("★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★ ");
		log.debug("");
		log.debug("");
	}

}
