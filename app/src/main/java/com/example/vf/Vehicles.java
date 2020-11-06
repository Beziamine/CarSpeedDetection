package com.example.vf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Vehicles {
    private int i,max_age,state, age;
    private double xi,yi;
    int speed;
    private ArrayList<ArrayList<Double>> tracks = new ArrayList<ArrayList<Double>>();

    //private List tracks = new ArrayList();
    private int R,B,G;
    private boolean done;
    private String dir = new String();

    public Vehicles(int i,Double xi,Double yi,int max_age){
        this.i = i;
        this.xi = xi;
        this.yi = yi;
        this.max_age = max_age;
        this.tracks = new ArrayList<ArrayList<Double>>();
        this.R = (int)(Math.random() * 256);
        this.B = (int)(Math.random() * 256);
        this.G = (int)(Math.random() * 256);
        this.done = false;
        this.state = 0;
        this.speed = 0;
        this.age = 0;

    }

    // Getter
    public List<Integer> getRGB() {
        List<Integer> l = new ArrayList<Integer>();
        l.add(this.R);
        l.add(this.G);
        l.add(this.B);
        return l;
    }

    public List getTracks(){
        return this.tracks;
    };

    public Double getXi(){
        return this.xi;
    }

    public Double getYi(){
        return this.yi;
    }

    public int getState(){
        return this.state;
    }

    public int getId(){
        return this.i;
    }

    public void setDone(){
        this.done = true;
    }

    public int getSpeed(){ return this.speed;}

    public void updateCoords(int xn, int yn){
        this.age = 0;
        ArrayList<Double> l = new ArrayList<Double>();
        l.add(this.xi);
        l.add(this.yi);
        this.tracks.add(l);
        this.xi = xn;
        this.yi = yn;
    }

    public boolean going_Up(int mid_start,int mid_end){
      if(this.tracks.size()>=2){
          if (this.state ==0){
              if((this.tracks.get(tracks.size()-1).get(1)<mid_end) && (this.tracks.get(tracks.size()-2).get(1) >= mid_end)){
                state = 1;
                this.dir = "up";
                return true;
              }
              else
                  return false;
          }
          else
              return false;
      }
      else
          return false ;
    };

    public boolean going_Down(int mid_start,int mid_end){
        if(this.tracks.size()>=2){
            if (this.state ==0){
                if((this.tracks.get(tracks.size()-1).get(1)> mid_start) && (this.tracks.get(tracks.size()-2).get(1) <= mid_start)){
                    state = 1;
                    this.dir = "down";
                    return true;
                }
                else
                    return false;
            }
            else
                return false;
        }
        else
            return false ;
    };

    public boolean age_one(){
        this.age +=1;
        if(this.age > this.max_age)
            this.done=true;
        return true;
    }


    public void calcul_Speed(int ppm , int fps){
        double x1,x2,x3,x4;
        x1 = this.tracks.get(this.tracks.size()-1).get(1);
        x2 = this.tracks.get(this.tracks.size()-2).get(1);
        x3 = this.tracks.get(this.tracks.size()-1).get(0);
        x4 = this.tracks.get(this.tracks.size()-2).get(0);
        double dpixel = Math.sqrt(Math.pow(x1-x2,2) + Math.pow(x3-x4,2));
        double dmeter = dpixel / ppm;
        double s = dmeter * fps * 3.6;
        this.speed = (int)( this.speed +s )/2;
    }
}
