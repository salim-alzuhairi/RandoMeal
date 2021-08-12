package com.example.randomeal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.cdma.CdmaCellLocation;


import androidx.annotation.Nullable;

import java.util.ArrayList;


class db_manager extends SQLiteOpenHelper {

    SQLiteDatabase db_write = this.getWritableDatabase();
    SQLiteDatabase db_read = this.getReadableDatabase();

////////////////// data base /////////////////////////////////////

    private static String db_name = "random_meal.db";
    private  static int db_version = 1;

////////////////// sub item /////////////////////////////////////

    private static String food_list = "food_list";
    private static String list_id_key = "list_id";
    private static String name_key = "name";
    private static String image_name_key = "image_name";
    private static String image_key = "image";

/////////////////// list //////////////////////////////////////////

    private static String table_order_name = "list";
    private static String list_name = "name";
    private static String list_color = "color";


    content_manager content_manager;

    public db_manager(@Nullable Context context) {
        super(context, db_name, null, db_version);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

////////////////////////// list ///////////////////////////////////////
        String create_order_query = "create table " + table_order_name + " ("
                + list_name + " text , "
                + list_color + " int)";


        /////////////////////////////// item /////////////////////////////////////////////
        String create_item_query = "create table " + food_list + " ("
                + list_id_key + " int, "
                + name_key + " text, "
                + image_name_key + " text, "
                + image_key + " blob, foreign key ( " + list_id_key + " ) references " + table_order_name + ")";


        sqLiteDatabase.execSQL(create_order_query);

        sqLiteDatabase.execSQL(create_item_query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String delete_item = "drop table if exists " + food_list;
        String delete_table = "drop table if exists " + table_order_name;
        sqLiteDatabase.execSQL(delete_table);
        sqLiteDatabase.execSQL(delete_item);
        onCreate(sqLiteDatabase);
    }


    public void add_order(content_manager content_manager) {

        ContentValues val = new ContentValues();
        val.put(list_name, content_manager.getList_name());
        val.put(list_color, content_manager.getList_color());

        db_write.insert(table_order_name, null, val);

    }

    public ArrayList<content_manager> read_order(){

        ArrayList arrayList = new ArrayList();
        Cursor cursor = db_read.rawQuery("select * from " + table_order_name, null);
        Cursor cursor1 = db_read.rawQuery("select rowid as 'row' from " + table_order_name,null);

        if (cursor.moveToFirst() && cursor1.moveToFirst()) {

            do {

                int id = cursor1.getInt(cursor1.getColumnIndex("row"));
                String name = cursor.getString(cursor.getColumnIndex(list_name));
                int color = cursor.getInt(cursor.getColumnIndex(list_color));

                content_manager = new content_manager(true, id,  name, color);

                arrayList.add(content_manager);

            }while (cursor.moveToNext() && cursor1.moveToNext());

        }

        return arrayList;

    }

    public void up_order(content_manager content_manager){

        ContentValues c = new ContentValues();


        db_write.update(table_order_name, c, null, null);

    }

    public void del_order(content_manager content_manager){

        db_write.delete(food_list, list_id_key + "=?", new String[]{String.valueOf(content_manager.getList_id())});
        db_write.delete(table_order_name, "rowid" + "=?", new String[]{String.valueOf(content_manager.getList_id())});
        db_write.execSQL("vacuum");
        db_write.execSQL("delete from " + food_list + " where " + list_id_key + "<=0");
        db_write.execSQL("update " + food_list + " set " + list_id_key + "=" + list_id_key + "-1 where " + list_id_key + ">" + content_manager.getList_id());

    }

    public int get_length_list() {

        Cursor c = db_read.rawQuery("SELECT count(*) as 'count' FROM " + table_order_name, null);

        c.moveToFirst();

        int r = c.getInt(c.getColumnIndex("count"));

        return r;

    }

    /////////////////////////// item //////////////////////////////////////////////////////////////

    public void add_item(content_manager content_manager) {


        ContentValues content_meal = new ContentValues();
        content_meal.put(name_key, content_manager.getName_key());
        content_meal.put(list_id_key, content_manager.getList_id_key());
        content_meal.put(image_name_key, content_manager.getImage_name_key());
        content_meal.put(image_key, content_manager.getImage_key());

        db_write.insert(food_list, null, content_meal);

    }

    public ArrayList<content_manager> read_item(int list_id){

        list_id = list_id+1;
        ArrayList array_list = new ArrayList();

        String com = " where " + list_id_key + "=" + list_id;
        Cursor cursor = db_read.rawQuery("select * from " + food_list + com, null);
        Cursor cursor1 = db_read.rawQuery("select rowid as 'row' from " + food_list + com, null);
        if (cursor.moveToFirst() && cursor1.moveToFirst()) {
            do {

                int id = cursor1.getInt(cursor1.getColumnIndex("row"));
                int listId = cursor.getInt(cursor.getColumnIndex(list_id_key));
                String name = cursor.getString(cursor.getColumnIndex(name_key));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(image_key));
                String s = name;

                content_manager = new content_manager(id, listId, s, null, image);
                array_list.add(content_manager);

            } while (cursor.moveToNext() && cursor1.moveToNext());
        }
        return array_list;
    }

    public void del_item(int id){

        ContentValues v = new ContentValues();
        db_write.delete(food_list, "rowid" + "=?", new String[]{String.valueOf(id)});
        db_write.execSQL("vacuum");

    }

    public byte[] get_image(int tag) {
        tag++;
        Cursor c = db_read.rawQuery("select " + image_key + " from " + food_list + " where rowid=" + tag, null);

        c.moveToFirst();

        return c.getBlob(c.getColumnIndex(image_key));

    }

    public int get_length_item(boolean all_item, int list_id){

        list_id = list_id+1;
        Cursor c;

        String query = "select count(*) as 'count' from " + food_list;

        if (all_item){

            c = db_read.rawQuery(query, null);

        }else {
            c = db_read.rawQuery( query + " where " + list_id_key + " = " + list_id, null);

        }
        c.moveToFirst();

        int r = c.getInt(c.getColumnIndex("count"));
        return r;

    }

}
