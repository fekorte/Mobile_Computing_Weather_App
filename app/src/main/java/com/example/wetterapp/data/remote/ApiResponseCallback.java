package com.example.wetterapp.data.remote;

public interface ApiResponseCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}