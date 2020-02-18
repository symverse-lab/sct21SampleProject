package com.symverse.sct21.gsym.domain;

import org.web3j.protocol.core.Response;

public class SendSct21 extends Response<String>{

	public String getHash() {
        return getResult();
    }

}
