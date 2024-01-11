package com.android.playlistssync;

import java.util.Date;

public class RequestCodeGenerator {

    public RequestCodeGenerator() {}

    public int getCode() {
        long timestamp = new Date().getTime();
        return Math.abs( (int) timestamp % 100000 );
    }

}