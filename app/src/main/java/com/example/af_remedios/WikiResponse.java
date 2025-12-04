package com.example.af_remedios;

import com.google.gson.annotations.SerializedName;

public class WikiResponse {
    @SerializedName("extract")
    private String extract;

    public String getExtract() { return extract; }
}