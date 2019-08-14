package me.mebubi.myalbum.utility;

public class PostTimeFormat {

    private PostTimeFormat() { }

    // returns a string of how long ago the content was posted
    public static String getTimeString(long postTime) {

        long currentTime = System.currentTimeMillis();
        long timePassedInSeconds = (currentTime - postTime) / 1000;

        long tempTimePassed = 0;
        if(timePassedInSeconds < 60) { // > 60 seconds
            return "Just now";
        }
        else if (timePassedInSeconds < 60*60) { // < 60 minutes
            tempTimePassed = (timePassedInSeconds/60);
            if(tempTimePassed == 1)
                return tempTimePassed + " minute ago";
            return tempTimePassed + " minutes ago";
        }
        else if (timePassedInSeconds < 60*60*24) { // < 24 hours
            tempTimePassed = (timePassedInSeconds/(60*60));
            if(tempTimePassed == 1)
                return tempTimePassed + " hour ago";
            return tempTimePassed + " hours ago";
        }
        else if (timePassedInSeconds < 60*60*24*30) { // < 30 days
            tempTimePassed = (timePassedInSeconds/(60*60*24));
            if(tempTimePassed == 1)
                return tempTimePassed + " day ago";
            return tempTimePassed + " days ago";
        }
        else if (timePassedInSeconds < 60*60*24*365) { // < 365 days
            tempTimePassed = (timePassedInSeconds/(60*60*24*30));
            if(tempTimePassed == 1)
                return tempTimePassed + " month ago";
            return tempTimePassed + " months ago";
        }
        else { // more than a year has passed
            tempTimePassed = (timePassedInSeconds/(60*60*24*365));
            if(tempTimePassed == 1)
                return tempTimePassed + " year ago";
            return tempTimePassed + " years ago";
        }

    }

    // test method for method above
    public static String getTimeStringTest(long timePassedInSeconds) {

        long tempTimePassed = 0;
        if(timePassedInSeconds < 60) { // > 60 seconds
            return "Posted just now";
        }
        else if (timePassedInSeconds < 60*60) { // < 60 minutes
            tempTimePassed = (timePassedInSeconds/60);
            if(tempTimePassed == 1)
                return "Posted " + tempTimePassed + " minute ago";
            return "Posted " + tempTimePassed + " minutes ago";
        }
        else if (timePassedInSeconds < 60*60*24) { // < 24 hours
            tempTimePassed = (timePassedInSeconds/(60*60));
            if(tempTimePassed == 1)
                return "Posted " + tempTimePassed + " hour ago";
            return "Posted " + tempTimePassed + " hours ago";
        }
        else if (timePassedInSeconds < 60*60*24*30) { // < 30 days
            tempTimePassed = (timePassedInSeconds/(60*60*24));
            if(tempTimePassed == 1)
                return "Posted " + tempTimePassed + " day ago";
            return "Posted " + tempTimePassed + " days ago";
        }
        else if (timePassedInSeconds < 60*60*24*365) { // < 365 days
            tempTimePassed = (timePassedInSeconds/(60*60*24*30));
            if(tempTimePassed == 1)
                return "Posted " + tempTimePassed + " month ago";
            return "Posted " + tempTimePassed + " months ago";
        }
        else { // more than a year has passed
            tempTimePassed = (timePassedInSeconds/(60*60*24*365));
            if(tempTimePassed == 1)
                return "Posted " + tempTimePassed + " year ago";
            return "Posted " + tempTimePassed + " years ago";
        }

    }

}