package org.goldenroute.cq.model;

public enum TestModel {
    Review,
    Test;

    public static TestModel fromInteger(int x) {
        switch (x) {
            case 0:
                return Review;
            case 1:
                return Test;
        }
        return null;
    }

    public static int toInteger(TestModel x) {
        switch (x) {
            case Review:
                return 0;
            case Test:
                return 1;
        }
        return -1;
    }
}

