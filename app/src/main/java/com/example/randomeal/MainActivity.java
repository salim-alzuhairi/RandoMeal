package com.example.randomeal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.slider.Slider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    TextView now, error_text;
    CheckBox lunch_check, dinner_check, breakfast_check;
    Toolbar btm_app;
    View bar, fab, meal_name;
    ImageView image;
    BottomSheetBehavior btmNav;
    CoordinatorLayout coordinator;
    NavigationView nav;
    add_meal add_meal;
    ArrayList name_list;
    ArrayList<byte[]> images;
    Slider slider;
    LinearLayout linear;
    AdView mAdView;


    List B = new ArrayList(Arrays.asList("فول قلابة", "فول جرة", "شكشوكة", "كبدة", "فلافل", "حمص", "مقلقل", "فطيرة زعتر", "معصوب", "شورما"));
    List L = new ArrayList(Arrays.asList("كبسة", "مندي", "مشوي"));
    List D = new ArrayList(B);
    List A = new ArrayList();

    Random r = new Random();
    ChipGroup chip_group;

    int old_item, item = -1;

    boolean if_breakfast = true, if_lunch = true, if_dinner = true;

    char[] enter = new char[]{'a', 'a', 'a'};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        now = findViewById(R.id.now);
        error_text = findViewById(R.id.error_text);
        coordinator = findViewById(R.id.coordinator2);
        bar = coordinator.findViewById(R.id.btm_drawer_fab2);
        btm_app = findViewById(R.id.btm_app_bar_fab2);
        nav = findViewById(R.id.nav_view_fab2);
        breakfast_check = findViewById(R.id.breakfast_check);
        lunch_check = findViewById(R.id.lunch_check);
        dinner_check = findViewById(R.id.dinner_check);
        meal_name = findViewById(R.id.meal_name);
        fab = findViewById(R.id.fab2);
        image = findViewById(R.id.image);
        mAdView = findViewById(R.id.adView);

        ads();

        chip_group = new ChipGroup(this);
        linear = new LinearLayout(this);
        slider = new Slider(this);

        slider.setValue(1); slider.setStepSize(1); slider.setValueFrom(1); slider.setValueTo(3);
        slider.setThumbElevation(2); slider.setThumbRadius(16); slider.setTrackHeight(16);

        TextView app_meal = new TextView(this);
        TextView both_meal = new TextView(this);
        TextView my_meal = new TextView(this);

        app_meal.setText(R.string.app_meal); app_meal.setPadding(0, 50, 20, 0);
        my_meal.setText(R.string.my_meal); my_meal.setGravity(Gravity.CENTER); my_meal.setPadding(0, 50, 0, 0);
        both_meal.setText(R.string.both); both_meal.setGravity(Gravity.END); both_meal.setPadding(20, 50, 0, 0);

        FrameLayout frame = new FrameLayout(this);
        frame.addView(app_meal);
        frame.addView(my_meal);
        frame.addView(both_meal);

        TextView t_title = new TextView(this);
        t_title.setText(getResources().getText(R.string.my_meal) + ":");
        t_title.setTextSize(25);
        t_title.setTypeface(null, Typeface.BOLD);

        int[] attrs = { android.R.attr.listDivider };
        TypedArray ta = getApplicationContext().obtainStyledAttributes(attrs);
        Drawable divider = ta.getDrawable(0);
        ta.recycle();

        ImageView imgv = new ImageView(this);
        imgv.setImageDrawable(divider);

        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setGravity(Gravity.START);
        linear.addView(frame);
        linear.addView(slider);
        linear.addView(imgv);
        linear.addView(t_title);

        nav.addView(linear);

        TextView t = new TextView(this);
        t.setText(R.string.there_are_no_meals_for_you);
        t.setTextColor(Color.RED);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextSize(20);
        t.setGravity(Gravity.CENTER);
        t.setVisibility(View.VISIBLE);

        nav.setPadding(5,5,5,5);
        linear.addView(t);



        btmNav = BottomSheetBehavior.from(bar);
        btmNav.setState(BottomSheetBehavior.STATE_HIDDEN);
        btm_app.setNavigationOnClickListener(view -> {
            nav.setVisibility(View.VISIBLE);
            btmNav.setState(BottomSheetBehavior.STATE_EXPANDED);
            btmNav.setSkipCollapsed(true);

        });

        btm_app.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()) {

                case R.id.add_meal:
                    Intent intent2 = new Intent(MainActivity.this, add_meal.class);
                    startActivity(intent2);
                    break;

                case R.id.about:
                    Intent intent = new Intent(MainActivity.this, about.class);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        fab.setOnClickListener(view -> {

            now.setText("");
            image.setImageDrawable(getResources().getDrawable(R.drawable.app_icon));

            for (int i = 0; i < 10; i++) {

                switch ((int) slider.getValue()) {

                    case 1:
                        app_meal();
                        break;
                    case 2:
                        my_meal();
                        break;
                    case 3:
                        Boolean b = r.nextBoolean();
                        if (b)
                            app_meal();
                        else
                            my_meal();
                        break;

                }

            }
        });

        if (getIntent().hasExtra("bundle")){

            Bundle bundle = getIntent().getBundleExtra("bundle");
            name_list = (ArrayList) bundle.getSerializable("name");
            ArrayList<Integer> image_list = (ArrayList) bundle.getSerializable("image");

            add_meal = new add_meal();

            chip_group.setSelectionRequired(false);
            chip_group.setSingleSelection(false);

            images = new ArrayList();

            for (int i = 0; i < name_list.size(); i++) {

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip, chip_group, false);
                chip.setText((String) name_list.get(i));
                chip.setCloseIconVisible(true);
                chip.setChecked(true);
                chip.setTag(i);
                chip.setOnCloseIconClickListener(view -> chip_group.removeView(chip));

                chip_group.addView(chip);

                db_manager db = new db_manager(MainActivity.this);
                byte[] img = db.get_image(image_list.get(i));
                images.add(img);

            }
            linear.addView(chip_group);

            if (chip_group.getChildCount() >= 1)
                t.setVisibility(View.GONE);
            else
                t.setVisibility(View.VISIBLE);


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar1, menu);

        return true;
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        if (btmNav.getState() == BottomSheetBehavior.STATE_EXPANDED){

            btmNav.setState(BottomSheetBehavior.STATE_HIDDEN);

        }else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            toast(getString(R.string.press_again_to_exit));

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);

        }
    }

    void app_meal(){

        error_text.setText(R.string.Please_make_sure_to_choose_the_meal_time);

        if (breakfast_check.isChecked() && if_breakfast) {
            for (Object add : B)
                A.add(add);
            if_breakfast = false;
            enter[0] = 'b';
        } else if (!breakfast_check.isChecked() && enter[0] == 'b') {
            for (Object delet : B)
                A.remove(delet);
            if_breakfast = true;
            enter[0] = 'a';
        }


        if (dinner_check.isChecked() && if_dinner) {
            for (Object add : D)
                A.add(add);
            if_dinner = false;
            enter[1] = 'd';
        } else if (!dinner_check.isChecked() && enter[1] == 'd') {
            for (Object delet : D)
                A.remove(delet);
            if_dinner = true;
            enter[1] = 'a';
        }


        if (lunch_check.isChecked() && if_lunch) {
            for (Object add : L)
                A.add(add);
            if_lunch = false;
            enter[2] = 'l';
        } else if (!lunch_check.isChecked() && enter[2] == 'l') {
            for (Object delet : L)
                A.remove(delet);
            if_lunch = true;
            enter[2] = 'a';
        }

        ran();

        try {
            while (A.get(old_item).equals(now.getText())) {
                ran();
            }
        } catch (ArrayIndexOutOfBoundsException ex) {

        } catch (IndexOutOfBoundsException ex2) {

        }
    }

    void my_meal(){

        error_text.setText(R.string.you_have_not_added_any_of_your_meals);

        if (chip_group.getChildCount() >= 1){

            ArrayList<Chip> array_list = new ArrayList();

            for (int i = 0; i < chip_group.getChildCount(); i++) {
                Chip chip =(Chip) chip_group.getChildAt(i);

                if (chip.isChecked()){

                    array_list.add(chip);

                }
            }

            if (array_list.size() >= 1) {

                error_text.setText("");
                int int_name = r.nextInt(array_list.size());
                now.setText(array_list.get(int_name).getText());
                byte[] img = images.get((int) array_list.get(int_name).getTag());

                if (img != null)
                    image.setImageBitmap(BitmapFactory.decodeByteArray(img, 0, img.length));
                else
                    image.setImageDrawable(getResources().getDrawable(R.drawable.app_icon));
            }
        }
    }

    void ran() {

        old_item = item;
        try {
            item = r.nextInt(A.size());
            now.setText((String) A.get(item));
            image.setImageDrawable(getResources().getDrawable(R.drawable.app_icon));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        if (now.getText() != ""){

            error_text.setText("");

        }

    }

    void toast(String  text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    void ads(){

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

}