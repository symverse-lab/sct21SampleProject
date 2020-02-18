/* Copyright 2019 freecodeformat.com */
package com.symverse.wallet.keystore.model;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SymverseWalletKeyStoreVO {

    private String address;
    private Bitcoin bitcoin;
    private Ca ca;
    private int createdAt;
    private Crypto crypto;
    private Ethereum ethereum;
    private String id;
    private String name;
    private String pubHash;
    private String role;
    private String state;
    private SymId symId;
    private int version;
    

}