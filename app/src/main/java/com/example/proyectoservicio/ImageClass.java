package com.example.proyectoservicio;


import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoservicio.ml.Model;
import com.example.proyectoservicio.ml.Model2;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ImageClass extends AppCompatActivity {
    private List<Mat>mats;
    private List<Bitmap>bitmaps;
    private List<String>identificadores;
    private int cont;
    private TextView textView;
    private ViewFlipper imageFlipper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_class);
        imageFlipper = findViewById( R.id.image_flipper );
        textView = findViewById( R.id.textView);
        bitmaps = new ArrayList<>();
        mats = new ArrayList<>();
        cont=0;
        identificadores = new ArrayList<>();
        if(OpenCVLoader.initDebug())
            Toast.makeText(this,"Cargado OpenCv",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this,"Algo salio mal al cargar Open CV",Toast.LENGTH_SHORT).show();
        Obtencion_Imagenes();
        CargarSlider();
        IdentificarNumeros();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LimpiarVariables();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //---------------------------------------- Conversion

    private Bitmap Mat_to_Bitmap(Mat mat){
        Mat matAux = mat.clone();
        Bitmap bitmap = Bitmap.createBitmap(matAux.cols(), matAux.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matAux,bitmap);
        return bitmap;
    }
    //--------------------------------------------------Obtencion de numeros
    private  void Obtencion_Imagenes(){
        Bundle parametros = this.getIntent().getExtras();
        if(parametros !=null){
            mats.add(new Mat(parametros.getLong("image_outputCrop")));
            int contCirculos = parametros.getInt("contCirculos");
            for(int i = 0; i< contCirculos; i++){
                mats.add(new Mat(parametros.getLong("image_output"+i)));
            }
            if(mats.get(0) != null){
                for (Mat mat:mats) {
                    bitmaps.add(Mat_to_Bitmap(mat));
                }
            }
            else
                Toast.makeText(this,"Error no existe imagen de origen",Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(this,"Error al cargar Bunble",Toast.LENGTH_LONG).show();
    }
    private void IdentificarNumeros(){
        textView.setText("");
        for(int i=1;i<bitmaps.size();i++){
            if(i%2==0){
                classifyImage2(bitmaps.get(i));
            }
            else{
                classifyImage(bitmaps.get(i));
            }

        }
        String str = textView.getText()+" Kw";
        textView.setText(str);
    }
    private void classifyImage(Bitmap image)
    {
        try {
            int imageSize = 224;
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize,false);
            Model model = Model.newInstance(getApplicationContext());
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; ++i)
            {
                for (int j = 0; j < imageSize; ++j) {
                    int val = intValues[pixel++]; //RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * 1.f);
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * 1.f);
                    byteBuffer.putFloat((val & 0xFF) * 1.f);
                }
            }
            ArrayList<String>classes = new ArrayList<>();
            for(int i=0;i<10;i++){
                classes.add(""+i);
            }
            inputFeature0.loadBuffer(byteBuffer);
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float [] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0 ; i < confidences.length ; ++i)
            {
                if (confidences[i] > maxConfidence)
                {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            identificadores.add(classes.get(maxPos));
            String str = textView.getText()+classes.get(maxPos);
            textView.setText(str);
            model.close();
        } catch (IOException e) {
            Toast.makeText(this, "No jala", Toast.LENGTH_SHORT).show();
        }
    }
    private void classifyImage2(Bitmap image)
    {
        try {
            int imageSize = 224;
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize,false);
            Model2 model = Model2.newInstance(getApplicationContext());
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; ++i)
            {
                for (int j = 0; j < imageSize; ++j) {
                    int val = intValues[pixel++]; //RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * 1.f);
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * 1.f);
                    byteBuffer.putFloat((val & 0xFF) * 1.f);
                }
            }
            ArrayList<String>classes = new ArrayList<>();
            for(int i=0;i<10;i++){
                classes.add(""+i);
            }
            inputFeature0.loadBuffer(byteBuffer);
            Model2.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float [] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0 ; i < confidences.length ; ++i)
            {
                if (confidences[i] > maxConfidence)
                {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            identificadores.add(classes.get(maxPos));
            String str = textView.getText()+classes.get(maxPos);
            textView.setText(str);
            model.close();
        } catch (IOException e) {
            Toast.makeText(this, "No jala", Toast.LENGTH_SHORT).show();
        }
    }

    //---------------------------Slider
    private void CargarSlider(){
        for (Bitmap bmp:bitmaps) {
            ImageView image = new ImageView ( getApplicationContext() );
            image.setImageBitmap(bmp);
            imageFlipper.addView(image);
        }
    }
    public void CargarSiguiente(View view){
        if(cont<identificadores.size()){
            imageFlipper.showNext();
            cont++;
            if(cont != 0){
                Toast.makeText(this,"Se leyo "+identificadores.get(cont-1),Toast.LENGTH_LONG).show();
            }

        }
    }
    public void CargarPrevio(View view){
        if(cont>0){
            cont--;
            imageFlipper.showPrevious();
        }
    }

    //---------------------------Limpiar Variables
    private void LimpiarVariables(){
        for (Mat mat:mats)
        {
            mat.release();
        }
        mats.clear();
        bitmaps.clear();
        identificadores.clear();
    }
}