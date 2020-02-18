/* Copyright 2019 freecodeformat.com */
package com.symverse.server.keystore.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SymverseServerKeyStoreVO {

    private String address;
    private Crypto crypto;
    private String id;
    private int version;

}