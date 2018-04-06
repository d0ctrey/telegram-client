package org.telegram;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by s_tayari on 12/20/2017.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }
}
