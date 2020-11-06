package com.example.vf;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static java.lang.String.valueOf;
import static org.opencv.core.Core.FONT_HERSHEY_SCRIPT_SIMPLEX;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2  {

    private static final String TAG = "MainActivity";
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    private Mat mRgba;
    BackgroundSubtractorMOG2 mog2;
    private ArrayList<Vehicles> cars = new ArrayList<Vehicles>();

    int seuil1,seuil2,seuil3 ;
    int pid = 1 ,fileIdx = 0 , currentMinute,image_per_file = 0;
    int max_p_age = 5;
    int speedMax = 10;

    String user= "mail4test20202@gmail.com";
    String password = "azerty123+" ;

    public String sb, bd, rp;
    int startPros = 0;
    Boolean minuteChenged = false;

    private ArrayList<GMailSender> sendersList = new ArrayList<GMailSender>();

    GMailSender sender;

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        for( int i = 0; i <60 ; i++){
            sendersList.add(i,new GMailSender(user, password));
        }

        Intent intent = getIntent();
        rp = intent.getStringExtra("mail");
        speedMax = Integer.parseInt( intent.getStringExtra("speed") ) ;

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch(status){

                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mog2 = Video.createBackgroundSubtractorMOG2();
        seuil1 = (int)(mRgba.height() * 0.298);
        seuil2 =(int) (mRgba.height() * 0.4765);
        seuil3 =(int)(mRgba.height()*0.6718);

        Calendar d = Calendar.getInstance();
        currentMinute = d.getTime().getMinutes() ;
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There's a problem, yo!", Toast.LENGTH_SHORT).show();
        }

        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }




    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        Mat fgMask = new Mat();
        List<MatOfPoint> contour2 = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        double cont_area;
        List<Moments> mu = new ArrayList<Moments>(contour2.size());
        int idx2 =0;

        Calendar d = Calendar.getInstance();
        int LastMinute = d.getTime().getMinutes() - 1;


        ContextWrapper cw1 = new ContextWrapper(getApplicationContext());
        File directory2 = cw1.getDir("imageDir"+ LastMinute, Context.MODE_PRIVATE);

        int thisMinute = d.getTime().getMinutes();
        if (thisMinute > currentMinute){
            minuteChenged = true;
        }
        if (thisMinute == 0 & currentMinute == 59){
            minuteChenged = true;
        }

        if (minuteChenged){
            currentMinute = thisMinute;
            if( numberOfFiles(directory2) != 0){
                    if (startPros == 1){
                    }
                    else {
                        startPros = 1;
                        Integer data[] = {thisMinute-1,null,null};
                        new MyAsyncClass().execute(data);
                        minuteChenged=false;
                    }
            }
            else minuteChenged = false;
        }

        mog2.apply(mRgba, fgMask);

        Imgproc.findContours(fgMask, contour2, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        int line_up = (int)(mRgba.height()*0.1562);
        int line_down = (int)(mRgba.height()*0.625);
        int up_limit=line_up - (int)(mRgba.height()*0.039);
        int down_limit= line_down + (int)(mRgba.height()*0.039);


        for (int contourIdx=0; contourIdx < contour2.size(); contourIdx++)
        {
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f( contour2.get(contourIdx).toArray());


            double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            MatOfPoint points = new MatOfPoint(approxCurve.toArray());

            cont_area = Imgproc.contourArea(contour2.get(contourIdx));

            if(cont_area > 1000){

                Rect rect = Imgproc.boundingRect(points);

                Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 230, 20, 255), 1);

                mu.add(idx2, Imgproc.moments(contour2.get(contourIdx), false));
                Moments p = mu.get(idx2);
                int cx = (int) (p.get_m10() / p.get_m00());
                int cy = (int) (p.get_m01() / p.get_m00());
                idx2++;

                boolean newcar = true;
                int y =-1;
                if( cy < down_limit & cy > up_limit){
                    for(int i=0; i < cars.size();i++) {
                        if (Math.abs(cx - cars.get(i).getXi()) <= rect.width & Math.abs(cy - cars.get(i).getYi()) <= rect.height) {
                            newcar = false;
                            y=i;
                            cars.get(i).updateCoords(cx, cy);

                            if ((cy > line_up) & (cy < line_down)) {
                                if (cx < seuil1) {
                                    if (cars.get(i).getTracks().size() > 2) {
                                        cars.get(i).calcul_Speed(34, 30);
                                    }
                                } else if (cx < seuil2) {
                                    if (cars.get(i).getTracks().size() > 2) {
                                        cars.get(i).calcul_Speed(30, 30);
                                    }
                                } else if (cx < seuil3) {
                                    if (cars.get(i).getTracks().size() > 2) {
                                        cars.get(i).calcul_Speed(33, 30);
                                    }
                                } else {
                                    if (cars.get(i).getTracks().size() > 2) {
                                        cars.get(i).calcul_Speed(26, 30);
                                    }
                                }
                            }
                        }
                    }

                    if (!newcar){
                        int speed = 0;
                        speed = cars.get(y).getSpeed();

                        if (speed > speedMax){

                                Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 230, 20, 255), 1);

                                Imgproc.putText(mRgba, Integer.toString(speed), new Point(rect.x, rect.y), FONT_HERSHEY_SCRIPT_SIMPLEX, 1.0, new Scalar(0, 255, 0), 1);

                                Mat croppedImage = new Mat(mRgba, rect);

                                Bitmap b = convertMatToBitMap(croppedImage);

                                new MyAsyncClass2().execute(b);

                        }
                    }

                    else {
                        Vehicles veh = new Vehicles(pid,(double)cx,(double)cy,max_p_age);
                        cars.add(veh);
                        pid+=1;
                    }
                }
            }
        }

        return mRgba;
    }

    class MyAsyncClass extends AsyncTask<Integer, Void, Void> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            try {

                Calendar dx = Calendar.getInstance();
                int LastMinutex = integers[0];
                ContextWrapper cw1x = new ContextWrapper(getApplicationContext());
                File directory2x = cw1x.getDir("imageDir"+ LastMinutex, Context.MODE_PRIVATE);
                Log.d("before errooor","ha moseeeef");
                sendersList.set(0, new GMailSender(user, password));
                Log.d("after errooor","ha moseeeef");

                sb = "Captures of "+ dx.getTime().getHours() +":" + (LastMinutex);
                bd = "Captures";
                Calendar c2 = Calendar.getInstance();
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = sdf2.format(c2.getTime());

                Log.d("from WORKER Thread","Before add Attachement Thread");

                File[] listoffiles = directory2x.listFiles();

                for (int k=0; k< numberOfFiles(directory2x) ; k++ ) {
                    sendersList.get(0).addAttachment(listoffiles[k].getPath(),k);
                }

                Log.d("from WORKER Thread","after Attachement Thread");
                // Add subject, Body, your mail Id, and receiver mail Id.
                sendersList.get(0).sendMail(sb, bd, user, rp);
                Log.d("send", "done");

                deleteRecursive(directory2x);
                startPros = 0;
            }
            catch (Exception ex) {
                Log.d("exceptionsending", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }

    class MyAsyncClass2 extends AsyncTask<Bitmap, Bitmap, Integer> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            image_per_file++;
        }

        @Override
        protected Integer doInBackground(Bitmap... params) {


            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = sdf.format(c.getTime());
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            fileIdx = c.getTime().getMinutes();
            File directory = cw.getDir("imageDir"+ fileIdx, Context.MODE_PRIVATE);
            // Create imageDir
            File mypath=new File(directory,strDate+ ".jpg");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                params[0].compress(Bitmap.CompressFormat.PNG, 100, fos);

                //new MyAsyncClass().execute();
                //sender.addAttachment(mypath.getPath(),id);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.v("SavingImage","saved tooooooooo");
            return image_per_file;
        }

        @Override
        protected void onPostExecute(Integer image_per_file) {
        }
    }

    private static Bitmap convertMatToBitMap(Mat input){
        Bitmap bmp = null;
        Mat rgb = new Mat();
        Core.rotate(input, rgb, Core.ROTATE_90_CLOCKWISE);
        Imgproc.cvtColor(rgb, rgb, Imgproc.CHAIN_APPROX_NONE);

        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb, bmp);
        }
        catch (CvException e){
            Log.d("Exception",e.getMessage());
        }
        return bmp;
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private Integer numberOfFiles( File file){
        File[] list = file.listFiles();
        int count = 0;
        for (File f: list) {
            String name = f.getName();
            if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".some media extention"))
                count++;
            System.out.println("170 " + count);
        }
        return count;
    }
}