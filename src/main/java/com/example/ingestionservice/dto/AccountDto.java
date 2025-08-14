package com.example.ingestionservice.dto;

public class AccountDto {
    private String id;
    private String name;
    private String type;

    public AccountDto(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
}
