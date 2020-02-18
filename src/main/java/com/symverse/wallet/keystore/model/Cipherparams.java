/* Copyright 2019 freecodeformat.com */
package com.symverse.wallet.keystore.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cipherparams {

    private String iv;
    public void setIv(String iv) {
         this.iv = iv;
     }
     public String getIv() {
         return iv;
     }

}