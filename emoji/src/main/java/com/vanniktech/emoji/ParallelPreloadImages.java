package com.vanniktech.emoji;

import android.content.Context;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.emoji.EmojiCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelPreloadImages {

    public static void main(final Context context, final EmojiCategory[] emojiCategories) {
        // Assume you have an array of integers
//        int[] array = {1, 2, 3, 4, 5};

        // Calculate an optimal thread pool size
        int threadPoolSize = Runtime.getRuntime().availableProcessors();

        // Create a custom executor to run tasks in parallel
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // Create a list of Future instances
        List<Future<Void>> futures = new ArrayList<>();

        // Iterate through the array and use ExecutorService.submit to run the function for each element
        for ( EmojiCategory emojiCategory: emojiCategories){
            for (Emoji emoji:emojiCategory.getEmojis()){
                final Emoji emojiF = emoji;
                Callable<Void> callable = () -> {
                    // Your function to process each element
                    processElement(emojiF, context);
                    return null;
                };
                futures.add(executor.submit(callable));
            }
        }


        // Wait for all tasks to complete
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // All elements have been processed in parallel
        System.out.println("All elements processed.");

        // Shut down the executor service
        executor.shutdown();
    }

    private static void processElement(Emoji emoji, Context context) {
        emoji.getDrawable(context);
    }

}