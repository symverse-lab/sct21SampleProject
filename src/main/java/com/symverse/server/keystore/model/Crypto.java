/* Copyright 2019 freecodeformat.com */
package com.symverse.server.keystore.model;

import lombok.Setter;

import lombok.Getter;

@Getter
@Setter
public class Crypto {

    private String cipher;
    private String ciphertext;
    private Cipherparams cipherparams;
    private String kdf;
    private Kdfparams kdfparams;
    private String mac;

}