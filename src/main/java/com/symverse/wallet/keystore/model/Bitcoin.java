/* Copyright 2019 freecodeformat.com */
package com.symverse.wallet.keystore.model;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Bitcoin {

    private String address;
    private String currency;
    @JsonProperty("currencyId")
    private String currencyid;
    private String id;
    private String name;
    @JsonProperty("network_symbol")
    private String networkSymbol;
    private List<String> token;
  

}