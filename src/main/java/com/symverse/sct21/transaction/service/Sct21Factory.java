package com.symverse.sct21.transaction.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.symverse.common.model.CommUtil;
import com.symverse.core.config.systemenv.SystemEnvFactory;
import com.symverse.sct21.common.util.GetKeyStoreJson;
import com.symverse.sct21.common.util.KeyStoreManagement;
import com.symverse.sct21.common.util.SymGetAPI;
import com.symverse.sct21.transaction.domain.Sct21SendRawTransaction;
import com.symverse.sct21.transaction.domain.Sct21TempleteVO;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.parser.JSONParser;


@Slf4j
@Component
public class Sct21Factory {
	static final Logger logger = LoggerFactory.getLogger(Sct21Factory.class);
	
	// -DSERVICE_MODE=testnet 
	// -DKEYSTORE_PASSWORD=1234 
	// -DKEYSTORE_FILENAME=testnet-keystore.json 
	// -DCHAIN_ID=2 
	// -DsystemEnvFactory.NODE_URL=http://1.234.16.211:8545
	
	
	@Autowired private SystemEnvFactory systemEnvFactory; 
	@Autowired private KeyStoreManagement keyStoreManagement; 
	@Autowired private GetKeyStoreJson KeyStoreJson;
	@Autowired private Sct21SendRawTransactionService sct21SendRawTransactionService;
	
	/*
		**** 기능 List Up ****
		0	SCT21_CREATE	SCT21 계약을 생성합니다.
		1	SCT21_TRANSFER	토큰을 전송합니다.
		2	SCT21_TRANSFER_FROM	위임된 토큰을 전송합니다.
		3	SCT21_APPROVE	토큰을 위임합니다.
		4	SCT21_DECREASE_APPROVE	위임 된 토큰을 회수합니다.
		5	SCT21_MINT	토큰을 추가 발행합니다.
		6	SCT21_BURN	토큰을 태웁니다.
		7	SCT21_PAUSE	계약을 일시정지 합니다.
		8	SCT21_UNPAUSE	계약의 일시정지를 해제합니다.
		9	SCT21_TRANSFER_OWNER	계약의 Owner를 변경합니다.
		10	SCT21_LOCK_TRANSFER	일부 토큰을 잠금 상태로 전송합니다.
		11	SCT21_UNLOCK_AMOUNT	잠금 상태로 전송한 토큰을 잠금 해제합니다.
		12	SCT21_RESTORE_LOCK_AMOUNT	잠금 상태로 전송한 토큰을 회수합니다.
		13	SCT21_ADD_LOCK_AMOUNT	creator 소유의 잠금 상태의 토큰양을 추가합니다.
		14	SCT21_SUB_LOCK_AMOUNT	creator 소유의 잠금 상태의 토큰양을 회수합니다.
		15	SCT21_ACCOUNT_LOCK	특정 유저의 계약의 함수 사용(전송)을 정지시킵니다.
		16	SCT21_ACCOUNT_UNLOCK	특정 유저의 계약의 함수 사용(전송)을 정지 해제합니다.
	*/
	
	public String sendRawTransaction(  String keyStoreFileName ,  String keyStorePassWord ,
									  String toSymIdAddress , String amount) throws Exception{
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue(keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(toSymIdAddress); //old phone
		sct21SendRawTransaction.setValue(new BigInteger(amount+"000000000000000000"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();  // 그냥 sym 송금은 input값 없어도됨.
		inputParam.setSctType("-1"); 
		inputParam.setMethod("-1");  //
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add(sct21SendRawTransaction.getTo());  // 수신자
		paramsArray.add(amount+"000000000000000000"); // 토큰 양
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("0");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add( CommUtil.preHexRemove(systemEnvFactory.WORK_NODE)); // mainnet worknode - 2  0x0002b103ddaae9780002		1.234.16.207	8545
		sct21SendRawTransaction.setWorkNode(workNodesValue);
		String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);
		return getCouponTransactionHash;
	}
	
	
	// SCT21 계약을 생성합니다. (Authorization: None)
	/**
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param name - STRING Max: 10	SCT 계약 이름 입니다.
	 * @param Symbol - STRING Max: 3  SCT 계약 심볼 입니다.
	 * @param TotalSupply - 	SCT 총 발행량 입니다.
	 * @param TotalLockSupply - SCT 총 발행량에서 잠금 상태의 토큰 양입니다.
	 * @param OwnerSymId - SYMID, 10 Bytes	계약 소유자 입니다.
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_CREATE(  String keyStoreFileName ,  String keyStorePassWord ,
								String name , String symBol , String totalSupply , String totalLockSupply , String ownerSymId  ) throws Exception  {
		
		 List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		 String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName , "address") ;
		 Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		 
		 
		 
		 
		 setSymAPIConnectionParam.add(keyStroeAddress);
		 setSymAPIConnectionParam.add("pending");
		 // 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		 String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,"");
		 
		 Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();                                          
		 sct21SendRawTransaction.setFrom(keyStroeAddress);                                                                          
		 sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));                                                              
		 sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정                                                
		 sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정                                                    
		 sct21SendRawTransaction.setTo("null");                                                                                     
		 sct21SendRawTransaction.setValue(new BigInteger("2"));                                                                     
		 Sct21TempleteVO inputParam = new Sct21TempleteVO();                                                                        
		 inputParam.setSctType("21");
		 inputParam.setMethod("0");                                                                     
		 ArrayList<String> paramsArray = new ArrayList<String>();                                                                   
		 // input name , symbol , totalsupply , ownersymid                                                                          
		 paramsArray.add(name);  // 코인 이름                                                                                    
		 paramsArray.add(symBol);  // 코인이름 약어   - 무조건 3자리의 영문으로 표시합니다.                                                                                      
		 paramsArray.add(totalSupply); // 총 코인 수량                                                                                       
		 paramsArray.add(totalLockSupply); // 총 코인 수량                                                       
		 paramsArray.add( "".equals(ownerSymId) ? keyStroeAddress : ownerSymId  ); // owner symid가 없으면 본인 symid를 넣는다.
		 
		 inputParam.setParams(paramsArray);                                                                                         
		 sct21SendRawTransaction.setInput(inputParam);                                                                              
		 sct21SendRawTransaction.setType("1");                                                                                      
		 List<String> workNodesValue = new ArrayList<>();                                                                           
		 workNodesValue.add(systemEnvFactory.WORK_NODE);  // default worknode - 0002db0859e72aad0002
		 sct21SendRawTransaction.setWorkNode(workNodesValue);                                                                       
		 String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	    
		 return getCouponTransactionHash;
	}
	

	// 소유한 토큰을 전송합니다. (Authorization: None)
	/**
	 * @param keyStoreFileName  - loading keystoreFile obj
	 * @param keyStorePassWord  - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @param recipientSymId - 토큰을 전달 받을 SymId입니다.
	 * @param amount
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_TRANSFER(   String keyStoreFileName ,  String keyStorePassWord ,
								   String contractAddress , String recipientSymId , String amount) throws Exception {
		
		
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("2"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("1");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add(recipientSymId); // 전달받을 symid
		paramsArray.add(amount); // 토큰 양
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
  	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);
  	    return getCouponTransactionHash;
	}

	

	/** 위임된 토큰을 전송합니다. (Authorization: None)
	 * @param keyStoreFileName  - loading keystoreFile obj
	 * @param keyStorePassWord  - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @param From 
	 * @param Recipient
	 * @param Amount
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_TRANSFER_FROM(  String keyStoreFileName ,  String keyStorePassWord , 
			                            String contractAddress  , String recipientSymId , String amount) throws Exception {
		 List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		 String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName ,  "address");
		 System.out.println("sign keystore : "+keyStroeAddress);
		 Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		 setSymAPIConnectionParam.add(keyStroeAddress); 
		 setSymAPIConnectionParam.add("pending");
		 // 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		 String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		 Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		 sct21SendRawTransaction.setFrom( keyStroeAddress ); // 0x00021000000000030002
		 sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		 sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		 sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		 sct21SendRawTransaction.setTo(contractAddress); 
		 sct21SendRawTransaction.setValue(new BigInteger("0"));
		 Sct21TempleteVO inputParam = new Sct21TempleteVO();
		 inputParam.setSctType("21"); 
		 inputParam.setMethod("2");  
		 ArrayList<String> paramsArray = new ArrayList<String>();
		 paramsArray.add( "0x00021000000000020002" );  // 토큰의 실제 소유 SymId입니다. // 위임한 SymId를 넣으면 안됨! // ★★ 서명은 위임받은 SymId 개인키로 서명 해야합니다.★★
		 paramsArray.add( recipientSymId );  // 토큰을 전달 받을 SymId입니다. 0x00021000000000040002
		 paramsArray.add( amount ); // 토큰 양
		 inputParam.setParams(paramsArray);
		 sct21SendRawTransaction.setInput(inputParam);
		 sct21SendRawTransaction.setType("1");
		 List<String> workNodesValue = new ArrayList<>();
		 workNodesValue.add(systemEnvFactory.WORK_NODE);
		 sct21SendRawTransaction.setWorkNode(workNodesValue);
	     String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	  
	     return getCouponTransactionHash;
	}

	
	


	/** 소유한 토큰을 위임합니다. (Authorization: None) 
	 * @param keyStoreFileName - - loading keystoreFile obj 입니다.
	 * @param keyStorePassWord - 패스워드
	 * @param contractAddress -  - sct21 contract address
  	 * @param spenderSymId - 토큰을 위윔할 symId
	 * @param amount - 전송할 토큰의 양입니다.
	 * @return - transaction hash value - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_APPROVE(  String keyStoreFileName ,  String keyStorePassWord ,
			                     String contractAddress , String spenderSymId , String amount) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName , "address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue = SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("2"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("3");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add(spenderSymId);  // 토큰을 위임할 symId 입니다.
		paramsArray.add(amount); // 전송할 토큰 양의 양입니다.
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   
	    return getCouponTransactionHash;
	}
	
	
	
	
	


	/** 위임된 토큰을 회수합니다. (Authorization: None)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @param amount - 위임될 토큰의 양
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_DECREASE_APPROVE(   String keyStoreFileName ,  String keyStorePassWord
										 , String contractAddress, String spenderSymId , String amount) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("4");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add( spenderSymId );  // 토큰을 다시 회수할 SymId입니다.
		paramsArray.add( amount); // 추가 발행할 토큰 양
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   
	    return getCouponTransactionHash;
	}


	/** sct 21  토큰을 추가 발행합니다.
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @param amount - 위임될 토큰의 양
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_MINT(  String keyStoreFileName ,  String keyStorePassWord , 
							  String contractAddress , String amount ) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName , "address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("5");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add( keyStroeAddress);  // 토큰을 최초 만든  symId
		paramsArray.add( amount ); // 증가시킬 토큰 량 
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);
	    return getCouponTransactionHash;
	}
	


	/** sct 21 토큰 태움 - ( 토큰 총량 감소하기 )
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @param amount - 위임될 토큰의 양
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_BURN(  String keyStoreFileName ,  String keyStorePassWord 
							  ,String contractAddress ,String amount) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName , "address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue = SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("6");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add(keyStroeAddress);  // 토큰을 최초 만든  symid
		paramsArray.add(amount); // 감소시킬 토큰 량 
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential );
	    return getCouponTransactionHash;
	}
	
	

	/** sct 21 토큰 거래 일시 정지  (Authorization: owner, creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_PAUSE(  String keyStoreFileName ,  String keyStorePassWord , String contractAddress) throws Exception {
	    List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("7");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		inputParam.setParams(paramsArray); // input parameter 없음 ( Parameters - none )
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);
	    return getCouponTransactionHash;
	}
	
 

	/** sct 21 토큰 거래 일시 정지  해제 ( Authorization: owner, creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_UNPAUSE(  String keyStoreFileName ,  String keyStorePassWord , String contractAddress) throws Exception {
	    List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21");
		inputParam.setMethod("8");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   	
	    return getCouponTransactionHash;
	}

	
	/** 계약의 소유자를 이전합니다. (Authorization: owner, creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_TRANSFER_OWNER( String keyStoreFileName ,  String keyStorePassWord , String contractAddress , String newOwnerSymId ) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("9");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add( newOwnerSymId );  // 토큰을 최초 만든  symid
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   	
	    return getCouponTransactionHash;
	}
	

	/** 일부 토큰을 잠금 상태로 전송합니다. - (Authorization: owner, creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_LOCK_TRANSFER(  String keyStoreFileName ,  String keyStorePassWord ,
									   String contractAddress , String recipientSymId , String amount , String lockAmount ) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("10");  
		ArrayList<String> paramsArray = new ArrayList<String>(); 
		// ※ 주의사항 :  SCT21_LOCK_TRANSFER 함수는  전송할 토큰의 양 , 잠근 전송할 토큰 양이 둘다 0 일 수는 없습니다. 또한  이 기능으로 토큰을 보낼수 있지만 SCT21_TRANSFER 함수를 쓸대 보다 1000 gas 비를 더 소모합니다. 따라서 LOCK 송금을 해야할때만 사용하시기 바랍니다.
		paramsArray.add( recipientSymId );  // 토큰을 받을 SymId입니다. 
		paramsArray.add( amount );  // 전송할 토큰 양입니다.
		paramsArray.add( lockAmount );  // 잠금 전송할 토큰 양입니다.

		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   	
	    return getCouponTransactionHash;
	}
	

	/** 잠금 상태로 전송한 토큰을 잠금 해제합니다.
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_UNLOCK_AMOUNT( String keyStoreFileName ,  String keyStorePassWord , String contractAddress ,
									  String recipientSymId , String amount) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		
		//input 값 셋팅
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("11");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add( recipientSymId ); // 잠금 상태의 토큰을 갖고 있는 SymId입니다.
		paramsArray.add( amount );  // 회수할 잠금 상태의 토큰 양입니다.
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   	
	    return getCouponTransactionHash;
	}
	

	/** 잠금 상태로 전송한 토큰을 회수합니다. (Authorization: owner, creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @param recipientSymId
	 * @param amount
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_RESTORE_LOCK_AMOUNT( String keyStoreFileName ,  String keyStorePassWord , String contractAddress ,
											String recipientSymId , String amount) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		
		//input 값 셋팅
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("12");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add( recipientSymId ); // 잠금 상태의 토큰을 갖고 있는 SymId입니다.
		paramsArray.add( amount );  // 회수할 잠금 상태의 토큰 양입니다.
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   	
	    return getCouponTransactionHash;
	}
	

	/**  creator 소유의 잠금 상태의 토큰양을 추가합니다. (Authorization: creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @param amount - TotalLockSupply - add token amount
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_ADD_LOCK_AMOUNT( String keyStoreFileName ,  String keyStorePassWord , String contractAddress , String amount) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("13");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add( amount ); // totalLockSupply에 추가 할 토큰 양입니다.
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   	
	    return getCouponTransactionHash;
	}
	

	 /** creator 소유의 잠금 상태의 토큰양을 회수 합니다. (Authorization: creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @param amount - TotalLockSupply - add token amount
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_SUB_LOCK_AMOUNT( String keyStoreFileName ,  String keyStorePassWord , String contractAddress , String amount) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("14");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add( amount ); // creator의 소유로 회수할 토큰 양입니다.
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   	
	    return getCouponTransactionHash;
	}
	
	/** 특정 유저의 계약의 함수 사용(토큰 전송)을 잠금 상태로 변경합니다. (Authorization: creator, owner) - 잠금 상태의 유저는 TRANSFER, TRANSFER_FROM, APPROVE, DECREASE_APPROVE의 함수 실행을 할 수 없습니다.
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @param symId - SymId to change to locked state.
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_ACCOUNT_LOCK( String keyStoreFileName ,  String keyStorePassWord , String contractAddress , String symId) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("15");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add( symId ); //	잠금 상태로 변경할 SymId입니다.
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   	
	    return getCouponTransactionHash;
	}
	
	/** 특정 유저의 계약의 함수 사용(토큰 전송)의 잠금 해제합니다. (Authorization: creator, owner)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @param symId - SymId to unlock.
	 * @return - transaction hash value
	 * @throws Exception
	 */
	public String SCT21_ACCOUNT_UNLOCK( String keyStoreFileName ,  String keyStorePassWord , String contractAddress  , String symId ) throws Exception {
		List<String> setSymAPIConnectionParam  = new ArrayList<String>();
		String keyStroeAddress = KeyStoreJson.getKeyStoreValue( keyStoreFileName,"address");
		Credentials credential = keyStoreManagement.getCredentials( keyStoreFileName , "".equals(keyStorePassWord) ?  systemEnvFactory.KEYSTORE_PASSWORD : keyStorePassWord );  // parameter keystorepassword  인자값 없으면 systemkeystorepassword 물고온다.
		setSymAPIConnectionParam.add(keyStroeAddress);
		setSymAPIConnectionParam.add("pending");
		// 블록체인 엔진에서 현재의 블록 TX nonce값을 가져 옵니다.
		String nonceValue =SymGetAPI.getSymAPIConnection("GET", systemEnvFactory.NODE_URL ,"sym_getTransactionCount",setSymAPIConnectionParam ,""); 
		Sct21SendRawTransaction sct21SendRawTransaction	= new Sct21SendRawTransaction();
		sct21SendRawTransaction.setFrom(keyStroeAddress);
		sct21SendRawTransaction.setNonce(new BigInteger(nonceValue));
		sct21SendRawTransaction.setGasPrice(new BigInteger("25000000000")); // 값 고정
		sct21SendRawTransaction.setGasLimit(new BigInteger("2000000")); // 값 고정
		sct21SendRawTransaction.setTo(contractAddress); 
		sct21SendRawTransaction.setValue(new BigInteger("0"));
		Sct21TempleteVO inputParam = new Sct21TempleteVO();
		inputParam.setSctType("21"); 
		inputParam.setMethod("16");  
		ArrayList<String> paramsArray = new ArrayList<String>();
		paramsArray.add( symId ); // 	잠금 상태를 해제할 SymId입니다.
		inputParam.setParams(paramsArray);
		sct21SendRawTransaction.setInput(inputParam);
		sct21SendRawTransaction.setType("1");
		List<String> workNodesValue = new ArrayList<>();
		workNodesValue.add(systemEnvFactory.WORK_NODE);
		sct21SendRawTransaction.setWorkNode(workNodesValue);
	    String getCouponTransactionHash = sct21SendRawTransactionService.sct21SendRawTransaction(sct21SendRawTransaction , credential);	   	
	    return getCouponTransactionHash;
	}
	
	
	
	

	

	




}
