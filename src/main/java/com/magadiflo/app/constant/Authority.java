package com.magadiflo.app.constant;

public class Authority {

    //De esta manera representaremos los permisos
    public static final String[] USER_AUTHORITIES = {"user:read"}; //usuario
    public static final String[] HR_AUTHORITIES = {"user:read", "user:update"}; //recursos humanos
    public static final String[] MANAGER_AUTHORITIES = {"user:read", "user:update"}; //gerente
    public static final String[] ADMIN_AUTHORITIES = {"user:read", "user:create", "user:update"}; //administrador
    public static final String[] SUPER_ADMIN_AUTHORITIES = {"user:read", "user:create", "user:update", "user:delete"}; //súper usuario

}
