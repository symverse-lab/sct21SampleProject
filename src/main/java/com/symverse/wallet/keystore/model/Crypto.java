/* Copyright 2019 freecodeformat.com */
package com.symverse.wallet.keystore.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Crypto {

    private String cipher;
    private Cipherparams cipherparams;
    private String ciphertext;
    private String kdf;
    private Kdfparams kdfparams;
    private String mac;

}