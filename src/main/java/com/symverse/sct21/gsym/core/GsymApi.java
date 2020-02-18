package com.symverse.sct21.gsym.core;

import org.web3j.protocol.core.Request;

import com.symverse.sct21.gsym.domain.SendSct21;

public interface GsymApi {

    //추가 20190426
    public Request<?, SendSct21> sct21SendRawTransaction (String signedTransactionData);

    
    /*
     * 
     * 

    public Request<?, Response> symGetRawCitizenByHash(String hash);

    public Request<?, Response> symGetRawCitizenBySymID(String symId);

    public Request<?, Response> symGetCitizens();

    public Request<?, Response> symClientVerseion();*/

}
