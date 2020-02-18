package com.symverse.sct21.transaction.domain;

import java.math.BigInteger;
import java.util.List;

import com.symverse.sct21.transaction.domain.Sct21TempleteVO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Sct21SendRawTransaction {

	   // 이더리움 TX 구조
	   // [
	   //     "from" : "" ,
	   //     "nonce" : "" ,
	   //     "gasPrice" : "" ,
	   //     "gasLimit" : "" ,
	   //     "to" : "" ,
	   //     "value" : "" ,
	   //     "input" : "" , 
	
	       // input의 파라미터는 다음과 같다.
		   // template = [ 
		   //   “type”: “0x0”, 
		   // 	“method”: “0x0”,   //     "type" : "" ,
		   //  	“params = ["SymverseCoin", "SYM", "0x5000000000000000", "0x0000000001” ]   //   
		   // ]   //     "v" : "" ,
	
	   //     "r" : "" ,
	   //     "s" : "" 
	   // ]
		                           

	   private String from        ; 
	   private BigInteger nonce    ;
	   private BigInteger gasPrice   ; 
	   private BigInteger gasLimit   ; 
	   private String to     ; 
	   private BigInteger value      ;
	   private Sct21TempleteVO input ;
	   private String type;
	   private List<String> workNode;
	   private String extra;
	   
	   
	   public Sct21SendRawTransaction() {
		   this.gasPrice = new BigInteger("25000000000"); // 값 고정                                                
		   this.gasLimit = new BigInteger("2000000"); // 값 고정
	   }
	   
	 
	   

}
