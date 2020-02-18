package com.symverse.sct21.transaction.service;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import com.symverse.common.model.CommUtil;
import com.symverse.core.config.systemenv.SystemEnvFactory;
import com.symverse.exception.ServerErrorException;
import com.symverse.sct21.common.util.KeyStoreManagement;
import com.symverse.sct21.gsym.core.Gsym;
import com.symverse.sct21.gsym.core.JsonRpc2_0Gsym;
import com.symverse.sct21.gsym.domain.SendSct21;
import com.symverse.sct21.transaction.domain.Sct21SendRawTransaction;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service("sct21SendRawTransactionService")
public class Sct21SendRawTransactionService {
	static final Logger logger = LoggerFactory.getLogger(Sct21SendRawTransactionService.class);
	
    private JsonRpc2_0Gsym jsonRpc2_0Gsym;
    
	@Autowired
	KeyStoreManagement keyStoreManagement;
	
	@Autowired SystemEnvFactory systemEvn;  // System.getProperty를 가져옵니다.

	@Autowired
    public void initJsonRpc2_0Sym(Environment env){
		this.jsonRpc2_0Gsym = Gsym.build(new HttpService(systemEvn.NODE_URL));
    }
    
    /**
	 * citizen 등록
	 * @param citizenRawTransaction
	 * @return
     * @throws Exception 
	 */
	public String sct21SendRawTransaction (Sct21SendRawTransaction sct21SendRawTransaction , Credentials credentialParam ) throws Exception {
		log.debug("");
		log.debug("Sct21SendRawTransaction SctType  : ["+sct21SendRawTransaction.getInput().getSctType()+"]");
		log.debug("Sct21SendRawTransaction methodType  :  ["+sct21SendRawTransaction.getInput().getMethod()+"]");
		log.debug("");
		SendSct21 sendsct21 = new SendSct21();
		//TODO sign값 value로 설정 넣어주기

		byte[] signedMessage = null ;
		if("1.0.14".equals(systemEvn.ENGINE_VERSION)) {
			signedMessage = Sct21RawTransactionEncoderEngineVersion1_0_14.signMessage(
					  sct21SendRawTransaction
					, (byte) Integer.parseInt(systemEvn.CHAIN_ID)
					,  0 == Integer.parseInt(systemEvn.FORK_ID) ? new byte[] {} : CommUtil.bigIntToByteArray( Integer.parseInt(systemEvn.FORK_ID) )
					,  0 == Integer.parseInt(systemEvn.TEMP_ID) ? new byte[] {} : CommUtil.bigIntToByteArray( Integer.parseInt(systemEvn.TEMP_ID) )
					, credentialParam  );
		}
		
		
		
		String hexValue = Numeric.toHexString(signedMessage);
		logger.debug("Transaction HexValue: " + hexValue);
		
		try {
			// 블록체인 엔진으로 sym_sendrawtransaction 을 보냅니다.
			sendsct21 = jsonRpc2_0Gsym.sct21SendRawTransaction(hexValue).sendAsync().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new Exception(e.getMessage());
		}
		
		if(sendsct21.hasError()) {
			throw new ServerErrorException(sendsct21.getError().getMessage());
		}
		
		log.debug("Sct21SendRawTransaction hash: " + sendsct21.getHash());
		return sendsct21.getHash();
	}


	

	

	




}
