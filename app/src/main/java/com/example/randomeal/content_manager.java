package com.example.randomeal;


import androidx.annotation.Nullable;

class content_manager {

    ////////////////// sub item /////////////////////////////////////
    private int id_key;
    private int list_id_key;
    private String name_key;
    private String image_name_key;
    private byte[] image_key;

    /////////////////// list //////////////////////////////////////////
    private int list_id;
    private String list_name;
    private int list_color;


    ////////////////// item ///////////////////////////////
    public content_manager(int id_key, int list_id_key, String name_key, String image_name_key, byte[] image_key) {
        this.id_key = id_key;
        this.list_id_key = list_id_key;
        this.name_key = name_key;
        this.image_name_key = image_name_key;
        this.image_key = image_key;
    }

    //////////////////// list /////////////////////////////////////
    public content_manager(@Nullable Boolean isList, int list_id, String list_name, int list_color) {
        this.list_id = list_id;
        this.list_name = list_name;
        this.list_color = list_color;
    }

    ////////////////// item ///////////////////////////////////////////////////////////
    public int getId_key() {
        return id_key;
    }


    public int getList_id_key() {
        return list_id_key;
    }


    public String getName_key() {
        return name_key;
    }


    public String getImage_name_key() {
        return image_name_key;
    }


    public byte[] getImage_key() {
        return image_key;
    }


    //////////////////////// list /////////////////////////////////////////////////////////////////////////
    public int getList_id() {
        return list_id;
    }


    public String getList_name() {
        return list_name;
    }


    public int getList_color() {
        return list_color;
    }

}
