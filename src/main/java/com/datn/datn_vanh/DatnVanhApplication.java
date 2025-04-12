package com.datn.datn_vanh;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class DatnVanhApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatnVanhApplication.class, args);
	}


}
