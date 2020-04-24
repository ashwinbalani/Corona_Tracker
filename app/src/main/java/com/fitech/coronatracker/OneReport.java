package com.fitech.coronatracker;

import androidx.annotation.NonNull;

public class OneReport implements Comparable<OneReport> {

    public int id;
    public String image;
    public String newsLocation;
    public String newsDate;
    public String distance;

    public OneReport(int id, String image, String newsLocation, String newsDate,String distance) {
        this.id = id;
        this.image = image;
        this.newsLocation = newsLocation;
        this.newsDate = newsDate;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNewsDate() {
        return newsDate;
    }

    public void setNewsDate(String newsDate) {
        this.newsDate = newsDate;
    }

    public String getNewsLocation() {
        return newsLocation;
    }

    public void setNewsLocation(String newsLocation) {
        this.newsLocation = newsLocation;
    }

    public double getDistance() {
        try {
            return Double.parseDouble(distance);
        } catch(Exception ex) {
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        try {
            OneReport newsItem = (OneReport)obj;
            if(newsItem.image.equals(this.image))
                return true;
            else
                return false;
        } catch (Exception ex) {

        }
        return super.equals(obj);
    }

    //For sorting the ids
    @Override
    public int compareTo(@NonNull OneReport newsItem) {
        try {
            int new_id = newsItem.id;
            return new_id - this.id;
        } catch (Exception ex) {
            return 0;
        }

    }
}
