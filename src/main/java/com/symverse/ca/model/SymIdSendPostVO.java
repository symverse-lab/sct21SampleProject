package com.symverse.ca.model;


public class SymIdSendPostVO {
    private String userNm;
    private String publicKeyHash;
    private String mobileNum;
    private String email;
    private String verificationType;
    private String id;
    public void setUserNm(String userNm) {
         this.userNm = userNm;
     }
     public String getUserNm() {
         return userNm;
     }

    public void setPublicKeyHash(String publicKeyHash) {
         this.publicKeyHash = publicKeyHash;
     }
     public String getPublicKeyHash() {
         return publicKeyHash;
     }

    public void setMobileNum(String mobileNum) {
         this.mobileNum = mobileNum;
     }
     public String getMobileNum() {
         return mobileNum;
     }

    public void setEmail(String email) {
         this.email = email;
     }
     public String getEmail() {
         return email;
     }

    public void setVerificationType(String verificationType) {
         this.verificationType = verificationType;
     }
     public String getVerificationType() {
         return verificationType;
     }

    public void setId(String id) {
         this.id = id;
     }
     public String getId() {
         return id;
     }
}
