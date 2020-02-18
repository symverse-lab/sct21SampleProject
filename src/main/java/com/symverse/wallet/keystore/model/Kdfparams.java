/* Copyright 2019 freecodeformat.com */
package com.symverse.wallet.keystore.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Kdfparams {

    private int dklen;
    private int n;
    private int p;
    private int r;
    private String salt;

}