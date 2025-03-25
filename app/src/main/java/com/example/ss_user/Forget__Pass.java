package com.example.ss_user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Properties;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Forget__Pass extends AppCompatActivity {
    private EditText emailEditText, otpInput, passEditText, confirmPassEditText;
    private TextView generateOtpButton, resetButton;
    private DatabaseReference databaseReference, otpReference;
    private int otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass);

        emailEditText = findViewById(R.id.email);
        otpInput = findViewById(R.id.otp);
        passEditText = findViewById(R.id.pass);
        confirmPassEditText = findViewById(R.id.confirmpass);
        generateOtpButton = findViewById(R.id.generate_otp);
        resetButton = findViewById(R.id.resetpass);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        otpReference = FirebaseDatabase.getInstance().getReference("otp");

        generateOtpButton.setOnClickListener(v -> sendOtp());
        resetButton.setOnClickListener(v -> resetPassword());
    }

    private void sendOtp() {
        String email = emailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean userFound = false;
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot userSnapshot : groupSnapshot.getChildren()) {
                        if (email.equals(userSnapshot.child("email").getValue(String.class))) {
                            userFound = true;
                            String username = userSnapshot.getKey();
                            otp = generateRandomNumber();
                            otpReference.child(groupSnapshot.getKey()).child(username).setValue(otp);
                            sendEmail(email, otp);
                            Toast.makeText(Forget__Pass.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                if (!userFound) {
                    Toast.makeText(Forget__Pass.this, "Email not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Forget__Pass.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();
        String otpString = otpInput.getText().toString().trim();
        String newPassword = passEditText.getText().toString().trim();
        String confirmPassword = confirmPassEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(otpString) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        otpReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean userFound = false;
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot userSnapshot : groupSnapshot.getChildren()) {
                        if (userSnapshot.getValue(Integer.class) == Integer.parseInt(otpString)) {
                            userFound = true;
                            String username = userSnapshot.getKey();
                            databaseReference.child(groupSnapshot.getKey()).child(username).child("password").setValue(newPassword);
                            otpReference.child(groupSnapshot.getKey()).child(username).removeValue();
                            Toast.makeText(Forget__Pass.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Forget__Pass.this, MainActivity.class));
                            finish();
                            return;
                        }
                    }
                }
                if (!userFound) {
                    Toast.makeText(Forget__Pass.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Forget__Pass.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int generateRandomNumber() {
        return new Random().nextInt(900000) + 100000;
    }

    private void sendEmail(String recipientEmail, int otp) {
        final String senderEmail = "ssgroupskolathur@gmail.com";
        final String senderPassword = "oert gstu yimc ewai";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Password Reset OTP");
            message.setText("Your OTP for password reset is: " + otp);

            new Thread(() -> {
                try {
                    Transport.send(message);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Email sent successfully", Toast.LENGTH_SHORT).show());
                } catch (MessagingException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to send email", Toast.LENGTH_SHORT).show());
                }
            }).start();
        } catch (MessagingException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failed to send email", Toast.LENGTH_SHORT).show();
        }
    }
}
