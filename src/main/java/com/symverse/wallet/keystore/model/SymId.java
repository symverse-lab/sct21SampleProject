/* Copyright 2019 freecodeformat.com */
package com.symverse.wallet.keystore.model;
import org.codehaus.jackson.annotate.JsonProperty;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SymId {

    @JsonProperty("accountNum")
    private int accountnum;
    @JsonProperty("citizenId")
    private String citizenid;

}