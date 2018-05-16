package com.pkmuru;

import java.util.ArrayList;
import java.util.Collection;

public class RestClient {

    public Collection<UserProfile> getProfile(String userName) throws Exception {
        Thread.sleep(1000);

        Collection<UserProfile> userProfiles = new ArrayList<>();
        userProfiles.add(new UserProfile("UserProfile...." + userName));

        if(userName.contains("0") || userName.contains("2")){
            throw new Exception("Failed...." + userName);
        }

        return userProfiles;


     }


    public Collection<Interest> getInterests(String userName) throws InterruptedException {
        Collection<Interest> interests=new ArrayList<>();

        Thread.sleep(1000);

        interests.add(new Interest("Interest...." + userName));

        return  interests;

     }

    public String subscribeInterest(String userName) throws InterruptedException {
        Thread.sleep(4000);
        return  "Subscribe..completed..." + userName;
    }
}
