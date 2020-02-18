package com.symverse.sct21.controller;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.WalletUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.symverse.ca.model.SymIdSendPostVO;
import com.symverse.common.http.OkHttpUtil;
import com.symverse.common.model.CommUtil;
import com.symverse.core.config.jasypt.JasyptUtil;
import com.symverse.core.config.systemenv.SystemEnvFactory;
import com.symverse.sct21.common.util.GetKeyStoreJson;
import com.symverse.sct21.common.util.SymGetAPI;
import com.symverse.sct21.transaction.service.Sct21Factory;
import com.symverse.server.keystore.model.SymverseServerKeyStoreVO;
import com.symverse.wallet.keystore.model.Ca;
import com.symverse.wallet.keystore.model.Ethereum;
import com.symverse.wallet.keystore.model.SymId;
import com.symverse.wallet.keystore.model.SymverseWalletKeyStoreVO;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;




@RestController
@RequestMapping("/sct21")
@RefreshScope
@Slf4j
public class Sct21Controller {
	
	
	
	@Autowired SystemEnvFactory systemEnv;  // System.POSTProperty를 가져옵니다.
	@Autowired GetKeyStoreJson getKeyStoreJson;  // keystore의 객체를 가져 옵니다.
	@Autowired private Sct21Factory sct20Factory;
	
	
	
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
	
	/**
	 * @param symId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/createWallet", method=RequestMethod.GET )
	@ResponseBody
	public Object test( ) throws Exception {
		 //@RequestParam String userNm , @RequestParam String email , @RequestParam String mobileNum , @RequestParam(defaultValue="CELL_PHONE") String verificationType
		 // testparam
		 String userNm = "양재현";		
		 String email = "kkk@gmail.com";		
		 String mobileNum = "01020813230";		
		 String verificationType = "CELL_PHONE";		
		 File walletFile ;
		 if(systemEnv.SERVICE_MODE.contains("docker")) { // docker container keystore save location
			 walletFile = new File("/webapp/walletFile/");
	     }else{
	         walletFile =  new File("C:/attachments/");
	     }
		 String result = WalletUtils.generateNewWalletFile("0000",  walletFile); // etherum 지갑만들때는 etherum web3j 써야함...
		 // String keyStoreName = "jaehyunOld-keystore.json";
		 File file =  new File("C:/attachments/"+result);
		 Credentials keyStoreObj = WalletUtils.loadCredentials("0000", file);
		 // Credentials keyStoreObj = WalletUtils.loadCredentials("0000", new ClassPathResource("keystore/jaehyunOld-keystore.json").getFile());
		 // Credentials keyStoreObj = WalletUtils.loadCredentials("0000", new ClassPathResource("keystore/"+keyStoreName).getFile());
		 System.out.println("sha3 result :"+ Hash.sha3(keyStoreObj.getEcKeyPair().getPublicKey().toString(16))); // 0x5f1bf75564f1fb86aef9bbb2ffc527693b6b0e54b257812118d0bf82184dcee0
		 String publicKeyToSha3 = Hash.sha3(keyStoreObj.getEcKeyPair().getPublicKey().toString(16));
		 String publicKeyHash = CommUtil.substringFromEnd( 40  , publicKeyToSha3 ) ;
		 System.out.println("public hash : "+ publicKeyHash);
		 
		 
		 ObjectMapper objectMapper = new ObjectMapper();
		 Map<String, String> jsonFile = objectMapper.readValue(file, Map.class);
		 String resultJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonFile);
		 
		 
		 Gson gson = new Gson();
		 SymverseServerKeyStoreVO serverKeyStore = gson.fromJson(resultJson.toString(), SymverseServerKeyStoreVO.class);
		 SymverseWalletKeyStoreVO walletKeyStore = gson.fromJson(resultJson.toString(), SymverseWalletKeyStoreVO.class);
		 
		 
		 // walletkeystore setting
		 // 이더리움 구조체 셋팅
		 Ethereum ethObj = new Ethereum();
		 ethObj.setAddress(walletKeyStore.getAddress());
		 ethObj.setCurrencyid("010000000002"); 
		 ethObj.setCurrency("ETH");            
		 ethObj.setId("010000000002");         
		 ethObj.setName("이더리움");               
		 ethObj.setNetworkSymbol("ETH");       
		 List<String> nullArray = new ArrayList<String>();
		 ethObj.setToken(nullArray);
		 walletKeyStore.setEthereum(ethObj);
		 
		 // ca에 회원 가입 요청
		 OkHttpUtil OkHttpUtil = new OkHttpUtil();
		 SymIdSendPostVO symIdSendPostObj = new SymIdSendPostVO();
		 symIdSendPostObj.setEmail(email);
		 symIdSendPostObj.setId("2");
		 symIdSendPostObj.setPublicKeyHash(publicKeyHash);
		 symIdSendPostObj.setMobileNum(mobileNum);
		 symIdSendPostObj.setUserNm(userNm);
		 symIdSendPostObj.setVerificationType(verificationType); // CELL_PHONE
		 String requestJsonParam = objectMapper.writeValueAsString(symIdSendPostObj);
		 System.out.println("requestJsonParam : "+requestJsonParam);
		 String resultHttp = OkHttpUtil.sendPost("https://mainnet-ca.symverse.com/ca/v1/citizenInfo",requestJsonParam);
		 System.out.println(resultHttp);
		 LinkedTreeMap map = new Gson().fromJson(resultHttp, LinkedTreeMap.class);
		 
		 
		 // {httpStatus=OK, message=success, 
		 // result={symId=00024233482d6b6a0002, 
		 // citizenBlockList=[{id=2595.0, citizenId=00024233482d6b6a, seqNum=0002, publicKeyHash=c6e39527e4049f9ec066698ba95ffefd6a80ad07
		 // role=General, verificationFlag=CELL_PHONE, state=ACTIVE, credit=00, country=KOREA_REPUBLIC_OF, refCode=00000001, notBefore=20191121, notAfter=20201121, delYn=N, creId=CA, creDtt=2019-11-21T07:31:39.335, updId=null, updDtt=2019-11-21T07:31:39.335}]}}
		 
		 
		 String symId = (String) ( ((LinkedTreeMap)map.get("result")).get("symId") );
		 ArrayList citizenBlockListVO = (ArrayList)  ((LinkedTreeMap)map.get("result")).get("citizenBlockList")  ;
		 LinkedTreeMap citizenBlockMap = (LinkedTreeMap)  citizenBlockListVO.get(0);
		 String citizenId = (String) citizenBlockMap.get("citizenId").toString();
		 String role = (String) citizenBlockMap.get("role").toString();
		 String state = (String) citizenBlockMap.get("state").toString();
		 String seqNum = (String) citizenBlockMap.get("seqNum").toString();
		 
		 
		    
		 // ca 정보 셋팅
		 Ca caObj = new Ca();
		 caObj.setAddress(symId);
		 caObj.setCurrencyid("000000000001"); 
		 caObj.setCurrency("SYM");            
		 caObj.setId("000000000001");         
		 caObj.setName("심버스");               
		 caObj.setNetworkSymbol("SYM");       
		 List<String> nullArray2 = new ArrayList<String>();
		 caObj.setToken(nullArray2);
		 walletKeyStore.setCa(caObj);
		 walletKeyStore.setCreatedAt(1234567890); // 지갑에서도 안쓰는건데 아무값이나 그냥 넣자~
		 
		 //나머지 정보 셋팅
		 walletKeyStore.setName(userNm);
		 walletKeyStore.setRole(role);
		 SymId symIdObj = new SymId();
		 symIdObj.setAccountnum( Integer.parseInt(seqNum.replaceAll("0","")) );
		 symIdObj.setCitizenid(citizenId);
		 walletKeyStore.setSymId(symIdObj);
		 walletKeyStore.setState(state);
		 walletKeyStore.setPubHash(publicKeyHash);
		 
		 
		 // serverkeystore setting 
		 serverKeyStore.setAddress(symId); //symId 셋팅
		 
		 
		 
		 
		 log.debug("serverKeyStore : "+ objectMapper.writeValueAsString(serverKeyStore));
		 log.debug("walletKeyStore : "+ objectMapper.writeValueAsString(walletKeyStore));
		 log.debug("symId : "+ symId );

		 

		// "name": "치치",
	    //    "pubHash": "9f53757fb46b404e77898bbcdbb36c8a0ccf02cc",
	    //    "role": "General",
	    //    "state": "ACTIVE",
	    //    "symId": {
	    //        "accountNum": 2,
	    //        "citizenId": "00026159665b7e62"
	    //    },
	    //    "version": 4
		 
		 
		 return   "serverKeyStore : "+ objectMapper.writeValueAsString(serverKeyStore);
		 	
		 
		 
	}
	
	
	/**
	 * @param symId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/getBalance", method=RequestMethod.GET )
	@ResponseBody
	public Object getBalance(@RequestParam String symId ) throws Exception {
		List<String> requestParam = new ArrayList<String>();
		requestParam.add(CommUtil.preHex(symId));
		requestParam.add("latest");
		String getBalance = SymGetAPI.getSymAPIConnection("GET", systemEnv.NODE_URL ,"sym_getBalance", requestParam ,"");
		BigDecimal  amountValue  =  new BigDecimal(  getBalance ).divide(new BigDecimal(  "1000000000000000000" )) ;
		DecimalFormat dfFormat = new DecimalFormat("#.####");
		String str =  dfFormat.format( amountValue.floatValue() );
		return str;
	}
	
	// symCoin 토큰 보내기 메소드
	/**
	 * @param toSymId
	 * @param sendAmount
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/sendRawTransaction", method=RequestMethod.GET)
	@ResponseBody
	public Object sendRawTransaction( @RequestParam(defaultValue="keystore.json") String keyStoreFileName 
									, @RequestParam(defaultValue="") String keyStorePassWord 
									, @RequestParam String toSymIdAddress 
									, @RequestParam String amount ) throws Exception {
		String resultHashValue = sct20Factory.sendRawTransaction(keyStoreFileName, keyStorePassWord, toSymIdAddress, amount);
		return resultHashValue;
	}
	
	
	/** <pre>SCT21 계약을 생성합니다. - 토큰  생성</pre>
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
	@RequestMapping(value="/SCT21_CREATE", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_CREATE( @RequestParam(defaultValue="keystore.json") String keyStoreFileName 
						 , @RequestParam(defaultValue="") String keyStorePassWord
						 , @RequestParam String name
						 , @RequestParam String symBol
						 , @RequestParam String totalSupply
						 , @RequestParam String totalLockSupply
						 , @RequestParam(defaultValue="") String ownerSymId 
						 ) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_CREATE( keyStoreFileName, keyStorePassWord, name, symBol, totalSupply, totalLockSupply, ownerSymId);
		String result =  
		"{\r\n" + 
		"   \"jsonrpc\": \"2.0\",\r\n" + 
		"   \"id\": 2,\r\n" + 
		"   \"method\": \"sym_getTransactionReceipt\",\r\n" + 
		"   \"params\":[\""+transactionResultHashValue+"\"]\r\n" + 
		"}\r\n";	
		return result+"위 방식으로 엔진에 조회하면 sct21로 생성된  컨트렉트 주소를 알 수  있습니다.\r\nPostman- 099 Gsym Engine API - 0001 쿠폰 트랜잭션 sym_getTransactionReceipt 참조";
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
	@RequestMapping(value="/SCT21_TRANSFER", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_TRANSFER(  @RequestParam(defaultValue="keystore.json") String keyStoreFileName 
								 , @RequestParam(defaultValue="") String keyStorePassWord
								 , @RequestParam String contractAddress
								 , @RequestParam String recipientSymId
								 , @RequestParam String amount
								 ) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_TRANSFER(keyStoreFileName, keyStorePassWord, contractAddress, recipientSymId, amount);
		return transactionResultHashValue;
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
	@RequestMapping(value="/SCT21_TRANSFER_FROM", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_TRANSFER_FROM( @RequestParam(defaultValue="keystore.json") String keyStoreFileName 
									 , @RequestParam(defaultValue="") String keyStorePassWord
									 , @RequestParam String contractAddress
									 , @RequestParam String recipientSymId 
									 , @RequestParam String amount
									 ) throws Exception {
		
		
		String transactionResultHashValue = sct20Factory.SCT21_TRANSFER_FROM( keyStoreFileName, keyStorePassWord, contractAddress, recipientSymId, amount);
		return transactionResultHashValue;
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
	@RequestMapping(value="/SCT21_APPROVE", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_APPROVE(   @RequestParam(defaultValue="keystore.json") String keyStoreFileName 
								 , @RequestParam(defaultValue="") String keyStorePassWord
								 , @RequestParam String contractAddress
								 , @RequestParam String spenderSymId 
								 , @RequestParam String amount 
								 ) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_APPROVE(keyStoreFileName, keyStorePassWord, contractAddress, spenderSymId, amount);
		return transactionResultHashValue;
	}
	
	/** 위임된 토큰을 회수합니다. (Authorization: None)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @param amount - 위임될 토큰의 양
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_DECREASE_APPROVE", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_DECREASE_APPROVE(  @RequestParam(defaultValue="keystore.json") String keyStoreFileName 
										 , @RequestParam(defaultValue="") String keyStorePassWord
										 , @RequestParam String contractAddress
										 , @RequestParam String spenderSymId 
										 , @RequestParam String amount 
										 ) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_DECREASE_APPROVE(keyStoreFileName, keyStorePassWord, contractAddress, spenderSymId, amount);
		return transactionResultHashValue;
	}
	
	/** sct 21  토큰을 추가 발행합니다.
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @param amount - 위임될 토큰의 양
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_MINT", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_MINT(  @RequestParam(defaultValue="keystore.json") String keyStoreFileName 
							 , @RequestParam(defaultValue="") String keyStorePassWord
							 , @RequestParam String contractAddress
							 , @RequestParam String amount 
			 				) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_MINT(keyStoreFileName, keyStorePassWord, contractAddress, amount);
		return transactionResultHashValue;
	}
	
	/** sct 21 토큰 태움 - ( 토큰 총량 감소하기 )
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @param amount - 위임될 토큰의 양
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_BURN", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_BURN(  @RequestParam(defaultValue="keystore.json") String keyStoreFileName 
							 , @RequestParam(defaultValue="") String keyStorePassWord
							 , @RequestParam String contractAddress
							 , @RequestParam String amount 
							) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_BURN(keyStoreFileName, keyStorePassWord, contractAddress, amount);
		return transactionResultHashValue;
	}
	
	/** sct 21 토큰 거래 일시 정지  (Authorization: owner, creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	 */
	@RequestMapping(value="/SCT21_PAUSE", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_PAUSE( @RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_PAUSE(keyStoreFileName, keyStorePassWord, contractAddress);
		return transactionResultHashValue;
	}
	
	
	/** sct 21 토큰 거래 일시 정지  해제 ( Authorization: owner, creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_UNPAUSE", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_UNPAUSE(@RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress ) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_UNPAUSE(keyStoreFileName, keyStorePassWord, contractAddress);
		return transactionResultHashValue;
	}
	
	
	/** 계약의 소유자를 이전합니다. (Authorization: owner, creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress -  - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_TRANSFER_OWNER", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_TRANSFER_OWNER(@RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress
			 , @RequestParam String newOwnerSymId ) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_TRANSFER_OWNER( keyStoreFileName, keyStorePassWord, contractAddress, newOwnerSymId );
		return transactionResultHashValue;
	}
	
	/** 일부 토큰을 잠금 상태로 전송합니다. - (Authorization: owner, creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_LOCK_TRANSFER", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_LOCK_TRANSFER(@RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress
			 , @RequestParam String recipientSymId
			 , @RequestParam String amount
			 , @RequestParam String lockAmount
			) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_LOCK_TRANSFER(keyStoreFileName, keyStorePassWord, contractAddress, recipientSymId, amount, lockAmount);
		return transactionResultHashValue;
	}
	
	/** 잠금 상태로 전송한 토큰을 잠금 해제합니다.
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_UNLOCK_AMOUNT", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_UNLOCK_AMOUNT(@RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress
			 , @RequestParam String recipientSymId
			 , @RequestParam String amount
			) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_UNLOCK_AMOUNT(keyStoreFileName, keyStorePassWord, contractAddress, recipientSymId, amount);
		return transactionResultHashValue;
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
	@RequestMapping(value="/SCT21_RESTORE_LOCK_AMOUNT", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_RESTORE_LOCK_AMOUNT(@RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress 
			 , @RequestParam String recipientSymId 
			 , @RequestParam String amount 
			) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_RESTORE_LOCK_AMOUNT(keyStoreFileName, keyStorePassWord, contractAddress, recipientSymId, amount);
		return transactionResultHashValue;
	}
	
	/**  creator 소유의 잠금 상태의 토큰양을 추가합니다. (Authorization: creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @param amount - TotalLockSupply - add token amount
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_ADD_LOCK_AMOUNT", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_ADD_LOCK_AMOUNT(@RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress 
			 , @RequestParam String amount 
			 ) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_ADD_LOCK_AMOUNT(keyStoreFileName, keyStorePassWord, contractAddress, amount);
		return transactionResultHashValue;
	}
	
	 /** creator 소유의 잠금 상태의 토큰양을 회수 합니다. (Authorization: creator)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @param amount - TotalLockSupply - add token amount
	 * @return - transaction hash value
	 * @throws Exception
	 */
	@RequestMapping(value="/SCT21_SUB_LOCK_AMOUNT", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_SUB_LOCK_AMOUNT(@RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress 
			 , @RequestParam String amount 
			) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_SUB_LOCK_AMOUNT(keyStoreFileName, keyStorePassWord, contractAddress, amount);
		return transactionResultHashValue;
	}
	
	/** 특정 유저의 계약의 함수 사용(토큰 전송)을 잠금 상태로 변경합니다. (Authorization: creator, owner) - 잠금 상태의 유저는 TRANSFER, TRANSFER_FROM, APPROVE, DECREASE_APPROVE의 함수 실행을 할 수 없습니다.
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @param symId - SymId to change to locked state.
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_ACCOUNT_LOCK", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_ACCOUNT_LOCK(@RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress
			 , @RequestParam String symId
			) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_ACCOUNT_LOCK(keyStoreFileName, keyStorePassWord, contractAddress, symId);
		return transactionResultHashValue;
	}
	
	
	/** 특정 유저의 계약의 함수 사용(토큰 전송)의 잠금 해제합니다. (Authorization: creator, owner)
	 * @param keyStoreFileName - loading keystoreFile obj
	 * @param keyStorePassWord - keystore password
	 * @param contractAddress - sct21 contract address
	 * @param symId - SymId to unlock.
	 * @return - transaction hash value
	 * @throws Exception
	*/
	@RequestMapping(value="/SCT21_ACCOUNT_UNLOCK", method=RequestMethod.GET)
	@ResponseBody
	public Object SCT21_ACCOUNT_UNLOCK(@RequestParam(defaultValue="keystore.json") String keyStoreFileName 
			 , @RequestParam(defaultValue="") String keyStorePassWord
			 , @RequestParam String contractAddress 
			 , @RequestParam String symId
			) throws Exception {
		String transactionResultHashValue = sct20Factory.SCT21_ACCOUNT_UNLOCK(keyStoreFileName, keyStorePassWord, contractAddress, symId );
		return transactionResultHashValue;
	}
	
	
	/**
	 * @param password
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/passswordEnc", method=RequestMethod.GET)
	@ResponseBody
	public Object passswordEnc(@RequestBody String password ) throws Exception {
		log.debug("parameter password : "+password);
		String encStr = JasyptUtil.stringEncryptor(password);
		log.debug("passwordEnc : "+encStr);  // 
		return encStr;
	}
	
	/**
	 * @param password
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/passswordDec", method=RequestMethod.GET)
	@ResponseBody
	public Object passswordDec(@RequestBody String password ) throws Exception {
		log.debug("parameter password : "+password);
		String decStr = JasyptUtil.stringDecryptor(password);
		log.debug("passwordEnc : "+decStr);
		return decStr;
	}
	
	
}
