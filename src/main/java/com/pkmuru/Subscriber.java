package com.pkmuru;

import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class Subscriber {

    final static Logger LOG = Logger.getLogger(Subscriber.class);


    private List<UserDto> getAllUsers() {
        List<UserDto> userDtos = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            userDtos.add(new UserDto("user" + i, "jid" + i + "@test.com"));

        }
        return userDtos;
    }


    RestClient restClient = new RestClient();


    ExecutorService profileExecutorService = Executors.newFixedThreadPool(5);

    ExecutorService interestExecutorService = Executors.newFixedThreadPool(5);

    ExecutorService subscriberExecutorService = Executors.newFixedThreadPool(5);


    CompletableFuture<Pair<UserDto, Collection<UserProfile>>> getProfile(UserDto userDto) {
        CompletableFuture<Pair<UserDto, Collection<UserProfile>>> future = new CompletableFuture<>();

        profileExecutorService.submit(() -> {
            try {

                LOG.info("getProfile... started..." + userDto.userName);
                Collection<UserProfile> userProfiles = restClient.getProfile(userDto.userName);
                future.complete(new Pair<>(userDto, userProfiles));
            } catch (Exception e) {
                LOG.error("getProfile... failed--->" + userDto.userName);
                future.completeExceptionally(e);
                // throw  e;
            }
        });

        future.exceptionally(throwable -> {
            LOG.error("getProfile exception... ------->" + userDto.userName + throwable.getMessage());
            return new Pair<>(userDto, null);
        });

        return future;

    }

    CompletableFuture<Pair<UserDto, Collection<Interest>>> getInterests(Pair<UserDto, Collection<UserProfile>> profile) {

        CompletableFuture<Pair<UserDto, Collection<Interest>>> future = new CompletableFuture<>();

        interestExecutorService.submit(() -> {
            try {
                LOG.info("UserProfile is null for " + profile);

                LOG.info("getInterests... started..." + profile.getKey().userName);

                Collection<Interest> interests = restClient.getInterests(profile.getKey().userName);
                future.complete(new Pair<>(profile.getKey(), interests));
            } catch (Exception e) {
                LOG.error(e);
                future.completeExceptionally(e);
            }
        });

        return future;

    }

    public CompletableFuture<String> putSubscribtion(Pair<UserDto, Collection<Interest>> interests) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        subscriberExecutorService.submit(() -> {
            try {
                LOG.info("subscribing...." + interests.getKey().userName);
                restClient.subscribeInterest(interests.getKey().userName);
                completableFuture.complete("Interest completed....." + interests.getKey().userName);
            } catch (InterruptedException e) {
                completableFuture.completeExceptionally(e);
            }

        });

        return completableFuture;
    }


    public void SubscribeAll() {

        List<UserDto> allUsers = getAllUsers();

        List<CompletableFuture<String>> allFutures = new ArrayList<>();

        allUsers.stream().forEach(userDto -> {


            CompletableFuture<String> future = getProfile(userDto)
                    .thenComposeAsync(this::getInterests)
                    .thenComposeAsync(this::putSubscribtion).whenComplete((s, throwable) -> {
                        if (s == null) {
                            LOG.error("Subscription failed....." + throwable);
                        } else {
                            LOG.info("Sucess.... " + s);
                        }
                    });
            allFutures.add(future);

        });

        CompletableFuture[] futureResultArray = allFutures.toArray(new CompletableFuture[allFutures.size()]);

        try {
            CompletableFuture.allOf(futureResultArray).get();
        } catch (Exception e) {
            LOG.info("Process started with few error");
        }


        profileExecutorService.shutdown();
        interestExecutorService.shutdown();
        subscriberExecutorService.shutdown();

        LOG.info("SubscribeAll completed......");


    }
}
