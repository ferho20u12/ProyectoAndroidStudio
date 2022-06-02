package com.example.proyectoservicio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class CamaraOpenCV extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private Intent intent;
    private Mat mRgba,mRgba2,mGray;
    private  int heightScreen,widthScreen;
    private List<Circulo>circulos;
    private int side;
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    Mat hierarchy;
    //----------------
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara_open_cv);
        intent = new Intent(CamaraOpenCV.this, ImageClass.class);
        cameraBridgeViewBase = findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(View.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        circulos = new ArrayList<>();
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
        baseLoaderCallback = new BaseLoaderCallback(this){
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                if (status == BaseLoaderCallback.SUCCESS) {
                    cameraBridgeViewBase.enableView();
                } else {
                    super.onManagerConnected(status);
                }
            }
        };
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8SC4);
        mRgba2 = new Mat(height,width, CvType.CV_8SC4);
        mGray = new Mat(height,width,CvType.CV_8UC1);
        hierarchy  = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mRgba2.release();
        mGray.release();
    }
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        CorregirCamara(mRgba);
        CorregirCamara(mGray);
        mRgba2 = mRgba.clone();
        heightScreen= mRgba.height();
        widthScreen = mRgba.width();
        side = (heightScreen/5)-1;
        Imgproc.rectangle(mRgba,new Point(0,(double) heightScreen/4),new Point(widthScreen,(double) (heightScreen/4+side)+20),new Scalar(0,255,0,1),3);
        return mRgba;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }


    // Funciones propias
    private void CorregirCamara(Mat mat){
        Core.transpose(mat, mat);
        Core.flip(mat, mat, 1);
    }
    private Mat RecortarRectangulo(Mat mat,Point coordenadas,Point rectangulo ){
        Rect rectCrop;
        Mat copy = mat.clone(),matcopy;
        if(rectangulo.x > mat.width()){
            double aux = rectangulo.x - mat.width();
            rectangulo.x = rectangulo.x-aux;
        }
        if(rectangulo.y > mat.width()){
            double aux = rectangulo.y - mat.width();
            rectangulo.x = rectangulo.y-aux;
        }
        rectCrop = new Rect(coordenadas,rectangulo);
        matcopy = copy.submat(rectCrop.clone()).clone();
        return matcopy;
    }
    private void DeteccionDeCiculos(Mat mat){
        Mat input = mat.clone();
        Mat circles = new Mat();
        Imgproc.blur(input, input, new Size(7, 7), new Point(2, 2));
        Imgproc.HoughCircles(input, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 100, 90, 0, 1000);
        if (circles.cols() > 0) {
            for (int x=0; x < Math.min(circles.cols(), 5); x++ ) {
                double[] circleVec = circles.get(0, x);
                if (circleVec == null) {
                    break;
                }
                Point center = new Point((int) circleVec[0], (int) circleVec[1]);
                int radius = (int) circleVec[2];
                Imgproc.circle(input, center, radius, new Scalar(255, 255, 255), 2);

                Circulo circulo = new Circulo(center,radius);
                circulos.add(circulo);
            }
        }
    }
    private void BurbujaMejorada(){
        Circulo AUX;
        int j;
        int bandera=1;
        for(int i = 0;i<(circulos.size()-1)&&bandera==1;i++)
        {
            bandera=0;
            for(j=0;j<circulos.size()-i-1;j++)
            {
                if(circulos.get(j).getCenter().x>circulos.get(j+1).getCenter().x)
                {
                    bandera=1;
                    AUX= new Circulo(circulos.get(j).getCenter(),circulos.get(j).getRadius());
                    circulos.set(j,circulos.get(j+1));
                    circulos.set(j+1,AUX);
                }
            }
        }
    }
    public void CallWindowImageDetection(View view) {
        Mat image_outputCrop;
        circulos.clear();
        image_outputCrop = RecortarRectangulo(mGray,new Point(0,(double) heightScreen/4),new Point(widthScreen,(double) (heightScreen/4+side)+20));
        DeteccionDeCiculos(image_outputCrop);
        if(circulos.size() == 5 ){
            BurbujaMejorada();
            List<Mat>mats =new ArrayList<>();
            for(Circulo circulo:circulos){
                double x,y;
                int radius;
                radius = circulo.getRadius();
                x= circulo.getCenter().x-radius;
                y= circulo.getCenter().y-radius;
                if((x < image_outputCrop.width() && x > 0) &&( y < image_outputCrop.height() && y > 0) ){
                    Mat mat = RecortarRectangulo(image_outputCrop,new Point(x,y),new Point(x+(radius*2),y+(radius*2)));
                    mats.add(mat);
                }
                else{
                    Toast.makeText(this,"Se detectaron los circulos pero debe centrarse mas la camara ",Toast.LENGTH_LONG).show();
                    return;
                }
            }
            intent.putExtra( "image_outputCrop",image_outputCrop.nativeObj);
            intent.putExtra("contCirculos",5);
            if(!mats.isEmpty()){
                for(int i = 0; i<mats.size();i++){
                    intent.putExtra( "image_output"+i,mats.get(i).nativeObj);
                }
            }
            startActivity(intent);
        }
        else{
            Toast.makeText(this,"No se detectaron todos los circulos, vuelva intentarlo "+circulos.size(),Toast.LENGTH_LONG).show();
        }
    }
}