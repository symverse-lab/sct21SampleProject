package com.symverse.sct21.transaction.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

import com.symverse.common.model.CommUtil;
import com.symverse.sct21.transaction.domain.Sct21SendRawTransaction;


public class Sct21RawTransactionEncoderEngineVersion1_0_14 {
	
	static final Logger logger = LoggerFactory.getLogger(Sct21RawTransactionEncoderEngineVersion1_0_14.class);
	
	public static final byte[] TRUE = {0x01};
	public static final byte[] FALSE = {};
	
	

	
    public static byte[] signMessage(Sct21SendRawTransaction sct21SendRawTransaction, byte chainId, byte[] forkId , byte[] tempId , Credentials credentials ) {
        byte[] encodedTransaction = hashEncode(sct21SendRawTransaction, chainId , forkId , tempId  );
        Sign.SignatureData signatureData = Sign.signMessage(encodedTransaction, credentials.getEcKeyPair());
        Sign.SignatureData signSignatureData = new Sign.SignatureData(signatureData.getV(), signatureData.getR(), signatureData.getS());
        return encode(sct21SendRawTransaction, signSignatureData );
    }

    public static byte[] hashEncode(Sct21SendRawTransaction sct21SendRawTransaction, byte chainId , byte[] forkId , byte[] tempId  ) {
    	// Sign.SignatureData signatureData = new Sign.SignatureData(chainId, new byte[] {}, new byte[] {});
        Sign.SignatureData signatureData = new Sign.SignatureData(chainId, forkId , tempId);
        return hashEncode(sct21SendRawTransaction, signatureData );
    }

    private static byte[] hashEncode(Sct21SendRawTransaction sct21SendRawTransaction, Sign.SignatureData signatureData) {
		List<RlpType> values = hashAsRlpValues(sct21SendRawTransaction, signatureData );        
		RlpList rlpList = new RlpList(values);
		return RlpEncoder.encode(rlpList);
    }
    
    public static byte[] encode(Sct21SendRawTransaction sct21SendRawTransaction, byte chainId) {
        Sign.SignatureData signatureData = new Sign.SignatureData(chainId, new byte[] {}, new byte[] {});
        return encode(sct21SendRawTransaction, signatureData);
    }

    private static byte[] encode(Sct21SendRawTransaction sct21SendRawTransaction, Sign.SignatureData signatureData ) {
		List<RlpType> values = hashAsRlpValues(sct21SendRawTransaction, signatureData);
		RlpList rlpList = new RlpList(values);
		return RlpEncoder.encode(rlpList);
    }

    
    
    /**
     *  input 파라미터 생성
     * @param Sct21SendRawTransaction methodType1 토큰 교환 - 송금 
     * @return
     */
    static byte[] hashInputRlpMethodSendSym_Values(Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode( new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 0 - SCT21_CREATE - SCT21 계약을 생성합니다. 
     * @return 
     */
    static byte[] hashInputRlpMethodType0_Values(Sct21SendRawTransaction sct21SendRawTransaction) {
		   //Name	STRING Max: 10	SCT 계약 이름 입니다.
		   //Symbol	STRING Max: 3	SCT 계약 심볼 입니다.
		   //TotalSupply	QUANTITY	SCT 총 발행량 입니다.
		   //TotalLockSupply	QUANTITY	SCT 총 발행량에서 잠금 상태의 토큰 양입니다.
    	   //OwnerSymId	SYMID, 10 Bytes	계약 소유자 입니다.
    	   List<RlpType> setValue = new ArrayList<>();
           setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));  // method type 은 10진수 biginteger로 보내준다.
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create(sct21SendRawTransaction.getInput().getParams().get(0)));  // 계약 이름 영뮨 20자 이내
    	   contents.add( RlpString.create(sct21SendRawTransaction.getInput().getParams().get(1)));  // 계약 심볼 영문 정확히 3글자
    	   contents.add( RlpString.create(new BigInteger( sct21SendRawTransaction.getInput().getParams().get(2)) ));  // sct 총 발행량 // tpye - QUANTITY
    	   contents.add( RlpString.create(new BigInteger( sct21SendRawTransaction.getInput().getParams().get(3)) ));  // sct 총 발행량에서 잠근 상태의 토큰 양  // tpye - QUANTITY
    	   contents.add( RlpString.create(Numeric.hexStringToByteArray( sct21SendRawTransaction.getInput().getParams().get(4)) ));  // 계약 소유자
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode( new RlpList(setValue));
    	   
    }
   
    
    /**
     * @param Sct21SendRawTransaction methodType 1 - SCT21_TRANSFER - 소유한 토큰을 전송합니다.
     * @return 
     */
    static byte[] hashInputRlpMethodType1_Values(Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create(Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  // 토큰을 전달 받을 SymId입니다.
    	   contents.add( RlpString.create(new BigInteger( sct21SendRawTransaction.getInput().getParams().get(1)) ));  // 전송할 토큰의 양입니다.  
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    /**
     * @param Sct21SendRawTransaction methodType 2 - SCT21_TRANSFER_FROM 위임된 토큰을 전송합니다.  
     * @return 
     */
    static byte[] hashInputRlpMethodType2_Values(Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create(Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  // sct21 토큰을 생성한 SymId  - 토큰의 실제 소유 SymId입니다.
    	   contents.add( RlpString.create(Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(1)) ));  // 토큰을 전달 받을 SymId입니다.
    	   contents.add( RlpString.create(new BigInteger( sct21SendRawTransaction.getInput().getParams().get(2)) ));  // 전송할 토큰의 양입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 3 - SCT21_APPROVE - 소유한 토큰을 위임합니다.
     * @return 
     */
    static byte[] hashInputRlpMethodType3_Values(Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create(Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  // 토큰을 위임할 SymId입니다.
    	   contents.add( RlpString.create(new BigInteger( sct21SendRawTransaction.getInput().getParams().get(1)) ));  // 위임할 토큰의 양입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 4 - SCT21_DECREASE_APPROVE - 위임된 토큰을 회수합니다.
     * @return 
     */
    static byte[] hashInputRlpMethodType4_Values(Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create(Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  // 토큰을 다시 회수할 SymId입니다.
    	   contents.add( RlpString.create(new BigInteger( sct21SendRawTransaction.getInput().getParams().get(1)) ));  // 회수할 토큰의 양입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 5 - SCT21_MINT - 토큰을 추가 발행합니다. 총 발행량에 영향을 끼칩니다. (Authorization: owner, creator)
     * @return 
     */
    static byte[] hashInputRlpMethodType5_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create(Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  // sct21 (Authorization: owner, creator)  // (Authorization: owner, creator) -  sct21 계약의 owner or creator 만이 처리 할 수 있습니다. 	
    	   contents.add( RlpString.create(new BigInteger( sct21SendRawTransaction.getInput().getParams().get(1)) ));  // 추가 발행할 토큰의 양입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    
    /**
     * @param Sct21SendRawTransaction methodType 6 - SCT21_BURN - 토큰양의 일부를 제거합니다. 총 발행량에 영향을 끼칩니다. 
     * @return 
     */
    static byte[] hashInputRlpMethodType6_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create(Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  //	토큰을 제거 할 SymId입니다. // (Authorization: owner, creator) -  sct21 계약의 owner or creator 만이 처리 할 수 있습니다. 
    	   contents.add( RlpString.create(new BigInteger( sct21SendRawTransaction.getInput().getParams().get(1)) ));  // 제거할 토큰의 양입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    /**
     * @param Sct21SendRawTransaction methodType 7 - SCT21_PAUSE - 계약을 일시 정지합니다. (Authorization: owner, creator)
     * @return 
     */
    static byte[] hashInputRlpMethodType7_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();  // parameter 없음   - NONE
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 8 - SCT21_UNPAUSE - 계약의 일시 정지를 해제합니다. (Authorization: owner, creator)
     * @return 
     */
    static byte[] hashInputRlpMethodType8_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();  // parameter 없음   - NONE 
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    /**
     * @param Sct21SendRawTransaction methodType 9 - SCT21_TRANSFER_OWNER - 계약의 소유자를 이전합니다. (Authorization: owner, creator)
     * @return 
     */
    static byte[] hashInputRlpMethodType9_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create(Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  // 새로운 owner 권한을 부여할 SymId입니다. ※ owner은 1명만 가능합니다. 
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 10 - SCT21_LOCK_TRANSFER - 일부 토큰을 잠금 상태로 전송합니다. ( Authorization: owner, creator )
     * @return 
     */
    static byte[] hashInputRlpMethodType10_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create( Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  // 토큰을 받을 SymId입니다.
    	   contents.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getParams().get(1)) ));  // 전송할 토큰 양입니다.
    	   contents.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getParams().get(2)) ));  // 잠금 전송할 토큰 양입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 11 - SCT21_UNLOCK_AMOUNT - 잠금 상태로 전송한 토큰을 잠금 해제합니다. (Authorization: owner, creator)
     * @return 
     */
    static byte[] hashInputRlpMethodType11_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create( Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  // 잠금 상태의 토큰을 갖고 있는 SymId입니다.
    	   contents.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getParams().get(1)) ));  // 잠금 해제할 토큰 양입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    /**
     * @param Sct21SendRawTransaction methodType 12 - SCT21_RESTORE_LOCK_AMOUNT - 잠금 상태로 전송한 토큰을 회수합니다. (Authorization: owner, creator)
     * @return 
     */
    static byte[] hashInputRlpMethodType12_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
 	   List<RlpType> setValue = new ArrayList<>();
 	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
        setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
 	   List<RlpType> contents = new ArrayList<RlpType>();
 	   contents.add( RlpString.create( Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) ));  // 잠금 상태의 토큰을 갖고 있는 SymId입니다.
 	   contents.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getParams().get(1)) ));  // 잠금 상태로 전송한 토큰을 회수합니다. 
 	   setValue.add( new RlpList(contents));	   
 	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 13 - SCT21_ADD_LOCK_AMOUNT - creator 소유의 잠금 상태의 토큰양을 추가합니다. (Authorization: creator) 해당 함수 처리 후 creator의 토큰양은 계약의 TotalLockSupply로 추가 됩니다.
     * @return 
     */
    static byte[] hashInputRlpMethodType13_Values( Sct21SendRawTransaction sct21SendRawTransaction ) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getParams().get(0)) ));  // TotalLockSupply에 추가 할 토큰 양입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 14 - SCT21_SUB_LOCK_AMOUNT - creator 소유의 잠금 상태의 토큰양을 회수합니다. (Authorization: creator) - 해당 함수 처리 후 계약의 TotalLockSupply에 있는 토큰 양이 creator의 소유로 이전 됩니다.
     * @return 
     */
    static byte[] hashInputRlpMethodType14_Values( Sct21SendRawTransaction sct21SendRawTransaction ) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getParams().get(0)) ));  // creator의 소유로 회수할 토큰 양입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 15 - SCT21_ACCOUNT_LOCK - 특정 유저의 계약의 함수 사용(토큰 전송)을 잠금 상태로 변경합니다. (Authorization: creator, owner) - 잠금 상태의 유저는 TRANSFER, TRANSFER_FROM, APPROVE, DECREASE_APPROVE의 함수 실행을 할 수 없습니다.
     * @return 
     */
    static byte[] hashInputRlpMethodType15_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create( Numeric.hexStringToByteArray(   sct21SendRawTransaction.getInput().getParams().get(0)) )); // 잠금 상태로 변경할 SymId입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param Sct21SendRawTransaction methodType 16 - SCT21_ACCOUNT_UNLOCK - 특정 유저의 계약의 함수 사용(토큰 전송)을 잠금 상태로 변경합니다. (Authorization: creator, owner) - 잠금 상태의 유저는 TRANSFER, TRANSFER_FROM, APPROVE, DECREASE_APPROVE의 함수 실행을 할 수 없습니다.
     * @return 
     */
    static byte[] hashInputRlpMethodType16_Values( Sct21SendRawTransaction sct21SendRawTransaction) {  
    	   List<RlpType> setValue = new ArrayList<>();
    	   setValue.add( RlpString.create( Numeric.hexStringToByteArray( CommUtil.strintDecimaltoHexValueConvert ( sct21SendRawTransaction.getInput().getSctType() )))  );
           setValue.add( RlpString.create( new BigInteger( sct21SendRawTransaction.getInput().getMethod() ) ));
    	   List<RlpType> contents = new ArrayList<RlpType>();
    	   contents.add( RlpString.create( Numeric.hexStringToByteArray(  sct21SendRawTransaction.getInput().getParams().get(0)) )); // 잠금 상태로 변경할 SymId입니다.
    	   setValue.add( new RlpList(contents));	   
    	   return  RlpEncoder.encode(new RlpList(setValue));
    }
    
    
    /**
     * @param sct21SendRawTransaction
     * 
     * @return List<RlpType>
     */
    static List<RlpType> hashWorkNodeRlpValues(Sct21SendRawTransaction sct21SendRawTransaction) {
       List<RlpType> result = new ArrayList<>();
       result.add(RlpString.create(Numeric.hexStringToByteArray(sct21SendRawTransaction.getWorkNode().get(0))));  // worknode1
       //result.add(RlpString.create(Numeric.hexStringToByteArray(couponRawTransaction.getWorkNodesList().get(1))));  // worknode2
       //result.add(RlpString.create(Numeric.hexStringToByteArray(couponRawTransaction.getWorkNodesList().get(2))));  // worknode3
       return result;
    }
    
  
    
    static List<RlpType> hashAsRlpValues(Sct21SendRawTransaction sct21SendRawTransaction, Sign.SignatureData signatureData ) {
    	List<RlpType> result = new ArrayList<>();
    	
        result.add(RlpString.create( Numeric.hexStringToByteArray(sct21SendRawTransaction.getFrom())));
        result.add(RlpString.create( sct21SendRawTransaction.getNonce()) );
        result.add(RlpString.create( sct21SendRawTransaction.getGasPrice()));
        result.add(RlpString.create( sct21SendRawTransaction.getGasLimit()));
        if(sct21SendRawTransaction.getTo().equals("null")) {
       	 	result.add(RlpString.create( FALSE ));  // to 
        }else {
        	result.add(RlpString.create( Numeric.hexStringToByteArray(sct21SendRawTransaction.getTo()) ));  // to  
        }
        result.add(RlpString.create( sct21SendRawTransaction.getValue() )); //value
        
        
        String methodType =sct21SendRawTransaction.getInput().getMethod();  // input rlp encoding 분기처리
        if( methodType.equals("0") ) {
       	   result.add( RlpString.create( hashInputRlpMethodType0_Values(sct21SendRawTransaction)) ); // SCT21_CREATE
        }else if(  methodType.equals("1") ) {
           result.add( RlpString.create( hashInputRlpMethodType1_Values(sct21SendRawTransaction)) ); // SCT21_TRANSFER
        }else if(  methodType.equals("2") ) {
           result.add( RlpString.create( hashInputRlpMethodType2_Values(sct21SendRawTransaction)) ); // SCT21_TRANSFER_FROM
        }else if(  methodType.equals("3") ) {
           result.add( RlpString.create( hashInputRlpMethodType3_Values(sct21SendRawTransaction)) ); // SCT21_APPROVE 
        }else if(  methodType.equals("4") ) {
           result.add( RlpString.create( hashInputRlpMethodType4_Values(sct21SendRawTransaction)) ); // SCT21_DECREASE_APPROVE 
        }else if(  methodType.equals("5") ) {
            result.add( RlpString.create( hashInputRlpMethodType5_Values(sct21SendRawTransaction)) ); // SCT21_MINT
        }else if(  methodType.equals("6") ) {
            result.add( RlpString.create( hashInputRlpMethodType6_Values(sct21SendRawTransaction)) ); // SCT21_BURN
        }else if(  methodType.equals("7") ) {
            result.add( RlpString.create( hashInputRlpMethodType7_Values(sct21SendRawTransaction)) ); // SCT21_PAUSE
        }else if(  methodType.equals("8") ) {
            result.add( RlpString.create( hashInputRlpMethodType8_Values(sct21SendRawTransaction)) ); // SCT21_UNPAUSE
        }else if(  methodType.equals("9") ) {
            result.add( RlpString.create( hashInputRlpMethodType9_Values(sct21SendRawTransaction)) ); // SCT21_TRANSFER_OWNER
        }else if(  methodType.equals("10") ) {
            result.add( RlpString.create( hashInputRlpMethodType10_Values(sct21SendRawTransaction)) );  // SCT21_LOCK_TRANSFER
        }else if(  methodType.equals("11") ) {
            result.add( RlpString.create( hashInputRlpMethodType11_Values(sct21SendRawTransaction)) );  // SCT21_UNLOCK_AMOUNT
        }else if(  methodType.equals("12") ) {
            result.add( RlpString.create( hashInputRlpMethodType12_Values(sct21SendRawTransaction)) );  // SCT21_RESTORE_LOCK_AMOUNT
        }else if(  methodType.equals("13") ) {
            result.add( RlpString.create( hashInputRlpMethodType13_Values(sct21SendRawTransaction)) ); // SCT21_ADD_LOCK_AMOUNT
        }else if(  methodType.equals("14") ) {
            result.add( RlpString.create( hashInputRlpMethodType14_Values(sct21SendRawTransaction)) ); // SCT21_SUB_LOCK_AMOUNT
        }else if(  methodType.equals("15") ) {
            result.add( RlpString.create( hashInputRlpMethodType15_Values(sct21SendRawTransaction)) ); // SCT21_ACCOUNT_LOCK
        }else if(  methodType.equals("16") ) {
            result.add( RlpString.create( hashInputRlpMethodType16_Values(sct21SendRawTransaction)) );  // SCT21_ACCOUNT_UNLOCK
        }else if(  methodType.equals("-1") ) {
            result.add( RlpString.create( hashInputRlpMethodSendSym_Values(sct21SendRawTransaction)) );  // send SymCoin
        }
        
        result.add(RlpString.create( sct21SendRawTransaction.getType().equals("1") ? TRUE : FALSE ));
        result.add(new RlpList(hashWorkNodeRlpValues(sct21SendRawTransaction))); // worknodes 
        result.add(RlpString.create( TRUE )); // extra
        if (signatureData.getV() == 0) {  // v
          result.add(RlpString.create(FALSE)); 
        } else {
       	  result.add(RlpString.create(BigInteger.valueOf(signatureData.getV())));
        }
        
        result.add(RlpString.create( Bytes.trimLeadingZeroes(signatureData.getR()) ));
        result.add(RlpString.create( Bytes.trimLeadingZeroes(signatureData.getS()) ));
        logger.debug("asRlpValues Finish");
        return result;
        
    }
    
   

}
