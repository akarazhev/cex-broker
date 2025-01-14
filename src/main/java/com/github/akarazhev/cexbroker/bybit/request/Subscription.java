package com.github.akarazhev.cexbroker.bybit.request;

public record Subscription(String op, String[] args) {

    public String toJson() {
        final StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"op\":\"").append(op).append("\",");
        json.append("\"args\":[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append("\"").append(args[i]).append("\"");
        }

        json.append("]");
        json.append("}");
        return json.toString();
    }
}
