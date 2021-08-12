package com.example.randomeal;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class add_meal extends AppCompatActivity {

    ExpandingList expanding_list;
    View custom, fab, color_image, color_picker, fab_ok, image_import_layout;
    InputMethodManager input;
    Button cancel, save;
    ImageView image_import, imageView;
    TextView text_import, text_title, text_color_import;
    DocumentFile document_file;
    TextInputLayout text_layout;
    TextInputEditText meal_name;
    ExpandingItem item;
    AdView mAdView;
    String text_type;
    int image_upload, select_color, images;
    boolean bool;

    db_manager db;
    content_manager content;

    Uri uri; //يحمل مسار الصورة المختارة للوجبة

    ArrayList tags = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);



        expanding_list = findViewById(R.id.expanding_list_main);
        input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        fab = findViewById(R.id.fab_add_list);
        fab_ok = findViewById(R.id.fab_ok);
        mAdView = findViewById(R.id.adView2);

        ads();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                toast(getString(R.string.permission), false);
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1024);

            }
        }

        try {
            create_items();
        }catch (CursorIndexOutOfBoundsException ex){

        }
        fab.setOnClickListener(view -> showInsertDialog(false, 0));
    }

    //دالة onclick لعنصرين في color_picker.xml تستخدم في add_meal.xml
    public void add_color(View view) {

        ColorPickerDialogBuilder
                .with(add_meal.this)
                .setTitle(getString(R.string.choose_order_color))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton(R.string.done, (dialog, selectedColor, allColors) -> {
                    color_image.setBackgroundColor(selectedColor);
                    text_color_import.setText("#" + Integer.toHexString(selectedColor));
                    select_color = selectedColor;

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                })
                .build()
                .show();
    }

    //دالة للتعامل مع الصورة بعد اختيارها من المعرض
    ActivityResultLauncher lan = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            imageView = new ImageView(add_meal.this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));

            //التحقق من ان رمز الطلب هو نفس رمز طلب الصورة
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                uri = result.getData().getData(); //مسار الصورة(مشاكل في المسار)

                document_file = DocumentFile.fromSingleUri(add_meal.this, uri); //تحويل المسار الى ملف
                Glide.with(add_meal.this).load(document_file.getUri()).into(imageView);
                Glide.with(add_meal.this).load(document_file.getUri()).into(image_import);
                text_import.setText(document_file.getName());
            }
        }
    });
    //دالة onclick لعنصرين في image_import.xml تستخدم في add_meal.xml
    public void add_image(View view) {

        input.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        lan.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));

    }

    //دالة لإضافة العناصر والقوائم
    private void add_item(String title, int colorRes, int tag, String[] subItems, Bitmap[] image, int[] item_tags) {
        //Let's create an item with R.layout.expanding_layout
        item = expanding_list.createNewItem(R.layout.expanding_layout); //انشاء قائمة جديدة

        //If item creation is successful, let's configure it
        if (item != null) {
            item.setIndicatorIconRes(R.drawable.app_icon);
            //It is possible to get any view inside the inflated layout. Let's set the text in the item
            ((TextView) item.findViewById(R.id.title)).setText(title);
            set_color(item , colorRes); //استدعاء دالة set_color لوضع لون القائمة
            item.setTag(tag); //وضع Tag للقائمة لإستخدامة لحذف القائمة
            item.findViewById(R.id.add_more_sub_items).setTag(tag);

            //We can create items in batch.
            item.createSubItems(subItems.length); //انشاء عناصر

            //تكرار بعدد العناصر المنشأة
            for (int i = 0; i < item.getSubItemsCount(); i++) {
                //Let's get the created sub item by its index
                final View view = item.getSubItemView(i); //متغير للقائمة i
                view.findViewById(R.id.sub_image).setTag(tags.size());
                tags.add(i);

                //Let's set some values in
                configureSubItem(item, view, subItems[i], image[i], item_tags[i]); //استدعاء دالة configureSubItem واعطائها البيانات المطلوبة

            }

            //عند الضغط على اضافة وجبة
            item.findViewById(R.id.add_more_sub_items).setOnClickListener(v -> {
                showInsertDialog(true, (int)v.getTag()); //استدعاء دالة showInsertDialog وتحديد انها وجبة وليست قائمة واعطائها القائمة
            });

            //عند الضغط على حذف القائمة
            item.findViewById(R.id.remove_item).setOnClickListener(v -> {
                text_type = "قائمة"; //مهم قبل استدعاء delete_dialog
                delete_dialog(item, 0); //استدعاء دالة delete_dialog
            });
        }

    }

    //لإدارة العناصر
    private void configureSubItem(final ExpandingItem item, final View view, String subTitle, Bitmap bitmap, int tag) {
        ((TextView) view.findViewById(R.id.sub_title)).setText(subTitle); //اعطاء العنصر اسمه
        final ImageView[] image = {view.findViewById(R.id.sub_image)}; //صورة العنصر
        //التحقق من ان الصورة ليست null
        if (bitmap != null) {

            image[0].setImageBitmap(bitmap);

        } else { //في حال كانت الصورة null

            image[0].setImageDrawable(getResources().getDrawable(R.drawable.app_icon)); //تعيين ايقونة التطبيق
        }
        view.setTag(tag); //تعيين Tag للعنصر لإستخدامه لحذف العنصر
        int t = (int) view.getTag(); //جلب Tag العنصر

        //عند الضغط على حذف العنصر
        view.findViewById(R.id.remove_sub_item).setOnClickListener(view12 -> {
            text_type = "وجبة"; //مهم قبل استدعاء delete_dialog
            delete_dialog(item, t); //استدعاء دالة delete_dialog
        });

        CheckBox check_all = item.findViewById(R.id.check_all_list);
        bool = true;

        check_all.setOnClickListener(view1 -> bool = true);

        check_all.setOnCheckedChangeListener((compoundButton, b) -> {

            if (bool) {
                for (int i = 0; i < item.getSubItemsCount(); i++) {
                    View check_sub = item.getSubItemView(i);
                    CheckBox check = check_sub.findViewById(R.id.check_sub);
                    check.setChecked(b);
                }
            }
        });

        CheckBox check_sub = view.findViewById(R.id.check_sub);
        check_sub.setOnCheckedChangeListener((compoundButton, b) -> {


            ArrayList array_bol = new ArrayList(){};

            for (int i = 0; i < item.getSubItemsCount(); i++) {
                View v = item.getSubItemView(i);
                CheckBox c = v.findViewById(R.id.check_sub);
                //التحقق من تحديد العناصر
                if (c.isChecked()) {
                    array_bol.add(true);
                }else {
                    array_bol.add(false);
                }
            }
            bool = true;
            //التحقق من ان جميع العناصر محددة
            if (!array_bol.contains(false)) {

                check_all.setChecked(true);

            }else{ //التحقق من ان هناك عنصر واحد على الاقل غير محدد
                bool = false;
                check_all.setChecked(false);
            }
        });

        fab_ok.setOnClickListener(new View.OnClickListener() {

            Bundle bundle = new Bundle();

            @Override
            public void onClick(View view) {

                ArrayList name_list = new ArrayList();
                ArrayList image_list = new ArrayList();

                int count = expanding_list.getItemsCount();
                for (int i = 0; i < count; i++) {

                    ExpandingItem sub = expanding_list.getItemByIndex(i);

                    for (int j = 0; j < sub.getSubItemsCount(); j++) {

                        CheckBox check = sub.getSubItemView(j).findViewById(R.id.check_sub);
                        TextView name = sub.getSubItemView(j).findViewById(R.id.sub_title);
                        images = (int) sub.getSubItemView(j).findViewById(R.id.sub_image).getTag();

                        if (check.isChecked()) {

                            name_list.add(name.getText());
                            image_list.add(images);
                            bundle.putSerializable("image", image_list);

                        }
                    }
                }

                Intent intent = new Intent(add_meal.this, MainActivity.class);


                bundle.putSerializable("name", name_list);
                intent.putExtra("bundle", bundle);

                startActivity(intent);

            }
        });
    }

    //دالة لجلب وتنظيم البيانات من قاعة البيانات وارسالها لدالة add_item
    private void create_items() {

        db = new db_manager(add_meal.this);
        content_manager read_order, read_item; //انشاء متغيرين من نوع content_manager لقرائة القوائم وقرائة العناصر
        String[] sub_name; //يوضع فيها اسماء العناصر
        int[] sub_id; //يوضع فيها id العناصر لإستخدامها كTag
        Bitmap[] imgs;

        for (int i = 0; i < db.get_length_list(); i++) { //تكرار بعدد القوائم في قاعدة البيانات

            int length = db.get_length_item(false, i); //عدد العناصر في القائمة
            read_order = db.read_order().get(i); //يتم جلب القائمة رقم i من قاعدة البيانات
            sub_name = new String[length]; //وضع عدد العناصر للقائمة
            sub_id = new int[length];
            imgs = new Bitmap[length];

            for (int j = 0; j < length; j++) {

                read_item = db.read_item(i).get(j); //لجلب العنصر j  من القائة i من قاعدة البيانات

                //التحقق من ان العنصر مرتبط بهذه القائمة
                if (read_item.getList_id_key() == read_order.getList_id()){

                    //اضافة اسم وid العنصر الى المصفوفتين
                    sub_name[j] = read_item.getName_key();
                    sub_id[j] = read_item.getId_key();

                    //التحقق من ان الصورة ليست null
                    if (read_item.getImage_key() != null)
                        imgs[j] = (BitmapFactory.decodeByteArray(read_item.getImage_key(), 0, read_item.getImage_key().length)); //وضع الصورة من قاعدة البيانات في bitmap
                    else
                        imgs[j] = null;

                }
            }
            add_item(read_order.getList_name(), read_order.getList_color(), read_order.getList_id(), sub_name, imgs, sub_id); //استدعاء دالة add_item واعطائها البيانات
        }
    }

    //Dialog يظهر عند حذف العناصر والقوائم
    public void delete_dialog(ExpandingItem item, int tag){

        new AlertDialog.Builder(add_meal.this)
                .setTitle(getString(R.string.delete_the) + text_type) //قائمة\عنصر
                .setMessage(getString(R.string.are_you_sure_to_delete_the) + text_type + getString(R.string.ques))
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {

                    if (text_type.equals("قائمة")){ //في حال حذف قائمة

                        int d = (Integer) item.getTag(); //الحصول على Tag القائمة
                        content = new content_manager(true, d, null, 0); //نضع رقم ال Tag مكان id للقائمة
                        db = new db_manager(add_meal.this);
                        db.del_order(content); //حذف القائمة من قاعدة البيانات
                        item.removeSubItem(item); //حذف القائمة من الActivity

                    }else if (text_type.equals("وجبة")){ //في حال حذف وجبة

                        db = new db_manager(add_meal.this);
                        db.del_item(tag); //حذف الوجبة من قاعدة البيانات
                        toast(getString(R.string.deleted), true);
                        finish();
                        startActivity(getIntent()); //ايقاف الActivity وبدأها مجددا
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel(); //الغاء عملة الحذف
                    }
                })
                .create()
                .show();
    }

    //دالة للحصول على المسار الحقيقي للصورة
    public String getRealPathFromURI(Uri contentUri) {

        //يرجع null في حال كان المسار القادم فارغ
        if (contentUri == null)
            return null;
        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }



    //دالة لحل مشاكل الExpandingitem
    //دالة لوضع اللون المختار للExpandingitem
    public void set_color (ExpandingItem item, int color){

        ((GradientDrawable)item.findViewById(R.id.icon_indicator_top).getBackground().mutate()).setColor(color);
        ((GradientDrawable)item.findViewById(R.id.icon_indicator_bottom).getBackground().mutate()).setColor(color);
        item.findViewById(R.id.icon_indicator_middle).setBackgroundColor(color);

    }

    //دالة لإظهار Dialog عند اضافة قائمة او عنصر
    void showInsertDialog(boolean is_meal, int tag) {

        AlertDialog.Builder alertdialog = new AlertDialog.Builder(add_meal.this);
        custom = LayoutInflater.from(add_meal.this).inflate(R.layout.meal_add, null);
        alertdialog.setView(custom);
        AlertDialog custom2 = alertdialog.create();
        custom2.show(); //اظهار الdialog
        color_picker = custom.findViewById(R.id.color_picker);
        image_import_layout = custom.findViewById(R.id.image_import_layout);

        //التحقق من اضافة قائمة او وجبة
        if (!is_meal){

            text_type = getString(R.string.order);
            image_import_layout.setVisibility(View.GONE); //اخفار خيار اختيار الصورة

        }else{

            text_type = getString(R.string.meal);
            color_picker.setVisibility(View.GONE); // اخفار خيار اختيار اللون

        }

        input.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        cancel = custom.findViewById(R.id.cancel1);
        save = custom.findViewById(R.id.save1);
        image_import = custom.findViewById(R.id.image_import);
        text_import = custom.findViewById(R.id.text_import);
        text_title = custom.findViewById(R.id.text_title);
        text_layout = custom.findViewById(R.id.text_layout);
        meal_name = custom.findViewById(R.id.meal_name);
        color_image = custom.findViewById(R.id.color_import);
        text_color_import = custom.findViewById(R.id.text_color_import);

        //text_type = قائمة \ وجبة
        text_title.setText(getString(R.string.add) + text_type);
        text_layout.setHint(getString(R.string.name_is) + text_type);
        custom2.setCanceledOnTouchOutside(false); //عدم الغاء الdialog عند الضغط خارجة


        //عند الضغط على الغاء
        cancel.setOnClickListener(view -> custom2.cancel());

        //عند الضغط على حفظ
        save.setOnClickListener(view -> {
            byte[] image;

            String name = String.valueOf(meal_name.getText());
            String image_name = String.valueOf(text_import.getText());

                if (!is_meal){

                    content = new content_manager(true, 0, name, select_color);
                    db = new db_manager(add_meal.this);
                    db.add_order(content);


                }else {

                    if (getRealPathFromURI(uri) != null){

                        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        image = stream.toByteArray();

                    }else {
                        image = null;
                    }

                    content_manager content_item = new content_manager(0, tag, name, image_name, image);
                    db.add_item(content_item);

                }

                toast(getString(R.string.added_the) + text_type, true);
                //اعادة تشغيل الActivity
                custom2.cancel();
                finish();
                startActivity(getIntent());

        });

        //عند الغاء الdialog
        custom2.setOnCancelListener(dialogInterface -> {
            add_meal.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //اخفاء الكيبورد
        });
    }

    //دالة لعرض Toast
    void toast(String  text, Boolean isShort){
        if (isShort)
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    void ads(){

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

}