package com.groupstp.datasupplier.core.bean.datasupplier.provider.bank.dadata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * DaData bank provider sending data transfer object
 *
 * @author adiatullin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaDataBankRequest implements Serializable {
    private static final long serialVersionUID = 9191151992595505061L;

    @JsonProperty("query")
    private String query;
    @JsonProperty("count")
    private Integer count;


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
