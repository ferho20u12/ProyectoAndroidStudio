package com.example.proyectoservicio;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageCarousel carousel;
    private List<CarouselItem> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        carousel = findViewById(R.id.carousel);
        carousel.registerLifecycle(getLifecycle());
        list = new ArrayList<>();
        CargarSlider();
    }
    public void CallWindowCamara(View view) {
        Intent intent = new Intent(this, CamaraOpenCV.class);
        intent.putExtra("contCirculos", carousel.getId());
        //Toast.makeText(this,""+carousel.,Toast.LENGTH_LONG).show();
        startActivity(intent);
    }
    private void CargarSlider(){
        //------------------------0
        list.add(
                new CarouselItem(
                        R.drawable.medidorwestinghouse,
                        "Medidor Westinghouse"
                )
        );
        carousel.setData(list);
    }
}