package com.example.a_say_it;

public class JsonResponse {
    private int result;
    private String return_type;
    private ReturnObject return_object;

    public static class ReturnObject {
        private String recognized;
        private String score;

        // Getters
        public String getRecognized() {
            return recognized;
        }

        public String getScore() {
            return score;
        }
    }

    // Getters
    public int getResult() {
        return result;
    }

    public String getReturn_type() {
        return return_type;
    }

    public ReturnObject getReturn_object() {
        return return_object;
    }
}
